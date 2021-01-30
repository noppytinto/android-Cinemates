package mirror42.dev.cinemates.ui.search;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.rxjava3.core.Observable;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.tmdbAPI.model.Cast;
import mirror42.dev.cinemates.ui.search.model.CastSearchResult;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;
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
                Runnable searchMoviesTask = createSearchMoviesTask(givenQuery);
                Thread t = new Thread(searchMoviesTask);
                t.start();
            }
                break;
            case CAST: {
                // ignore loggedUser
                //TODO:
//                Runnable searchActorsTask = createSearchActorsTask(givenQuery);
//                Thread t = new Thread(searchActorsTask);
//                t.start();
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
                Runnable searchMoviesTask = createSearchMoviesTask(givenQuery);
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
                                        userSearchResult.setProfilePictureUrl(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("ProfileImage"));

                                        result.add(userSearchResult);
                                    }// for

                                    // once finished set result
//                                    Collections.reverse(postsList);
                                    postSearchResultList(result);
                                    postDownloadStatus(DownloadStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
                                    postSearchResultList(null);
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
    }

    private Runnable createSearchMoviesTask(String givenQuery) {
        return ()-> {
            String movieTitle = givenQuery;
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<SearchResult> result = null;



            // checking string
            if((movieTitle == null) || (movieTitle.isEmpty())) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postSearchResultList(null);
            }

            movieTitle = movieTitle.trim();
            result = new ArrayList<>();

            try {
                // querying TBDb
                JSONObject jsonObj = tmdb.getJsonMoviesListByTitle(movieTitle, PAGE_1);
                JSONArray resultsArray = jsonObj.getJSONArray("results");

                // fetching results
                for(int i=0; i<resultsArray.length(); i++) {
                    JSONObject x = resultsArray.getJSONObject(i);
                    int id = x.getInt("id");
                    String title = x.getString("title");

                    // if overview is null
                    // getString() will fail
                    // (due to the unvailable defaultLanguage version)
                    // that's why the try-catch
                    String overview = null;
                    try {
                        overview = x.getString("overview");
                        if((overview==null) || (overview.isEmpty()))
                            overview = "(trama non disponibile in italiano)";
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                        overview = "(trama in italiano non disp.)";
                    }

                    // if poster_path is null
                    // getString() will fail
                    // that's why the try-catch
                    String posterURL = null;
                    try {
                        posterURL = x.getString("poster_path");
                        posterURL = tmdb.buildPosterUrl(posterURL);
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                    }

                    //
                    MovieSearchResult mv = new MovieSearchResult(id, title, overview, posterURL);
                    result.add(mv);
                }// for


                // once finished set results
                postSearchResultList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postSearchResultList(null);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()


    //todo: createSearchActorsTask()
    private Runnable createSearchActorsTask(String givenQuery) {
        return ()-> {
            Log.d(TAG, "THREAD: SEARCH PAGE - SEARCH ACTORS");
            String actorName = givenQuery;
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<Cast> result = null;



            // checking string
            if((actorName == null) || (actorName.isEmpty())) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postSearchResultList(null);
            }

            actorName = actorName.trim();
            result = new ArrayList<>();

            try {
                // querying TBDb
                JSONObject jsonObj = tmdb.getJsonActorsListByName(actorName, PAGE_1);
                JSONArray resultsArray = jsonObj.getJSONArray("results");

                // fetching results
                for(int i=0; i<resultsArray.length(); i++) {
                    JSONObject x = resultsArray.getJSONObject(i);
                    int id = x.getInt("id");
                    String name = x.getString("name");

                    // if overview is null
                    // getString() will fail
                    // (due to the unvailable defaultLanguage version)
                    // that's why the try-catch
//                    String overview = null;
//                    try {
//                        overview = x.getString("overview");
//                        if((overview==null) || (overview.isEmpty()))
//                            overview = "(trama non disponibile in italiano)";
//                    } catch (Exception e) {
//                        e.getMessage();
//                        e.printStackTrace();
//                        overview = "(trama in italiano non disp.)";
//                    }

                    // if poster_path is null
                    // getString() will fail
                    // that's why the try-catch
                    String profilePictureUrl = null;
                    try {
                        profilePictureUrl = x.getString("profile_path");
                        profilePictureUrl = tmdb.buildPersonImageUrl(profilePictureUrl);
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                    }

                    //
                    Cast cc = new Cast();
                    cc.setTmdbID(id);
                    cc.setFullName(name);
                    cc.setProfilePictureUrl(profilePictureUrl);

                    result.add(cc);
                }// for


                // once finished set results
//                postMoviesList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postSearchResultList(null);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()

    //todo: createSearchUserTask()
    private Runnable createSearchUserTask() {
        //TODO:

        return null;
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



    public Observable<ArrayList<SearchResult>> getCastSearchResultObservable(String searchTerm, int page) {
        return Observable.create(emitter -> {
            try {
                // we first search the actor
                TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
                ArrayList<Cast> casts = api.getCastByName(searchTerm, page);

                // for each candidate actor, we build a SearchResult object
                ArrayList<SearchResult> searchResults = new ArrayList<>();
                for(Cast x: casts) {
                    CastSearchResult searchResult = buildCastSearchResult(x);
                    if(searchResult!=null) {
                        searchResults.add(searchResult);
                    }
                }

                // then we emit the result
                emitter.onNext(searchResults);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }



    private CastSearchResult buildCastSearchResult(Cast item) {
        if(item==null) return null;

        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
        CastSearchResult searchResult = new CastSearchResult.Builder(item.getTmdbID(), item.getFullName())
                .setKnownFor(item.getKnownFor())
                .setProfilePicture(api.buildPersonImageUrl(item.getProfilePictureUrl()))
                .build();

        return searchResult;
    }

//    private SearchResult buildMovieSearchResult(Actor item) {
//        if(item==null) return null;
//
//        TheMovieDatabaseApi api = TheMovieDatabaseApi.getInstance();
//        MovieSearchResult searchResult = new MovieSearchResult.Builder(item.getTmdbID(), item.getFullName())
//                .setBio(item.getBiography())
//                .setBirthDate(item.getBirthDate())
//                .setKnownFor(item.getKnownFor())
//                .setProfilePicture(api.buildPersonImageUrl(item.getProfilePictureUrl()))
//                .build();
//
//        return searchResult;
//    }

}// end SearchViewModel class
