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

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.CustomListCreatedPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.FollowPost;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.reaction.CommentsViewModel.TaskStatus;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import mirror42.dev.cinemates.utilities.ThreadManager;
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
    private MutableLiveData<TaskStatus> taskStatus;
    private static ArrayList<Post> cachedPosts;

    private final String WATCHLIST_POST_TYPE = "WL";
    private final String WATCHED_LIST_POST_TYPE = "WD";
    private final String FAVORITE_LIST_POST_TYPE = "FV";
    private final String CUSTOM_LIST_CREATED_POST_TYPE = "CC";
    private final String CUSTOM_LIST_POST_TYPE = "CL";
    private final String FOLLOW_POST_TYPE = "FW";


    //-------------------------------------------------------------------------- CONSTRUCTORS

    public HomeViewModel() {
        postsList = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
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

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }




    //-------------------------------------------------------------------------- METHODS

    public void fetchLimitedPosts(int page, User loggedUser) {
        Runnable task = createTaskGetLimitedPosts(page, loggedUser);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Runnable createTaskGetLimitedPosts(int page, User loggedUser) {
        return () -> {
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_limited_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", loggedUser.getEmail())
                        .add("page", String.valueOf(page))
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // executing request
                response = httpClient.newCall(request).execute();
                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Post post = null;
                            try {
                                String postType = jsonObject.getString("Type_Post");
                                int postId = jsonObject.getInt("Id_Post");

                                post = buildPost(postId, postType, loggedUser);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if(post!=null)
                                tempResult.add(post);
                        }// for

                        // once finished set result
                        setPostsList(tempResult);
                        setFetchStatus(FetchStatus.SUCCESS);
                    }
                    // if the response is null (no post)
                    else setFetchStatus(FetchStatus.EMPTY);
                }
                // if the response is unsuccesfull
                else setFetchStatus(FetchStatus.FAILED);
            } catch (IOException | JSONException ce) {
                ce.printStackTrace();
                setFetchStatus(FetchStatus.FAILED);
            }finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createTaskgetLimitedPosts()

    private Post buildPost(int postId, String postType, User loggedUser) throws Exception {
        Post post = null;
        switch (postType) {
            case WATCHLIST_POST_TYPE: {
                post = buildWatchlistPost(postId, loggedUser);
            }
                break;
            case WATCHED_LIST_POST_TYPE: {
                post = buildWatchedPost(postId, loggedUser);
            }
                break;
            case FAVORITE_LIST_POST_TYPE: {
                post = buildFavoritesPost(postId, loggedUser);
            }
                break;
            case CUSTOM_LIST_POST_TYPE: {
                post = buildCustomListPost(postId, loggedUser);
            }
                break;
            case CUSTOM_LIST_CREATED_POST_TYPE: {
                post = buildCustomListCreatedPost(postId, loggedUser);
            }
                break;
            case FOLLOW_POST_TYPE: {
                post = buildFollowPost(postId, loggedUser);
            }
                break;
        }
        return post;
    }

    private WatchlistPost buildWatchlistPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        WatchlistPost post = new WatchlistPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    // getting post owner data
                    User user = buildOwner(jsonObject);
                    // getting movie data
                    Movie movie = buildMovie(jsonObject);
                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    post.setDescription("ha aggiunto un film alla Watchlist.");
                    post.setMovie(movie);
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
//                else setFetchStatus(FetchStatus.EMPTY);
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private FavoritesPost buildFavoritesPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        FavoritesPost post = new FavoritesPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    // getting post owner data
                    User user = buildOwner(jsonObject);
                    // getting movie data
                    Movie movie = buildMovie(jsonObject);

                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    post.setDescription("ha aggiunto un film nei Preferiti.");
                    post.setMovie(movie);
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
//                else setFetchStatus(FetchStatus.EMPTY);
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private WatchedPost buildWatchedPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        WatchedPost post = new WatchedPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    // getting post owner data
                    User user = buildOwner(jsonObject);
                    // getting movie data
                    Movie movie = buildMovie(jsonObject);

                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    post.setDescription("ha visto: " + movie.getTitle());
                    post.setMovie(movie);
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
//                else setFetchStatus(FetchStatus.EMPTY);
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private CustomListCreatedPost buildCustomListCreatedPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        CustomListCreatedPost post = new CustomListCreatedPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    // getting post owner data
                    User user = buildOwner(jsonObject);

                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    String listName = jsonObject.getString("list_name");
                    post.setListName(listName);
                    post.setDescription("ha creato la lista: " + listName);
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
                else return null;
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private CustomListPost buildCustomListPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        CustomListPost post = new CustomListPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    // getting post owner data
                    User user = buildOwner(jsonObject);
                    // getting movie data
                    Movie movie = buildMovie(jsonObject);

                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    post.setMovie(movie);
                    String listName = jsonObject.getString("list_name");
                    post.setListName(listName);
                    post.setDescription("ha aggiunto un film alla lista: " + listName);
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
                else return null;
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private FollowPost buildFollowPost(int postId, User loggedUser) throws Exception{
        Response response = null;
        FollowPost post = new FollowPost();
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_post";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("post_id", String.valueOf(postId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            response = httpClient.newCall(request).execute();
            // check responses
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                // if response contains valid data
                if ( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    User user = buildOwner(jsonObject);
                    User followed = buildFollowed(jsonObject);

                    // assembling post
                    post.setPostId(jsonObject.getLong("Id_Post"));
                    post.setOwner(user);
                    post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
                    post.setFollowed(followed);
                    post.setDescription("ora segue: " + followed.getFullName() + " (@" + followed.getUsername() + ")");
                    post.setCommentsCount(jsonObject.getInt("comments_count"));
                    post.setLikesCount(jsonObject.getInt("likes_count"));
                    post.setIsLikedByMe(jsonObject.getBoolean("liked"));

                    // once finished set result
                    return post;
                }
                // if the response is null (no post)
//                else setFetchStatus(FetchStatus.EMPTY);
            }
            // if the response is unsuccesfull
//            else setFetchStatus(FetchStatus.FAILED);
        } catch (IOException | JSONException ce) {
            ce.printStackTrace();
//            setFetchStatus(FetchStatus.FAILED);
        }finally {
            if (response != null) {
                response.close();
            }
        }

        return post;
    }

    private User buildOwner(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("Name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setExternalUser(jsonObject.getBoolean("ExternalAccount"));
        if(user.getIsExternalUser())
            user.setProfilePictureURL(jsonObject.getString("ProfileImage"));
        else
            user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }

    private User buildFollowed(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("followed_username"));
        user.setFirstName(jsonObject.getString("followed_firstname"));
        user.setLastName(jsonObject.getString("followed_lastname"));
        return user;
    }

    private Movie buildMovie(JSONObject jsonObject) throws JSONException {
        TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
        int movieId = jsonObject.getInt("MovieId");
        Movie movie = tmdb.getMoviesDetailsById(movieId);
        return movie;
    }

    public void removeLike(long postId, String email, String token) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_delete_reaction_like";
            //
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
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
                    setTaskStatus(TaskStatus.LIKE_REMOVED_FAIL);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // if response is true
                            if (responseData.equals("true")) {
                                setTaskStatus(TaskStatus.LIKE_REMOVED);
                            }
                            else setTaskStatus(TaskStatus.LIKE_NOT_REMOVED);
                        } else setTaskStatus(TaskStatus.LIKE_REMOVED_FAIL);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTaskStatus(TaskStatus.LIKE_REMOVED_FAIL);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setTaskStatus(TaskStatus.LIKE_REMOVED_FAIL);
        }
    }

    public void addLike(long postId, String email, String token) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_add_reaction_like";
            //
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
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
                    setTaskStatus(TaskStatus.LIKE_ADDED_FAIL);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // if response is true
                            if (responseData.equals("true")) {
                                setTaskStatus(TaskStatus.LIKE_ADDED);
                            }
                            else {
                                setTaskStatus(TaskStatus.LIKE_NOT_ADDED);
                            }
                        } // if response is unsuccessful
                        else {
                            setTaskStatus(TaskStatus.LIKE_ADDED_FAIL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTaskStatus(TaskStatus.LIKE_ADDED_FAIL);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setTaskStatus(TaskStatus.LIKE_ADDED_FAIL);
        }
    }

    public void resetFetchStatus() {
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);

    }

//    public void loadCached() {
//        setPostsList(cachedPosts);
//        setFetchStatus(FetchStatus.CACHED);
//        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
//
//    }

}// end HomeViewModel class
