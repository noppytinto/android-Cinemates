package mirror42.dev.cinemates.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.Like;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
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
    private ArrayList<Post> watchlistPosts;
    private ArrayList<Post> favoritesPosts;
    private ArrayList<Post> watchedPosts;


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

    public void fetchPosts(User loggedUser) {
        Runnable watchlistPostsTask = createFetchWatchlistPostsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
        Thread t_1 = new Thread(watchlistPostsTask);
        t_1.start();

        try {
            t_1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Runnable favoritesPostsTask = createFetchFavoritesPostsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
        Thread t_2 = new Thread(favoritesPostsTask);
        t_2.start();

        try {
            t_2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Runnable watchedPostsTask = createFetchWatchedPostsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
        Thread t_3 = new Thread(watchedPostsTask);
        t_3.start();
    }

    private Runnable createFetchWatchlistPostsTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", "WL")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        setFetchStatus(FetchStatus.FAILED);
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
//                                    Collections.reverse(postsList);
                                    watchlistPosts = new ArrayList<>();
                                    watchlistPosts.addAll(postsList);
//                                    setFetchStatus(FetchStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
//                                    setPostsList(null);
//                                    setFetchStatus(FetchStatus.NOT_EXISTS);
                                }
                            } // if response is unsuccessful
                            else {
//                                setPostsList(null);
//                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setPostsList(null);
//                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                setPostsList(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchWatchlistPostTask()

    private Runnable createFetchFavoritesPostsTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", "FV")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        setFetchStatus(FetchStatus.FAILED);
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
                                    FavoritesPost favoritesPost;
                                    ArrayList<Post> postsList = new ArrayList<>();

                                    for(int i=0; i<jsonArray.length(); i++) {
                                        JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                                        favoritesPost = buildFavoritesPost(jsonDBobj, email, token, loggedUsername);

                                        postsList.add(favoritesPost);
                                    }// for

                                    // once finished set result
                                    favoritesPosts = new ArrayList<>();
                                    favoritesPosts.addAll(postsList);

//                                    Collections.reverse(postsList);
//                                    setPostsList(postsList);
//                                    setFetchStatus(FetchStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
//                                    setPostsList(null);
//                                    setFetchStatus(FetchStatus.NOT_EXISTS);
                                }
                            } // if response is unsuccessful
                            else {
//                                setPostsList(null);
//                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setPostsList(null);
//                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
//                setPostsList(null);
//                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchFavoritesPostTask()

    private Runnable createFetchWatchedPostsTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", "WD")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        setFetchStatus(FetchStatus.FAILED);
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
                                    WatchedPost watchedPost;
                                    ArrayList<Post> postsList = new ArrayList<>();

                                    for(int i=0; i<jsonArray.length(); i++) {
                                        JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                                        watchedPost = buildWatchedPost(jsonDBobj, email, token, loggedUsername);

                                        postsList.add(watchedPost);
                                    }// for

                                    // once finished set result
//                                    Collections.reverse(postsList);
                                    watchedPosts = new ArrayList<>();

                                    watchedPosts.addAll(postsList);

                                    ArrayList<Post> finalList = new ArrayList<>();
                                    finalList.addAll(watchlistPosts);
                                    finalList.addAll(favoritesPosts);
                                    finalList.addAll(watchedPosts);

                                    finalList = sortPostsByDate(finalList);

                                    setPostsList(finalList);
                                    setFetchStatus(FetchStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
//                                    setPostsList(null);
//                                    setFetchStatus(FetchStatus.NOT_EXISTS);
                                }
                            } // if response is unsuccessful
                            else {
//                                setPostsList(null);
//                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setPostsList(null);
//                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
//                setPostsList(null);
//                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchWatchedPostTask()



    private WatchlistPost buildWatchlistPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);
        // getting movie data
        Movie movie = buildMovie(jsonDBobj);

        // assembling post
        WatchlistPost watchlistPost = new WatchlistPost();
        watchlistPost.setPostId(jsonDBobj.getLong("Id_Post"));
        watchlistPost.setOwner(user);
        watchlistPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        watchlistPost.setDescription("ha aggiunto un film alla Watchlist.");
        watchlistPost.setMovie(movie);
        // getting reactions
        fetchLikes(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchComments(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return watchlistPost;
    }

    private FavoritesPost buildFavoritesPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);
        // getting movie data
        Movie movie = buildMovie(jsonDBobj);

        // assembling post
        FavoritesPost favoritesPost = new FavoritesPost();
        favoritesPost.setPostId(jsonDBobj.getLong("Id_Post"));
        favoritesPost.setOwner(user);
        favoritesPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        favoritesPost.setDescription("ha aggiunto un film nei Preferiti.");
        favoritesPost.setMovie(movie);
        // getting reactions
        fetchLikes(favoritesPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchComments(favoritesPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return favoritesPost;
    }

    private WatchedPost buildWatchedPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);
        // getting movie data
        Movie movie = buildMovie(jsonDBobj);

        // assembling post
        WatchedPost watchedPost = new WatchedPost();
        watchedPost.setPostId(jsonDBobj.getLong("Id_Post"));
        watchedPost.setOwner(user);
        watchedPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        watchedPost.setDescription("ha visto: " + movie.getTitle());
        watchedPost.setMovie(movie);
        // getting reactions
        fetchLikes(watchedPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchComments(watchedPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return watchedPost;
    }

    private User buildOwner(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("Name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }

    public Movie buildMovie(JSONObject jsonObject) throws JSONException {
        TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();

        Movie movie = new Movie();
        movie.setTmdbID(jsonObject.getInt("MovieId"));

        JSONObject jsonTmdbObj = tmdb.getJsonMovieDetailsById(movie.getTmdbID());
        try {
            // if poster_path is null
            // json.getString() will fail
            // that's why the try-catch

            String posterURL = jsonTmdbObj.getString("poster_path");
            posterURL = tmdb.buildPosterUrl(posterURL);
            movie.setPosterURL(posterURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        movie.setTitle(jsonTmdbObj.getString("title"));

        String overview = "";
        try {
            overview = jsonTmdbObj.getString("overview");
            if(overview==null || overview.isEmpty()) movie.setOverview("(trama non disponibile in italiano)");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        movie.setOverview(overview);

        return movie;
    }



    //----------------- likes

    private Post fetchLikes(Post watchlistPost, long postId, String email, String token, String loggedUsername) {
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

    private Post fetchComments(Post watchlistPost, long postId, String email, String token, String loggedUsername) {
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
                        Collections.reverse(comments);
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

    ArrayList<Post> sortPostsByDate(List<Post> list) {
        ArrayList<Post> sortedList = new ArrayList<>(); // create a copy for immutability principle
        sortedList.addAll(list);
        Collections.sort(sortedList);
        return sortedList;
    }

}// end HomeViewModel class
