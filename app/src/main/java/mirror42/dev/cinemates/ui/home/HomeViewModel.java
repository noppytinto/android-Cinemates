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

import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
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

    public void fetchData(String email, String token) {
        Runnable watchlistPostTask = createFetchWatchlistPostTask(email, token);
        Thread t = new Thread(watchlistPostTask);
        t.start();
    }

    private Runnable createFetchWatchlistPostTask(String email, String token) {
        return ()-> {
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<Post> result = null;

            try {
                // build httpurl and request for remote db
                HttpUrl httpUrl = buildHttpUrl();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                result = new ArrayList<>();
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
                            //
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();
                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    WatchlistPost watchlistPost;
                                    ArrayList<Post> postsList = new ArrayList<>();

                                    for(int i=0; i<jsonArray.length(); i++) {
                                        JSONObject jsonDBobj = jsonArray.getJSONObject(i);

                                        // getting post owner data
                                        User user = new User();
                                        user.setUsername(jsonDBobj.getString("Username"));
                                        user.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));

                                        // getting movie data
                                        Movie movie = new Movie();
                                        movie.setTmdbID(jsonDBobj.getInt("MovieId"));
                                        try {
                                            // if poster_path is null
                                            // json.getString() will fail
                                            // that's why the try-catch
                                            JSONObject jsonTmdbObj = tmdb.getJsonMovieDetailsById(movie.getTmdbID());
                                            String posterURL = jsonTmdbObj.getString("poster_path");
                                            posterURL = tmdb.buildPosterUrl(posterURL);
                                            movie.setPosterURL(posterURL);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // assembling post
                                        watchlistPost = new WatchlistPost();
                                        watchlistPost.setPostId(jsonDBobj.getLong("Id_Post"));
                                        watchlistPost.setPostType(Post.PostType.ADD_TO_WATCHLIST);
                                        watchlistPost.setOwner(user);
                                        watchlistPost.setPublishDateMillis(jsonDBobj.getLong("Date_Post_Creation"));
                                        watchlistPost.setDescription("ha aggiunto un film alla Watchlist.");
                                        watchlistPost.setMovie(movie);
                                        postsList.add(watchlistPost);
                                    }// for

                                    // once finished set result
                                    Collections.reverse(postsList);
                                    setPostsList(postsList);
                                    setFetchStatus(FetchStatus.SUCCESS);

                                }// if response contains no data
                                else {
                                    setPostsList(null);
                                    setFetchStatus(FetchStatus.EMPTY);
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
        final String dbFunction = "fn_select_watchlist_post";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        return httpUrl;
    }


    private WatchlistPost buildWatchlistPost(String jsonString) {


        return null;
    }



//
//    public void downloadMovieDetails(ArrayList<Movie> moviesList) {
//        Runnable task = createDownloadTask(moviesList);
//        Thread t = new Thread(task, "THREAD: HOME PAGE - DOWNLOAD MOVIE DETAILS");
//        t.start();
//    }
//
//    private Runnable createDownloadTask(ArrayList<Movie> moviesList) {
//        return ()-> {
//            Log.d(TAG, "THREAD: HOME PAGE - DOWNLOAD MOVIE DETAILS");
//            TheMovieDatabaseApi tmdb = new TheMovieDatabaseApi();
//
//            // TODO: check movie id
//
//            Movie result = null;
//            ArrayList<Post> postList = getPostsList().getValue();
//
//            for(Movie m: moviesList) {
//                try {
//
//
//                    m.setPosterURL(posterURL);
//
//                    postsList.getValue().get()
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            ArrayList<Post> postList = getPostsList().getValue();
//            postList
//
//            // once finished set results
//            setFetchStatus(FetchStatus.MOVIES_DETAILS_DOWNLOADED);
//            setPostsList(moviesList);
//        };
//    }// end createDownloadTask()
//




}// end HomeViewModel class
