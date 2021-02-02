package mirror42.dev.cinemates.ui.search;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.exception.CurrentTermEqualsPreviousTermException;
import mirror42.dev.cinemates.exception.EmptyValueException;
import mirror42.dev.cinemates.exception.NoResultException;
import mirror42.dev.cinemates.exception.RemoteDatabaseResponseErrorException;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.search.CastSearchResult;
import mirror42.dev.cinemates.model.search.MovieSearchResult;
import mirror42.dev.cinemates.model.search.SearchResult;
import mirror42.dev.cinemates.model.search.UserSearchResult;
import mirror42.dev.cinemates.model.tmdb.Cast;
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


/**
 * - MutableLiveData objects can be changed(directy and undirectly)
 * - LiveData objects cannot be changed directly!
 *   can only be observed! if changed undirectly!
 *   that's why we have
 *   searchViewModel.getText().observe(...)
 *
 *
 */
public class SearchViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private final int PAGE_1 = 1;
    private MutableLiveData<ArrayList<SearchResult>> searchResultList;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private RemoteConfigServer remoteConfigServer;
    private String previousSearchTerm; // if currentSerchTerm equals the previous one, don't start any new search
    private User loggedUser;





    //--------------------------------------------------------- CONSTRUCTORS

    public SearchViewModel() {
        searchResultList = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        previousSearchTerm="";
    }



    //--------------------------------------------------------- GETTERS/SETTERS

    public void postSearchResultList(ArrayList<SearchResult> searchResultList) {
        this.searchResultList.postValue(searchResultList);
    }

    public LiveData<ArrayList<SearchResult>> getObservableSearchResultList() {
        return searchResultList;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getObservableDownloadStatus() {
        return downloadStatus;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public void resetPreviousSearchTerm() {
        previousSearchTerm = "";
    }

    public ArrayList<SearchResult> getSearchResultList() {
        return searchResultList.getValue();
    }




    //--------------------------------------------------------- METHODS

    public void search(String searchTerm, SearchResult.SearchType searchType)
            throws EmptyValueException, CurrentTermEqualsPreviousTermException {
        if(searchTerm.isEmpty()) throw new EmptyValueException("CINEMATES EXCEPTIONS: Campo vuoto");
        if(searchTerm.equals(previousSearchTerm)) throw new CurrentTermEqualsPreviousTermException("CINEMATES EXCEPTIONS: termine ricerca corrente uguale al precedente");

        previousSearchTerm = searchTerm.trim();
        switch (searchType) {
            case MOVIE: {
                // ignore loggedUser
                Runnable task = createSearchMoviesTask(searchTerm, PAGE_1);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case CAST: {
                // ignore loggedUser
                Runnable task = createSearchCastTask(searchTerm, PAGE_1);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case USER: {
                Runnable task = createSearchUsersTask(searchTerm, loggedUser);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case UNIVERSAL: {
                //TODO:
                Runnable searchMoviesTask = createSearchMoviesTask(searchTerm, PAGE_1);
                Thread t = new Thread(searchMoviesTask);
                t.start();
            }
                break;
        }
    }

    /**
     * PRECONDITIONS: givenQuery must be not null or not empty
     * @param givenQuery
     * @param page
     * @return
     */
    private Runnable createSearchMoviesTask(String givenQuery, int page) {
        return ()-> {
            String movieTitle = givenQuery;
            ArrayList<SearchResult> result = new ArrayList<>();

            try {
                // querying TBDb
                TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
                ArrayList<Movie> movies = tmdb.getMoviesByTitle(movieTitle, page);
                if(movies==null || movies.size()==0) throw new NoResultException("CINEMATES EXCEPTIONS: Nessun risultato");

                // fetching results
                // for each candidate item, we build a SearchResult object
                for(Movie x: movies) {
                    try {
                        SearchResult searchResult = buildMovieSearchResult(x);
                        result.add(searchResult);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "createSearchCastTask: ", e);
                        // just skip
                    }
                }

                // once finished set results
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (NoResultException e) {
                Log.e(TAG, "createSearchMoviesTask: ", e);
                postDownloadStatus(DownloadStatus.NO_RESULT);
            }
        };
    }// end createSearchMoviesTask()

    private Runnable createSearchCastTask(String searchTerm, int page) {
        return ()-> {
            ArrayList<SearchResult> result = new ArrayList<>();
            String personName = searchTerm;

            try {
                // querying TBDb
                TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
                ArrayList<Cast> cast = api.getCastByName(personName, page);
                if(cast==null || cast.size()==0) throw new NoResultException("CINEMATES EXCEPTIONS: Nessun risultato");

                // for each candidate actor, we build a SearchResult object
                for(Cast x: cast) {
                    try {
                        SearchResult searchResult = buildCastSearchResult(x);
                        result.add(searchResult);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "createSearchCastTask: ", e);
                        // just skip
                    }
                }

                // once finished set results
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (NoResultException e) {
                Log.e(TAG, "createSearchCastTask: ", e);
                postDownloadStatus(DownloadStatus.NO_RESULT);
            }
        };
    }// end createSearchCastTask()

    // search by username/firstname/lastname
    private Runnable createSearchUsersTask(String givenQuery, User loggedUser) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_search_users";
                HttpUrl httpUrl = buildHttpUrl(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("search_term", givenQuery)
                        .add("email", loggedUser.getEmail())
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // performing request
                ArrayList<SearchResult> result = new ArrayList<>();
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        postDownloadStatus(DownloadStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if ( ! response.isSuccessful()) throw new RemoteDatabaseResponseErrorException("CINEMATES EXCEPTIONS: errore response Postgres");

                            //
                            String responseData = response.body().string();
                            if(responseData.equals("null")) throw new NoResultException("CINEMATES EXCEPTIONS: Nessun risultato");

                            // if response contains valid data
                            JSONArray jsonArray = new JSONArray(responseData);

                            for(int i=0; i<jsonArray.length(); i++) {
                                JSONObject jsonDBobj = jsonArray.getJSONObject(i);

                                UserSearchResult userSearchResult = new UserSearchResult();
                                userSearchResult.setUsername(jsonDBobj.getString("Username"));
                                userSearchResult.setFirstName(jsonDBobj.getString("Name"));
                                userSearchResult.setLastName(jsonDBobj.getString("LastName"));
                                userSearchResult.setProfilePictureUrl(remoteConfigServer.getCloudinaryDownloadBaseUrl() +
                                        jsonDBobj.getString("ProfileImage"));

                                result.add(userSearchResult);
                            }// for

                            // once finished set result
//                            Collections.reverse(postsList);
                            postSearchResultList(result);
                            postDownloadStatus(DownloadStatus.SUCCESS);
                        } catch (NoResultException e) {
                            Log.e(TAG, "onResponse: ", e);
                            postDownloadStatus(DownloadStatus.NO_RESULT);
                        }  catch (JSONException e){
                            Log.e(TAG, "onResponse: ", e);
                            postDownloadStatus(DownloadStatus.FAILED);
                        }catch (RemoteDatabaseResponseErrorException e) {
                            Log.e(TAG, "onResponse: ", e);
                            postDownloadStatus(DownloadStatus.FAILED);
                        }
                    }// end onResponse()
                });

            } catch (Exception e) {
                e.printStackTrace();
                postDownloadStatus(DownloadStatus.FAILED);
            }
        };
    }// end createSearchUsersTask()

    private CastSearchResult buildCastSearchResult(Cast item) {
        if(item==null) throw new NullPointerException("CINEMATES EXCEPTIONS: argomento nullo");

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        CastSearchResult searchResult = new CastSearchResult.Builder(item.getTmdbID(), item.getFullName())
                .setKnownFor(item.getKnownFor())
                .setProfilePicture(api.buildPersonImageUrl(item.getProfilePictureUrl()))
                .setDepartment(item.getDepartment())
                .build();

        return searchResult;
    }

    private MovieSearchResult buildMovieSearchResult(Movie item) throws NullPointerException{
        if(item==null) throw new NullPointerException("CINEMATES EXCEPTIONS: argomento nullo");

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        MovieSearchResult searchResult = new MovieSearchResult.Builder(item.getTmdbID(), item.getTitle())
                .setOverview(item.getOverview())
                .setPosterURL(api.buildPersonImageUrl(item.getPosterURL()))
                .build();

        return searchResult;
    }

    @NotNull
    private HttpUrl buildHttpUrl(String dbFunction) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();
    }

    // rxjava
//    public Observable<ArrayList<SearchResult>> getMoviesSearchResultObservable(String searchTerm, int page) {
//        return Observable.create(emitter -> {
//            try {
//                // we first search the actor
//                TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
//                ArrayList<Movie> movies = api.getMoviesByTitle(searchTerm, page);
//
//                // for each candidate actor, we build a SearchResult object
//                ArrayList<SearchResult> searchResults = new ArrayList<>();
//                for(Movie x: movies) {
//                    SearchResult searchResult = buildActorSearchResult(x);
//                    if(searchResult!=null) {
//                        searchResults.add(searchResult);
//                    }
//                }
//
//                // then we emit the result
//                emitter.onNext(searchResults);
//                emitter.onComplete();
//            } catch (Exception e) {
//                emitter.onError(e);
//            }
//        });
//    }



//    public Observable<ArrayList<SearchResult>> getCastSearchResultObservable(String searchTerm, int page) {
//        return Observable.create(emitter -> {
//            try {
//                // we first search the actor
//                TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
//                ArrayList<Cast> casts = api.getCastByName(searchTerm, page);
//
//                // for each candidate actor, we build a SearchResult object
//                ArrayList<SearchResult> searchResults = new ArrayList<>();
//                for(Cast x: casts) {
//                    CastSearchResult searchResult = buildCastSearchResult(x);
//                    if(searchResult!=null) {
//                        searchResults.add(searchResult);
//                    }
//                }
//
//                // then we emit the result
//                emitter.onNext(searchResults);
//                emitter.onComplete();
//            } catch (Exception e) {
//                emitter.onError(e);
//            }
//        });
//    }

}// end SearchViewModel class
