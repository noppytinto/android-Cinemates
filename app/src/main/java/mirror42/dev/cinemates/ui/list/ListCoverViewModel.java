package mirror42.dev.cinemates.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.FavoritesList;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.list.WatchedList;
import mirror42.dev.cinemates.model.list.Watchlist;
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

public class ListCoverViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<FetchStatus> fetchStatus;
    private TheMovieDatabaseApi tmdb;
    private MutableLiveData<Watchlist> watchlist;
    private MutableLiveData<FavoritesList> favoritesList; //TODO
    private MutableLiveData<WatchedList> watchedList;     //TODO


    //----------------------------------------------------------------------------- CONSTRUCTORS
    public ListCoverViewModel() {
        watchlist = new MutableLiveData<>();
        favoritesList = new MutableLiveData<>();
        watchedList = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        tmdb = TheMovieDatabaseApi.getInstance();
    }


    //----------------------------------------------------------------------------- GETTERS/SETTERS

    public void setWatchlist(Watchlist watchlist) {
        this.watchlist.postValue(watchlist);
    }

    public LiveData<Watchlist> getObservableWatchlist() {
        return watchlist;
    }

    public void setFavoritesList(FavoritesList favoritesList) {
        this.favoritesList.postValue(favoritesList);
    }

    public LiveData<FavoritesList> getObservableFavoritesList() {
        return favoritesList;
    }

    public void setWatchedList(WatchedList watchedList) {
        this.watchedList.postValue(watchedList);
    }

    public LiveData<WatchedList> getObservableWatchedsList() {
        return watchedList;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<FetchStatus> getObservableFetchStatus() {
        return fetchStatus;
    }



    //----------------------------------------------------------------------------- METHODS

    public void fetchList(User loggedUser, MoviesList.ListType listType) {
        switch (listType) {
            case WL: {
                Runnable task = createFetchWatchlistTask(loggedUser.getEmail(), loggedUser.getAccessToken());
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case FV: {
                //TODO
            }
                break;
            case WD: {
                //TODO
            }
                break;
        }
    }

    private Runnable createFetchWatchlistTask(String email, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                final String dbFunction = "fn_select_essential_list";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                RequestBody requestBody = new FormBody.Builder()
                        .add("email", email)
                        .add("list_type", "WL")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        setFetchStatus(FetchStatus.FAILED);
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


                                            Movie movie = new Movie();
                                            movie.setTmdbID(movieId);
                                            movie.setPosterURL(posterURL);

                                            result.add(movie);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    // once finished set results
                                    Watchlist watchlist = new Watchlist();
                                    watchlist.setMovies(result);
                                    setWatchlist(watchlist);
                                    setFetchStatus(FetchStatus.SUCCESS);
                                }
                                else {
                                    setWatchlist(null);
                                    setFetchStatus(FetchStatus.FAILED);
                                }
                            } // if response is unsuccessful
                            else {
                                setWatchlist(null);
                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            setWatchlist(null);
                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });
            } catch (Exception e) {
                setWatchlist(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createDownloadTask()


}// end WatchlistViewModel class
