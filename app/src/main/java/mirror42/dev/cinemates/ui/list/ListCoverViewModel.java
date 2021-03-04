package mirror42.dev.cinemates.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

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
import mirror42.dev.cinemates.utilities.ThreadManager;
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
    private MutableLiveData<FavoritesList> favoritesList;
    private MutableLiveData<WatchedList> watchedList;


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

    public void fetchMyList(User loggedUser, MoviesList.ListType listType) {
        switch (listType) {
            case WL: {
                Runnable task = createFetchWatchlistTask(loggedUser.getUsername(), loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;
            case FV: {
                Runnable task = createFetchFavourites(loggedUser.getUsername(), loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;
            case WD: {
                Runnable task = createFetchWatchedlistTask(loggedUser.getUsername(), loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                break;
        }
    }

    public void fetchOtherUserList(String username, User loggedUser, MoviesList.ListType listType) {
        switch (listType) {
            case WL: {
                Runnable task = createFetchWatchlistTask(username, loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case FV: {
                Runnable task = createFetchFavourites(username, loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            case WD: {
                Runnable task = createFetchWatchedlistTask(username, loggedUser.getAccessToken());
                ThreadManager t = ThreadManager.getInstance();
                try {
                    t.runTaskInPool(task);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }

    private Runnable createFetchWatchlistTask(String username, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                final String dbFunction = "fn_select_essential_list";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", username)
                        .add("list_type", "WL")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    String responseData = response.body().string();
                    ArrayList<Movie> result = null;

                    if ( ! responseData.equals("null")) {
                        result = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject x = jsonArray.getJSONObject(i);
                            int movieId = x.getInt("fk_movie");

                            try{
                                Movie movie = tmdb.getMoviesDetailsById(movieId);
                                result.add(movie);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // once finished set results
                        Watchlist watchlist = new Watchlist();
                        Collections.reverse(result);
                        watchlist.setMovies(result);
                        setWatchlist(watchlist);
                        setFetchStatus(FetchStatus.SUCCESS);
                    }
                    else {
                        setWatchlist(null);
                        setFetchStatus(FetchStatus.FAILED);
                    }


                } catch (Exception e) {
                    setWatchlist(null);
                    setFetchStatus(FetchStatus.FAILED);
                }

            } catch (Exception e) {
                setWatchlist(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchWatchlistTask()

    private Runnable createFetchFavourites(String username, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                final String dbFunction = "fn_select_essential_list";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", username)
                        .add("list_type", "FV")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    String responseData = response.body().string();
                    ArrayList<Movie> result = null;

                    if ( ! responseData.equals("null")) {
                        result = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(responseData);
                        String posterURL = null;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject x = jsonArray.getJSONObject(i);
                            int movieId = x.getInt("fk_Movie");

                            try{
                                Movie movie = tmdb.getMoviesDetailsById(movieId);
                                result.add(movie);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // once finished set results
                        FavoritesList favoritesList = new FavoritesList();
                        Collections.reverse(result);
                        favoritesList.setMovies(result);
                        setFavoritesList(favoritesList);
                        setFetchStatus(FetchStatus.SUCCESS);
                    }
                    else {
                        setFavoritesList(null);
                        setFetchStatus(FetchStatus.FAILED);
                    }

                } catch (Exception e) {
                    setFavoritesList(null);
                    setFetchStatus(FetchStatus.FAILED);
                }
            } catch (Exception e) {
                setFavoritesList(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    } // end createFetchFavourites()

    private Runnable createFetchWatchedlistTask(String username, String token) {
        return ()-> {
            final OkHttpClient httpClient = OkHttpSingleton.getClient();

            try {
                // generating url request
                final String dbFunction = "fn_select_essential_list";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", username)
                        .add("list_type", "WD")
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing http request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setFetchStatus(FetchStatus.FAILED);
                    }
                    String responseData = response.body().string();
                    ArrayList<Movie> result = null;

                    if (!responseData.equals("null")) {
                        result = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject x = jsonArray.getJSONObject(i);
                            int movieId = x.getInt("fk_movie");

                            try {
                                Movie movie = tmdb.getMoviesDetailsById(movieId);
                                result.add(movie);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // once finished set results
                        WatchedList watchedList = new WatchedList();
                        Collections.reverse(result);
                        watchedList.setMovies(result);
                        setWatchedList(watchedList);
                        setFetchStatus(FetchStatus.SUCCESS);
                    } else {
                        setWatchedList(null);
                        setFetchStatus(FetchStatus.FAILED);
                    }

                } catch (Exception e) {
                    setWatchedList(null);
                    setFetchStatus(FetchStatus.FAILED);
                }
            } catch (Exception e) {
                setWatchedList(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };// run
    } // end createFetchWatchedlistTask()




}// end WatchlistViewModel class
