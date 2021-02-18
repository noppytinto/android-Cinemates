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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.CustomListCreatedPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.Like;
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
    private SerialDisposable postsSubscription;
    private ArrayList<Post> followPosts; //TODO
    private MutableLiveData<TaskStatus> taskStatus;

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
        postsSubscription = new SerialDisposable();
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

    public void fetchPosts(User loggedUser) {
        if(loggedUser==null) return;

        Observable<ArrayList<Post>> observablePosts =
                getObservablePosts(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

        postsSubscription.set(observablePosts
                .subscribe(notifications -> {
                    if(notifications != null && notifications.size()>0) {
                        setPostsList(notifications);
                        setFetchStatus(FetchStatus.SUCCESS);
                    }
                    else {
                        setFetchStatus(FetchStatus.EMPTY);
                    }
                }));
    }


    // todo: follow post

    // rxjava
    public Observable<ArrayList<Post>> getObservableWatchlistPosts(String email, String token, String loggedUsername) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", WATCHLIST_POST_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            WatchlistPost post = buildWatchlistPost(jsonDBobj, email, token, loggedUsername);

                            tempResult.add(post);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no watchlist posts"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }// end getObservableWatchlistPosts()

    public Observable<ArrayList<Post>> getObservableFavoriteListPosts(String email, String token, String loggedUsername) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", FAVORITE_LIST_POST_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            FavoritesPost post = buildFavoritesPost(jsonDBobj, email, token, loggedUsername);

                            tempResult.add(post);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no favorite posts"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }// end getObservableFavoriteListPosts()

    public Observable<ArrayList<Post>> getObservableCustomListCreatedPosts(String email, String token, String loggedUsername) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", CUSTOM_LIST_CREATED_POST_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            CustomListCreatedPost post = buildCustomListCreatedPost(jsonDBobj, email, token, loggedUsername);

                            tempResult.add(post);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no custom list created posts"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }// end getObservableCustomListCreatedPosts()

    public Observable<ArrayList<Post>> getObservableCustomListPosts(String email, String token, String loggedUsername) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", CUSTOM_LIST_POST_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            CustomListPost post = buildCustomListPost(jsonDBobj, email, token, loggedUsername);

                            tempResult.add(post);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no custom list posts"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }// end getObservableCustomListPosts()

    public Observable<ArrayList<Post>> getObservableWatchedListPosts(String email, String token, String loggedUsername) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Post> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_posts";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("given_email", email)
                        .add("post_type", WATCHED_LIST_POST_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            WatchedPost post = buildWatchedPost(jsonDBobj, email, token, loggedUsername);

                            tempResult.add(post);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no watched list posts"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }// end getObservableWatchedListPosts()

    public Observable<ArrayList<Post>> getObservablePosts(String email, String token, String loggedUsername) {
        Observable<ArrayList<Post>> observableWatchlistPosts =
                getObservableWatchlistPosts(email, token, loggedUsername);

        Observable<ArrayList<Post>> observableFavoriteListPosts =
                getObservableFavoriteListPosts(email, token, loggedUsername);


        Observable<ArrayList<Post>> observableCustomListCreatedPosts =
                getObservableCustomListCreatedPosts(email, token, loggedUsername);

        Observable<ArrayList<Post>> observableCustomListPosts =
                getObservableCustomListPosts(email, token, loggedUsername);

        Observable<ArrayList<Post>> observableWatchedListPosts =
                getObservableWatchedListPosts(email, token, loggedUsername);

        // TODO: follow posts

        Observable<ArrayList<Post>> observableCombinedPosts =
                Observable.combineLatest(
                        observableWatchlistPosts.onErrorReturn(e -> new ArrayList<>()),         // if some error occurs during pipeline,
                        observableFavoriteListPosts.onErrorReturn(e-> new ArrayList<>()),       // don't block the chain,
                        observableCustomListCreatedPosts.onErrorReturn(e-> new ArrayList<>()),  // return an empty list instead
                        observableCustomListPosts.onErrorReturn(e-> new ArrayList<>()),
                        observableWatchedListPosts.onErrorReturn(e-> new ArrayList<>()),
                        (watchlistPosts, favoriteListPosts, customListCreatedPosts, customListPosts, watchedListPosts) -> {
                            final ArrayList<Post> combinedPosts = new ArrayList<>();
                            combinedPosts.addAll(watchlistPosts);
                            combinedPosts.addAll(favoriteListPosts);
                            combinedPosts.addAll(customListCreatedPosts);
                            combinedPosts.addAll(customListPosts);
                            combinedPosts.addAll(watchedListPosts);
                            return combinedPosts;
                        }
                );


        Observable<ArrayList<Post>> observableSortedCombinedPosts =
                observableCombinedPosts
                        .map(this::sortPostsByDate);



        return observableSortedCombinedPosts;
    }

    /**
     * sorting in DESC order
     * @param list
     * @return
     */
    ArrayList<Post> sortPostsByDate(List<Post> list) {
        ArrayList<Post> sortedList = new ArrayList<>(); // create a copy for immutability principle
        sortedList.addAll(list);
        Collections.sort(sortedList);
        return sortedList;
    }






















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

    private CustomListCreatedPost buildCustomListCreatedPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);

        // assembling post
        CustomListCreatedPost post = new CustomListCreatedPost();
        post.setPostId(jsonDBobj.getLong("Id_Post"));
        post.setOwner(user);
        post.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        String listName = jsonDBobj.getString("list_name");
        post.setListName(listName);
        post.setDescription("ha creato la lista: " + listName);
        // getting reactions
        fetchLikes(post, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchComments(post, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return post;
    }

    private CustomListPost buildCustomListPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);
        // getting movie data
        Movie movie = buildMovie(jsonDBobj);

        // assembling post
        CustomListPost post = new CustomListPost();
        post.setPostId(jsonDBobj.getLong("Id_Post"));
        post.setOwner(user);
        post.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
        post.setMovie(movie);
        String listName = jsonDBobj.getString("list_name");
        post.setListName(listName);
        post.setDescription("ha aggiunto un film alla lista: " + listName);
        // getting reactions
        fetchLikes(post, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        fetchComments(post, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
        return post;
    }

    //TODO
//    private FollowPost buildFollowPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
//        // getting post owner data
//        User user = buildOwner(jsonDBobj);
//
//        // assembling post
//        CustomListCreatedPost customListCreatedPost = new CustomListCreatedPost();
//        customListCreatedPost.setPostId(jsonDBobj.getLong("Id_Post"));
//        customListCreatedPost.setOwner(user);
//        customListCreatedPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
//        customListCreatedPost.setName(jsonDBobj.getString("list_name"));
//        customListCreatedPost.setDescription("ha creato la lista: " + jsonDBobj.getString("list_name"));
//        // getting reactions
//        fetchLikes(customListCreatedPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
//        fetchComments(customListCreatedPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
//        return customListCreatedPost;
//    }



    private User buildOwner(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("Name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }

    private Movie buildMovie(JSONObject jsonObject) throws JSONException {
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

    private Post fetchLikes(Post post, long postId, String email, String token, String loggedUsername) {
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
                        post.setLikes(likes);
                        post.setIsLikedByMe(loggedUsername);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return post;
    }

    public void removeLike(long postId, String email, String token) {
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



    //----------------- comments

    private Post fetchComments(Post post, long postId, String email, String token, String loggedUsername) {
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
                        post.setComments(comments);
                        post.setIsCommentedByMe(loggedUsername);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return post;
    }

}// end HomeViewModel class
