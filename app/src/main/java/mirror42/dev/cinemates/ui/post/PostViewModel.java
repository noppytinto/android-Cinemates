package mirror42.dev.cinemates.ui.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.CustomListCreatedPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.FollowPost;
import mirror42.dev.cinemates.model.Like;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<Post> postFetched;
    private long postID;




    //------------------------------------------------------ CONSTRUCTOR

    public PostViewModel() {
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        postFetched = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
    }






    //------------------------------------------------------ GETTERS/SETTERS

    public LiveData<FetchStatus> getObservableFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<Post> getObservablePostFetched() {
        return postFetched;
    }

    public void setPostFetched(Post post) {
        this.postFetched.postValue(post);
    }

    public PostType getFetchedPostType() {
        PostType postType = this.postFetched.getValue().getPostType();
        return postType;
    }

    public Post getPostFetched() {
        return this.postFetched.getValue();
    }




    //------------------------------------------------------ MY METHODS

    public void fetchPost(long postId, User loggedUser) {
        this.postID = postId;
        Runnable task = createFetchPostTask(postId, loggedUser);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createFetchPostTask(long postId, User loggedUser) {
        return () -> {
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

                // performing request
                try (Response response = httpClient.newCall(request).execute()) {
                    // check responses
                    if (response.isSuccessful()) {
                        Post result = null;
                        String responseData = response.body().string();

                        if ( ! responseData.equals("null")) {
                            try {
                                JSONObject jsonDBobj = new JSONObject(responseData);
                                result = buildPost(jsonDBobj, loggedUser);

                                setPostFetched(result);
                                setFetchStatus(FetchStatus.SUCCESS);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        setFetchStatus(FetchStatus.NOT_EXISTS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setFetchStatus(FetchStatus.FAILED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }

    private Post buildPost(JSONObject jsonObject, User loggedUser) throws Exception {
        Post post = null;
        String postType = jsonObject.getString("Type_Post");
        switch (postType) {
            case "WL": {
                post = buildWatchlistPost(jsonObject, loggedUser);
            }
                break;
            case "FV": {
                post = buildFavoritesPost(jsonObject, loggedUser);
            }
            break;
            case "WD": {
                post = buildWatchedPost(jsonObject, loggedUser);
            }
            break;
            // TODO
            case "CL": {
                post = buildCustomListPost(jsonObject, loggedUser);
            }
            break;
            case "CC": {
                post = buildCustomListCreatedPost(jsonObject, loggedUser);
            }
            break;
            // TODO
//            case "FW": {
//                post = buildWatchedPost(jsonObject, loggedUser);
//            }
//            break;
            default:
        }

        return post;
    }

    private WatchlistPost buildWatchlistPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // getting movie data
        Movie movie = buildMovie(jsonObject);

        // assembling post
        WatchlistPost post = new WatchlistPost();
        post.setPostId(postID);
        post.setOwner(user);
        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
        post.setDescription("ha aggiunto un film alla Watchlist.");
        post.setMovie(movie);
        fetchPostLikes(post, loggedUser);
        fetchPostComments(post, loggedUser);
        return post;
    }

    private FavoritesPost buildFavoritesPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // getting movie data
        Movie movie = buildMovie(jsonObject);

        // assembling post
        FavoritesPost post = new FavoritesPost();
        post.setPostId(postID);
        post.setOwner(user);
        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
        post.setDescription("ha aggiunto un film nei Preferiti.");
        post.setMovie(movie);
        fetchPostLikes(post, loggedUser);
        fetchPostComments(post, loggedUser);
        return post;
    }

    private WatchedPost buildWatchedPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // getting movie data
        Movie movie = buildMovie(jsonObject);

        // assembling post
        WatchedPost post = new WatchedPost();
        post.setPostId(postID);
        post.setOwner(user);
        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
        post.setDescription("ha visto: " + movie.getTitle());
        post.setMovie(movie);
        fetchPostLikes(post, loggedUser);
        fetchPostComments(post, loggedUser);
        return post;
    }

    private CustomListCreatedPost buildCustomListCreatedPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // assembling post
        CustomListCreatedPost post = new CustomListCreatedPost();
        post.setPostId(postID);
        post.setOwner(user);
        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
        String listName = jsonObject.getString("list_name");
        post.setListName(listName);
        post.setDescription("ha creato la lista: " + listName);
        // getting reactions
        fetchPostLikes(post, loggedUser);
        fetchPostComments(post, loggedUser);
        return post;
    }

    // TODO
    private CustomListPost buildCustomListPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // getting movie data
        Movie movie = buildMovie(jsonObject);

        // assembling post
        CustomListPost post = new CustomListPost();
        post.setPostId(postID);
        post.setOwner(user);
        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
        post.setDescription("aggiunto un film alla lista: " + jsonObject.getString("list_name"));
        post.setMovie(movie);
        fetchPostLikes(post, loggedUser);
        fetchPostComments(post, loggedUser);
        return post;
    }

    // TODO
    private FollowPost buildFollowPost(JSONObject jsonObject, User loggedUser) throws Exception{
        // getting post owner data
        User user = buildUser(jsonObject);

        // assembling post
        FollowPost post = new FollowPost();
//        post.setPostId(postID);
//        post.setOwner(user);
//        post.setPublishDateMillis(jsonObject.getLong("Date_Post_Creation"));
//        String listName = jsonObject.getString("list_name");
//        post.setName(listName);
//        post.setDescription("ha creato la lista: " + listName);
//        // getting reactions
//        fetchPostLikes(post, loggedUser);
//        fetchPostComments(post, loggedUser);
        return post;
    }









    private User buildUser(JSONObject jsonObject) throws JSONException {
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








    //----------------------------------------------------------------------------- REACTIONS

    private Post fetchPostComments(Post watchlistPost, User loggedUser) {
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
                    .add("target_post_id", String.valueOf(postID))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

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
                        watchlistPost.setIsCommentedByMe(loggedUser.getUsername());
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

    private Post fetchPostLikes(Post watchlistPost, User loggedUser) {
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
                    .add("target_post_id", String.valueOf(postID))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

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
                        watchlistPost.setIsLikedByMe(loggedUser.getUsername());
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




}// end PostViewModel class