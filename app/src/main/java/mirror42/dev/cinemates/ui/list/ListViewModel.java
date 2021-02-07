package mirror42.dev.cinemates.ui.list;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.*;
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

public class ListViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Movie>> selectedMovies;
    private MutableLiveData<FetchStatus> fetchStatus;

    /**
     * WL - watchlist
     * FV - favorites list
     * WD - watched list
     * CM - custom list
     */
    public enum ListType {WL, FV, WD, CM}



    //----------------------------------------------------------------------------------------- CONSTRUCTORS
    public ListViewModel() {
        selectedMovies = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
    }


    //----------------------------------------------------------------------------------------- GETTERS/SETTERS

    public void setSelectedMovies(ArrayList<Movie> moviesList) {
        this.selectedMovies.postValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getObservableSelectedMovies() {
        return selectedMovies;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }



    //----------------------------------------------------------------------------------------- METHODS

    public void removeMovie(int movieId, String email, String token) {
        Runnable downloadTask = createTask(movieId, email, token);
        Thread t = new Thread(downloadTask);
        t.start();
    }

    private Runnable createTask(int movieId, String email, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                final String dbFunction = "fn_remove_from_list_watchlist";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                RequestBody requestBody = new FormBody.Builder()
                        .add("movieid", String.valueOf(movieId))
                        .add("email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
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
                setSelectedMovies(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createDownloadTask()

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
