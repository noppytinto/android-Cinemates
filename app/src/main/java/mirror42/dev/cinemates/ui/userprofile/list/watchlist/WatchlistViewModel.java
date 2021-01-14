package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import mirror42.dev.cinemates.utilities.MyValues.*;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WatchlistViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Movie>> selectedMovies;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private RemoteConfigServer remoteConfigServer;



    //----------------------------------------------------------------------------------------- CONSTRUCTORS
    public WatchlistViewModel() {
        selectedMovies = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }


    //----------------------------------------------------------------------------------------- GETTERS/SETTERS

    public void postSelectedMovies(ArrayList<Movie> moviesList) {
        this.selectedMovies.postValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getSelectedMovies() {
        return selectedMovies;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }



    //----------------------------------------------------------------------------------------- METHODS

    public void fetchData(String email, String token) {
        Runnable downloadTask = createDownloadTask(email, token);
        Thread t = new Thread(downloadTask, "THREAD: LIST PAGE - FETCH ELEMENTS");
        t.start();
    }

    private Runnable createDownloadTask(String email, String token) {
        return ()-> {
            Log.d(TAG, "THREAD: LIST PAGE - FETCH ELEMENTS");
            HttpUrl httpUrl = null;
            // generating url request
            try {
                httpUrl = buildHttpUrl(email);

            } catch (Exception e) {
                e.printStackTrace();
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }

            // performing http request
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            try {

                RequestBody requestBody = new FormBody.Builder()
                        .add("email", email)
                        .build();

                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                //
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
                                TheMovieDatabaseApi tmdb = new TheMovieDatabaseApi();

                                if (!responseData.equals("null")) {
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
                                    postSelectedMovies(result);
                                    postDownloadStatus(DownloadStatus.SUCCESS);

                                }
                                else {
                                    postSelectedMovies(null);
                                    postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
                                postSelectedMovies(null);
                                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                            }
                        } catch (Exception e) {
                            postSelectedMovies(null);
                            postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                        }
                    }
                });
            } catch (Exception e) {
                postSelectedMovies(null);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()

    private HttpUrl buildHttpUrl(String email) throws Exception {
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
