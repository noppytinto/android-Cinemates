package mirror42.dev.cinemates.ui.list;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
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

    public void removeSingleMovieFromEssentialList(int movieId, MoviesList.ListType listType, User loggedUser) {
        switch (listType) {
            case WL:
                removeSingleMovieFromWatchlist(movieId, loggedUser.getEmail(), loggedUser.getAccessToken());
                break;
            case FV: {
                removeSingleMovieFromFavouritesList(movieId, loggedUser.getEmail(), loggedUser.getAccessToken());
            }
                break;
            case WD: {
                //TODO
            }
                break;
            case CM: {
                //TODO
            }
                break;
        }
    }

    private void removeSingleMovieFromWatchlist(int movieId, String email, String token) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        try {
            // generating url request
            final String dbFunction = "fn_delete_movie_from_essential_list";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("movieid", String.valueOf(movieId))
                    .add("list_type", "WL")
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
    }// end removeSingleMovieFromWatchlist()

    private void removeSingleMovieFromFavouritesList(int movieId, String email, String token) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        try {
            // generating url request
            final String dbFunction = "fn_delete_movie_from_essential_list";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("movieid", String.valueOf(movieId))
                    .add("list_type", "FV")
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
    }// end removeSingleMovieFromFavouritesList()

    public void removeMoviesFromList(ArrayList<Movie> moviesToRemove, MoviesList.ListType listType,  User loggedUser) {
        if(moviesToRemove!=null) {
            for(Movie m: moviesToRemove) {
                try {
                    removeSingleMovieFromEssentialList(m.getTmdbID(), listType,  loggedUser);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}// end WatchlistViewModel class
