package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;
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

    public void removeMovie(int movieId, String email, String token) {
        Runnable downloadTask = createTask(movieId, email, token);
        Thread t = new Thread(downloadTask, "THREAD: WATCHLIST PAGE - REMOVE MOVIE");
        t.start();
    }

    private Runnable createTask(int movieId, String email, String token) {
        return ()-> {
            Log.d(TAG, "THREAD: WATCHLIST PAGE - REMOVE MOVIE");
            HttpUrl httpUrl = null;
            // generating url request
            try {
                httpUrl = buildHttpUrl();

            } catch (Exception e) {
                e.printStackTrace();
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }

            // performing http request
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            try {

                RequestBody requestBody = new FormBody.Builder()
                        .add("movieid", String.valueOf(movieId))
                        .add("email", email)
                        .add("access_token", token)
                        .build();

                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                //
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                        Log.d(TAG, "onFailure: ");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();
//                                ArrayList<Movie> result = null;
//                                TheMovieDatabaseApi tmdb = new TheMovieDatabaseApi();

                                if (!responseData.equals("null")) {

                                    // once finished set results
//                                    postSelectedMovies(result);
//                                    postDownloadStatus(DownloadStatus.SUCCESS);

                                }
                                else {
//                                    postSelectedMovies(null);
//                                    postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
//                                postSelectedMovies(null);
//                                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            postSelectedMovies(null);
//                            postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
//                postSelectedMovies(null);
//                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()

    private HttpUrl buildHttpUrl() throws Exception {
        final String dbFunction = "fn_remove_from_list_watchlist";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        return httpUrl;
    }

    public void removeMoviesFromList(ArrayList<Movie> moviesToRemove, String email, String token) {
        if(moviesToRemove!=null) {
            for(Movie m: moviesToRemove) {
                try {
                    removeMovie(m.getTmdbID(), email, token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}// end WatchlistViewModel class
