package mirror42.dev.cinemates.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.Like;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Post>> postsList;
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;


    //-------------------------------------------------------------------------- CONSTRUCTORS

    public HomeViewModel() {
        postsList = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }





    //-------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<ArrayList<Post>> getPostsList() {
        return postsList;
    }

    public void setPostsList(ArrayList<Post> postsList) {
        this.postsList.postValue(postsList);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }





    //-------------------------------------------------------------------------- METHODS

    public void fetchPosts(String email, String token, String loggedUsername) {
        Runnable watchlistPostTask = createFetchWatchlistPostTask(email, token, loggedUsername);
        Thread t = new Thread(watchlistPostTask);
        t.start();
    }

    private Runnable createFetchWatchlistPostTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                HttpUrl httpUrl = buildHttpUrl();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    WatchlistPost watchlistPost;
                                    ArrayList<Post> postsList = new ArrayList<>();

                                    for(int i=0; i<jsonArray.length(); i++) {
                                        JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                                        watchlistPost = buildWatchlistPost(jsonDBobj, email, token, loggedUsername);

                                        postsList.add(watchlistPost);
                                    }// for

                                    // once finished set result
                                    Collections.reverse(postsList);
                                    setPostsList(postsList);
                                    setFetchStatus(FetchStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
                                    setPostsList(null);
                                    setFetchStatus(FetchStatus.NOT_EXISTS);
                                }
                            } // if response is unsuccessful
                            else {
                                setPostsList(null);
                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            setPostsList(null);
                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                setPostsList(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createDownloadTask()

    private HttpUrl buildHttpUrl() throws Exception {
        final String dbFunction = "fn_select_watchlist_posts";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        return httpUrl;
    }

    private WatchlistPost buildWatchlistPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = new User();
        user.setUsername(jsonDBobj.getString("Username"));
        user.setFirstName(jsonDBobj.getString("Name"));
        user.setLastName(jsonDBobj.getString("LastName"));

        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));

        // getting movie data
        Movie movie = new Movie();
        movie.setTmdbID(jsonDBobj.getInt("MovieId"));
        try {
            // if poster_path is null
            // json.getString() will fail
            // that's why the try-catch
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            JSONObject jsonTmdbObj = tmdb.getJsonMovieDetailsById(movie.getTmdbID());
            String posterURL = jsonTmdbObj.getString("poster_path");
            posterURL = tmdb.buildPosterUrl(posterURL);
            movie.setPosterURL(posterURL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // getting post reactions

        // assembling post
        WatchlistPost watchlistPost = new WatchlistPost();
        watchlistPost.setPostId(jsonDBobj.getLong("Id_Post"));
        watchlistPost.setPostType(Post.PostType.ADD_TO_WATCHLIST);
        watchlistPost.setOwner(user);
        watchlistPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        watchlistPost.setDescription("ha aggiunto un film alla Watchlist.");
        watchlistPost.setMovie(movie);
        fetchWatchlistPostLikes(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchWatchlistPostComments(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return watchlistPost;
    }


    //----------------- likes

    private WatchlistPost fetchWatchlistPostLikes(WatchlistPost watchlistPost, long postId, String email, String token, String loggedUsername) {
        try {
            final String dbFunction = "fn_select_likes";
            // building db url
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("target_post_id", String.valueOf(postId))
                    .add("email", email)
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

            // calling synchronously
            String responseData;
            try (Response response = httpClient.newCall(request).execute()) {
                if ( response.isSuccessful()) {
                    responseData = response.body().string();

                    if( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);
                        ArrayList<Like> likes = new ArrayList<>();

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            Like l = new Like();
                            User owner = new User();
                            owner.setFirstName(jsonDBobj.getString("Name"));
                            owner.setLastName(jsonDBobj.getString("LastName"));
                            owner.setUsername(jsonDBobj.getString("Username"));
                            owner.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));

                            l.setOwner(owner);
                            l.setPublishDateMillis(jsonDBobj.getLong("Publish_Date"));
                            likes.add(l);
                        }
                        watchlistPost.setLikes(likes);
                        watchlistPost.setIsLikedByMe(loggedUsername);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return watchlistPost;
    }

    public void removeLike(long postId, String email, String token) {
        Runnable task = createRemoveLikeTask(postId, email, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createRemoveLikeTask(long postId, String email, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_delete_reaction_like";
                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_post_id", String.valueOf(postId))
                        .add("email_reaction_owner", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setFetchStatus(FetchStatus.REFETCH);
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void addLike(long postId, String email, String token) {
        Runnable task = creatAddLikeTask(postId, email, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable creatAddLikeTask(long postId, String email, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_add_reaction_like";
                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_post_id", String.valueOf(postId))
                        .add("email_reaction_owner", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setFetchStatus(FetchStatus.REFETCH);
                                }
                                else {
                                }
                            } // if response is unsuccessful
                            else {
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }


    //----------------- comments

    private WatchlistPost fetchWatchlistPostComments(WatchlistPost watchlistPost, long postId, String email, String token, String loggedUsername) {
        try {
            final String dbFunction = "fn_select_comments";
            // building db url
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("target_post_id", String.valueOf(postId))
                    .add("email", email)
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

            // calling synchronously
            String responseData;
            try (Response response = httpClient.newCall(request).execute()) {
                if ( response.isSuccessful()) {
                    responseData = response.body().string();

                    if( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);
                        ArrayList<Comment> comments = new ArrayList<>();

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            Comment cm = new Comment();
                            User owner = new User();
                            owner.setFirstName(jsonDBobj.getString("Name"));
                            owner.setLastName(jsonDBobj.getString("LastName"));
                            owner.setUsername(jsonDBobj.getString("Username"));
                            owner.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));

                            cm.setOwner(owner);
                            cm.setPublishDateMillis(jsonDBobj.getLong("Publish_Date"));
                            cm.setText(jsonDBobj.getString("Text"));
                            cm.setId(jsonDBobj.getLong("Id_Reaction"));
                            comments.add(cm);
                        }
                        watchlistPost.setComments(comments);
                        watchlistPost.setIsCommentedByMe(loggedUsername);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return watchlistPost;
    }

    public void addComment(long postId, String commentText, String email, String token) {
        Runnable task = creatAddCommentTask(postId, commentText, email, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable creatAddCommentTask(long postId, String commentText, String email, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_add_reaction_comment";
                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_post_id", String.valueOf(postId))
                        .add("email_reaction_owner", email)
                        .add("comment_text", commentText)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setFetchStatus(FetchStatus.REFETCH);
                                }
                                else {
                                }
                            } // if response is unsuccessful
                            else {
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void deleteComment(long commentId, String email, String token) {
        Runnable task = createDeleteCommentTask(commentId, email, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createDeleteCommentTask(long commentId, String email, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_delete_reaction_comment_by_id";
                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("reaction_id", String.valueOf(commentId))
                        .add("email_reaction_owner", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setFetchStatus(FetchStatus.REFETCH);
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

}// end HomeViewModel class
