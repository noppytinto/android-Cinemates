package mirror42.dev.cinemates.ui.search;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Cast;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.model.search.CastSearchResult;
import mirror42.dev.cinemates.model.search.MovieSearchResult;
import mirror42.dev.cinemates.model.search.SearchResult;
import mirror42.dev.cinemates.model.search.UserSearchResult;
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





    //--------------------------------------------------------- CONSTRUCTORS

    public SearchViewModel() {
        searchResultList = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }



    //--------------------------------------------------------- GETTERS/SETTERS

    public void postSearchResultList(ArrayList<SearchResult> searchResultList) {
        this.searchResultList.postValue(searchResultList);
    }

    public LiveData<ArrayList<SearchResult>> getSearchResultList() {
        return searchResultList;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }



    //--------------------------------------------------------- METHODS


    public void fetchResults(String givenQuery, SearchResult.SearchType searchType, User loggedUser) {
        switch (searchType) {
            case MOVIE: {
                // ignore loggedUser
                Runnable task = createSearchMoviesTask(givenQuery, PAGE_1);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case CAST: {
                // ignore loggedUser
                Runnable task = createSearchCastTask(givenQuery, PAGE_1);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case USER: {
                Runnable task = createSearchUsersTask(givenQuery, loggedUser);
                Thread t = new Thread(task);
                t.start();
            }
                break;
            case UNIVERSAL: {
                //TODO:
                Runnable searchMoviesTask = createSearchMoviesTask(givenQuery, PAGE_1);
                Thread t = new Thread(searchMoviesTask);
                t.start();
            }
                break;
        }

    }

    private Runnable createSearchUsersTask(String givenQuery, User loggedUser) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_search_users";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
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
                        postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
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
//                                    Collections.reverse(postsList);
                                    postSearchResultList(result);
                                    postDownloadStatus(DownloadStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
                                    postSearchResultList(result);
                                    postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
                                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createSearchUsersTask()

    private Runnable createSearchMoviesTask(String givenQuery, int page) {
        return ()-> {
            String movieTitle = givenQuery;
            ArrayList<SearchResult> result = new ArrayList<>();

            // checking string
            if((movieTitle == null) || (movieTitle.isEmpty())) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postSearchResultList(result);
            }

            try {
                // querying TBDb
                TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
                movieTitle = movieTitle.trim();
                ArrayList<Movie> movies = tmdb.getMoviesByTitle(movieTitle, page);

                // fetching results
                // for each candidate item, we build a SearchResult object
                for(Movie x: movies) {
                    SearchResult searchResult = buildMovieSearchResult(x);
                    if(searchResult!=null) {
                        result.add(searchResult);
                    }
                }

                // once finished set results
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createSearchMoviesTask()

    private Runnable createSearchCastTask(String searchTerm, int page) {
        return ()-> {
            ArrayList<SearchResult> result = new ArrayList<>();
            String personName = searchTerm;

            // checking string
            if((personName == null) || (personName.isEmpty())) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postSearchResultList(result);
            }

            try {
                // querying TBDb
                TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
                personName = personName.trim();
                ArrayList<Cast> cast = api.getCastByName(personName, page);

                // for each candidate actor, we build a SearchResult object
                for(Cast x: cast) {
                    SearchResult searchResult = buildCastSearchResult(x);
                    if(searchResult!=null) {
                        result.add(searchResult);
                    }
                }

                // once finished set results
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createSearchCastTask()

    private CastSearchResult buildCastSearchResult(Cast item) {
        if(item==null) return null;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        CastSearchResult searchResult = new CastSearchResult.Builder(item.getTmdbID(), item.getFullName())
                .setKnownFor(item.getKnownFor())
                .setProfilePicture(api.buildPersonImageUrl(item.getProfilePictureUrl()))
                .setDepartment(item.getDepartment())
                .build();

        return searchResult;
    }

    private MovieSearchResult buildMovieSearchResult(Movie item) {
        if(item==null) return null;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        MovieSearchResult searchResult = new MovieSearchResult.Builder(item.getTmdbID(), item.getTitle())
                .setOverview(item.getOverview())
                .setPosterURL(api.buildPersonImageUrl(item.getPosterURL()))
                .build();

        return searchResult;
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
