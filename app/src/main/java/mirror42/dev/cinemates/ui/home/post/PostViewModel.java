package mirror42.dev.cinemates.ui.home.post;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
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

public class PostViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<Post> postFetched;




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

    public LiveData<Post> getPostFetched() {
        return postFetched;
    }

    public void setPostFetched(Post post) {
        this.postFetched.postValue(post);
    }




    //------------------------------------------------------ MY METHODS

    public void fetchPost(long postId, User loggedUser) {
        Runnable task = createFetchPostTask(postId, loggedUser);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createFetchPostTask(long postId, User loggedUser) {
        return () -> {
            Post result = null;

            try {
                // build httpurl and request for remote db
                HttpUrl httpUrl = buildHttpUrl();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("post_id", String.valueOf(postId))
                        .add("email", loggedUser.getEmail())
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if ( ! responseData.equals("null")) {
                                try {
                                    JSONObject jsonDBobj = new JSONObject(responseData);




                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                setPostFetched(result);
                                setFetchStatus(FetchStatus.SUCCESS);
                            }
                        }
                        else {
                            setFetchStatus(FetchStatus.NOT_EXISTS);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }


        };
    }

//    private WatchlistPost buildWatchlistPost(JSONObject jsonDBobj, String email, String token, String loggedUsername) throws Exception{
//        // getting post owner data
//        User user = new User();
//        user.setUsername(jsonDBobj.getString("Username"));
//        user.setFirstName(jsonDBobj.getString("Name"));
//        user.setLastName(jsonDBobj.getString("LastName"));
//
//        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));
//
//        // getting movie data
//        Movie movie = new Movie();
//        movie.setTmdbID(jsonDBobj.getInt("MovieId"));
//        try {
//            // if poster_path is null
//            // json.getString() will fail
//            // that's why the try-catch
//            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
//            JSONObject jsonTmdbObj = tmdb.getJsonMovieDetailsById(movie.getTmdbID());
//            String posterURL = jsonTmdbObj.getString("poster_path");
//            posterURL = tmdb.buildPosterUrl(posterURL);
//            movie.setPosterURL(posterURL);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // getting post reactions
//
//        // assembling post
//        WatchlistPost watchlistPost = new WatchlistPost();
//        watchlistPost.setPostId(jsonDBobj.getLong("Id_Post"));
//        watchlistPost.setPostType(Post.PostType.ADD_TO_WATCHLIST);
//        watchlistPost.setOwner(user);
//        watchlistPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
//        watchlistPost.setDescription("ha aggiunto un film alla Watchlist.");
//        watchlistPost.setMovie(movie);
//        fetchWatchlistPostLikes(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
//        fetchWatchlistPostComments(watchlistPost, jsonDBobj.getLong("Id_Post"), email, token, loggedUsername);
//        return watchlistPost;
//    }


    private HttpUrl buildHttpUrl() throws Exception {
        final String dbFunction = "fn_select_post";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        return httpUrl;
    }



}// end PostViewModel class