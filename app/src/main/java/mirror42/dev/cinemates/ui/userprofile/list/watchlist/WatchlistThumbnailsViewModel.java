package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.DownloadStatus;
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

public class WatchlistThumbnailsViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Movie>> moviesList;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private RemoteConfigServer remoteConfigServer;
    private TheMovieDatabaseApi tmdb;


    //----------------------------------------------- CONSTRUCTORS
    public WatchlistThumbnailsViewModel() {
        moviesList = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        tmdb = TheMovieDatabaseApi.getInstance();
    }


    //----------------------------------------------- GETTERS/SETTERS

    public void postMoviesList(ArrayList<Movie> moviesList) {
        this.moviesList.postValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getMoviesList() {
        return moviesList;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }



    //----------------------------------------------- METHODS

    public void fetchData(String email, String token) {
        Runnable downloadTask = createFetchTask(email, token);
        Thread t = new Thread(downloadTask);
        t.start();
    }

    private Runnable createFetchTask(String email, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                HttpUrl httpUrl = buildHttpUrl();
                RequestBody requestBody = new FormBody.Builder()
                        .add("email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();
                                ArrayList<Movie> result = null;

                                if ( ! responseData.equals("null")) {
                                    result = new ArrayList<>();
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    String posterURL = null;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject x = jsonArray.getJSONObject(i);
                                        int movieId = x.getInt("fk_movie");

                                        try{
                                            JSONObject jsonMovieDetails = tmdb.getJsonMovieDetailsById(movieId);

                                            try {
                                                posterURL = jsonMovieDetails.getString("poster_path");
                                                posterURL = tmdb.buildPosterUrl(posterURL);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        Movie movie = new Movie();
                                        movie.setTmdbID(movieId);
                                        movie.setPosterURL(posterURL);

                                        result.add(movie);
                                    }

                                    // once finished set results
                                    Collections.reverse(result);
                                    postMoviesList(result);
                                    postDownloadStatus(DownloadStatus.SUCCESS);

                                }
                                else {
                                    postMoviesList(null);
                                    postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
                                postMoviesList(null);
                                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                            }
                        } catch (Exception e) {
                            postMoviesList(null);
                            postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                        }
                    }
                });
            } catch (Exception e) {
                postMoviesList(null);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()

    private HttpUrl buildHttpUrl() throws Exception {
        final String dbFunction = "fn_get_movies_watchlist";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        return httpUrl;
    }


}// end WatchlistViewModel class
