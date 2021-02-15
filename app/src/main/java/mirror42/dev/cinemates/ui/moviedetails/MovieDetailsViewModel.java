package mirror42.dev.cinemates.ui.moviedetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.model.tmdb.Person;
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

public class MovieDetailsViewModel extends ViewModel {
    private MutableLiveData<Movie> movie;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private TheMovieDatabaseApi tmdb;
    private final int MAX_NUM_CREDITS = 10;
    private MutableLiveData<AddToListStatus> addToListStatus;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<CheckListStatus> isInWatchlistStatus;
    private MutableLiveData<CheckListStatus> isInWatchedListStatus;
    private MutableLiveData<CheckListStatus> isInFavoritesStatus;
    private MutableLiveData<CheckListStatus> custmListsCheckStatus;
    private MutableLiveData<LinkedHashMap<String, Boolean>> custmListsCheckResult;

    public enum AddToListStatus {
        SUCCESS,
        FAILED,
        IDLE
    }

    public enum CheckListStatus {
        IS_IN_WATCHED_LIST,
        IS_IN_WATCHLIST,
        IS_IN_FAVORITES_LIST,
        CUSTOM_LISTS_CHECKED,
        FAILED,
        IDLE
    }

    //----------------------------------------------- CONSTRUCTORS
    public MovieDetailsViewModel() {
        movie = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        addToListStatus = new MutableLiveData<>(AddToListStatus.IDLE);
        isInWatchlistStatus = new MutableLiveData<>(CheckListStatus.IDLE);
        isInWatchedListStatus = new MutableLiveData<>(CheckListStatus.IDLE);
        isInFavoritesStatus = new MutableLiveData<>(CheckListStatus.IDLE);
        custmListsCheckStatus = new MutableLiveData<>(CheckListStatus.IDLE);
        custmListsCheckResult = new MutableLiveData<>();

        tmdb = TheMovieDatabaseApi.getInstance();
        remoteConfigServer = RemoteConfigServer.getInstance();
    }




    //----------------------------------------------- GETTERS/SETTERS

    public void postMovie(Movie movie) {
        this.movie.postValue(movie);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }

    public void setAddToListStatus(AddToListStatus addToListStatus) {
        this.addToListStatus.postValue(addToListStatus);
    }

    public LiveData<AddToListStatus> getAddToListStatus() {
        return addToListStatus;
    }

    public void setIsInWatchlistStatus(CheckListStatus isInWatchlistStatus) {
        this.isInWatchlistStatus.postValue(isInWatchlistStatus);
    }

    public LiveData<CheckListStatus> getWatchlistStatus() {
        return isInWatchlistStatus;
    }

    public void setIsInWatchedListStatus(CheckListStatus isInWatchedListStatus) {
        this.isInWatchedListStatus.postValue(isInWatchedListStatus);
    }

    public LiveData<CheckListStatus> getWatchedListStatus() {
        return isInWatchedListStatus;
    }

    public void setIsInFavoritesListStatus(CheckListStatus isInFavoritesListStatus) {
        this.isInFavoritesStatus.postValue(isInFavoritesListStatus);
    }

    public LiveData<CheckListStatus> getFavoritesListStatus() {
        return isInFavoritesStatus;
    }


    public void setCustomListsCheckStatus(CheckListStatus customListsCheckStatus) {
        this.custmListsCheckStatus.postValue(customListsCheckStatus);
    }

    public LiveData<CheckListStatus> getCustomListsCheckStatus() {
        return custmListsCheckStatus;
    }

    public void setCustomListsCheckResult(LinkedHashMap<String, Boolean> customListsCheckResult) {
        this.custmListsCheckResult.postValue(customListsCheckResult);
    }

    public LiveData<LinkedHashMap<String, Boolean>> getCustomListsCheckResult() {
        return custmListsCheckResult;
    }


    //----------------------------------------------- METHODS

    public void downloadData(int movieId) {
        // starting async task here because of google suggestions
//        DownloadLatestReleases downloadLatestReleases = new DownloadLatestReleases(this);
//        downloadLatestReleases.execute(1);

        Runnable downloadTask = createDownloadTask(movieId);
        Thread t = new Thread(downloadTask);
        t.start();
    }

    private Runnable createDownloadTask(int givenMovieId) {
        return ()-> {
            int movieID = givenMovieId;
            Movie result = null;

            // checking input
            if(movieID<0) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postMovie(null);
            }

            try {
                // querying TBDb
                JSONObject jsonObj = tmdb.getJsonMovieDetailsById(movieID);


                //---- fetching title
                String title = jsonObj.getString("title");

                //---- fetching overview
                // if overview is null
                // getString() will fail
                // (due to the unvailable defaultLanguage version)
                // that's why the try-catch
                String overview = null;
                try {
                    overview = jsonObj.getString("overview");
                    if((overview==null) || (overview.isEmpty()))
                        overview = "(trama non disponibile in italiano)";
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                    overview = "(trama in italiano non disp.)";
                }

                //---- fetching poster_path
                // if poster_path is null
                // getString() will fail
                // that's why the try-catch
                String posterURL = null;
                try {
                    posterURL = jsonObj.getString("poster_path");
                    posterURL = tmdb.buildPosterUrl(posterURL);
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                //---- fetching backdrop_path
                String backDropURL = null;
                try {
                    backDropURL = jsonObj.getString("backdrop_path");
                    backDropURL = tmdb.buildBackdropUrl(backDropURL);
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                //---- fetching duration
                int duration = 0;
                try {
                    duration = jsonObj.getInt("runtime");
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                //---- fetching release date
                String releaseDate = null;
                try {
                    releaseDate = jsonObj.getString("release_date");

                    // extracting year from date
                    if((releaseDate != null) && (!releaseDate.isEmpty())) {
                        releaseDate = "(" + releaseDate.substring(0, 4) + ")";
                    }


                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }


                //---- fetching release status
                String releaseStatus = null;
                try {
                    releaseStatus = jsonObj.getString("status");
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                //---- fetching genres
                ArrayList<String> genres = null;

                try {
                    genres = new ArrayList<>();
                    JSONArray resultsArray = jsonObj.getJSONArray("genres");

                    for(int i=0; i<resultsArray.length(); i++) {
                        JSONObject x = resultsArray.getJSONObject(i);
                        String name = x.getString("name");
                        genres.add(name);
                    }
                } catch (Exception e) {
                    e.getMessage();
                    e.printStackTrace();
                }


                //---- fetching cast&crew
                ArrayList<Person> credits = null;
                credits = getNCredits(movieID, MAX_NUM_CREDITS);


                //----
                result = new Movie(movieID, title, overview, posterURL, backDropURL, duration, releaseStatus, releaseDate, genres, credits);

                // once finished set results
                postMovie(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postMovie(null);
                postDownloadStatus(DownloadStatus.FAILED);
            }
        };
    }// end createDownloadTask()

    private ArrayList<Person> getNCredits(int movieID, int numItems) {
        ArrayList<Person> result = null;

        // checking input
        if((movieID <0) && (numItems >0)) {
            postDownloadStatus(DownloadStatus.NOT_INITILIZED);
            return null;
        }

        //
        try {
            result = new ArrayList<>();

            // querying TBDb
            JSONObject jsonObj = tmdb.getJsonCreditsById(movieID);

            // fetching cast
            JSONArray resultsArray = jsonObj.getJSONArray("cast");
            int i=0;
            for(i=0; (i< numItems)&&(i< resultsArray.length()) ; i++) {
                JSONObject x = resultsArray.getJSONObject(i);

                // fetching TMDB id
                int id = 0;
                try {
                    id = x.getInt("id");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching full name
                String name = null;
                try {
                    name = x.getString("name");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching department
                String department = null;
                try {
                    department = x.getString("known_for_department");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching image path
                String profileImageURL = null;
                try {
                    profileImageURL = x.getString("profile_path");
                    profileImageURL = tmdb.buildPersonImageUrl(profileImageURL);
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching character
                String character = null;
                try {
                    character = x.getString("character");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                result.add(new Person(id, name, character, department, profileImageURL));
            }// end for


            // fetching crew
            for( ; (i< numItems)&&(i<resultsArray.length()); i++) {
                JSONObject x = resultsArray.getJSONObject(i);

                // fetching TMDB id
                int id = 0;
                try {
                    id = x.getInt("id");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching full name
                String name = null;
                try {
                    name = x.getString("name");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching department
                String department = null;
                try {
                    department = x.getString("known_for_department");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching image path
                String profileImageURL = null;
                try {
                    profileImageURL = x.getString("profile_path");
                    profileImageURL = tmdb.buildPersonImageUrl(profileImageURL);
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                // fetching character
                String character = null;
                try {
                    character = x.getString("character");
                } catch (JSONException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                result.add(new Person(id, name, character, department, profileImageURL));
            }// end for
        } catch (Exception e) {
            // if the search return nothing
            // result will be null
            e.getMessage();
            e.printStackTrace();
            postDownloadStatus(DownloadStatus.FAILED);
        }

        return result;
    }


    //--- lists

    public void addMovieToEssentialList(int movieId, MoviesList.ListType listType, User loggedUser) {
        switch (listType) {
            case WL:
                addToWatchlist(movieId, loggedUser.getEmail(), loggedUser.getAccessToken());
                break;
            case FV:
                addMovieToFavouriteList(movieId, loggedUser.getEmail(), loggedUser.getAccessToken());
                break;
            case WD:
                addToWatchedList(movieId, loggedUser.getEmail(), loggedUser.getAccessToken());
        }
    }

    private void addToWatchlist(int movieId, String email, String accessToken) {
        // checking input
        if(movieId<0) {
            setAddToListStatus(AddToListStatus.IDLE);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_insert_into_essential_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movieid", String.valueOf(movieId))
                    .add("email", email)
                    .add("list_type", "WL")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, accessToken);

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setAddToListStatus(AddToListStatus.SUCCESS);
                            else setAddToListStatus(AddToListStatus.FAILED);
                        }
                        else setAddToListStatus(AddToListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setAddToListStatus(AddToListStatus.FAILED);
        }
    }

    public void addMovieToFavouriteList(int movieId, String email, String accessToken) {
        if(movieId<0) {
            setAddToListStatus(AddToListStatus.IDLE);
            return;
        }

        try{
            final String dbFunction = "fn_insert_into_essential_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movieid", String.valueOf(movieId))
                    .add("email", email)
                    .add("list_type", "FV")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, accessToken);

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) {
                                setAddToListStatus(AddToListStatus.SUCCESS);
                            }
                            else {
                                setAddToListStatus(AddToListStatus.FAILED);
                            }
                        }
                        else {
                            setAddToListStatus(AddToListStatus.FAILED);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            setAddToListStatus(AddToListStatus.FAILED);
        }
    }

    private void addToWatchedList(int movieId, String email, String accessToken) {
        // checking input
        if(movieId<0) {
            setAddToListStatus(AddToListStatus.IDLE);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_insert_into_essential_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movieid", String.valueOf(movieId))
                    .add("email", email)
                    .add("list_type", "WD")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, accessToken);

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setAddToListStatus(AddToListStatus.SUCCESS);
                            else setAddToListStatus(AddToListStatus.FAILED);
                        }
                        else setAddToListStatus(AddToListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setAddToListStatus(AddToListStatus.FAILED);
        }
    }

    public void addMovieToCustomList(int movieId, String listName, User loggedUser) {
        // checking input
        if(movieId<0) {
            setAddToListStatus(AddToListStatus.IDLE);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_insert_movie_into_custom_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movie_id", String.valueOf(movieId))
                    .add("list_name", listName)
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setAddToListStatus(AddToListStatus.SUCCESS);
                            else setAddToListStatus(AddToListStatus.FAILED);
                        }
                        else setAddToListStatus(AddToListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setAddToListStatus(AddToListStatus.FAILED);
        }
    }


    //--- checks

    public void checkIsInWatchedList(int movieId, User loggedUser) {
        // checking input
        if(movieId<0) {
            setIsInWatchedListStatus(CheckListStatus.FAILED);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_is_in_watched_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movie_id", String.valueOf(movieId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setIsInWatchedListStatus(CheckListStatus.IS_IN_WATCHED_LIST);
                            else setIsInWatchedListStatus(CheckListStatus.FAILED);
                        }
                        else setIsInWatchedListStatus(CheckListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setIsInWatchedListStatus(CheckListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setIsInWatchedListStatus(CheckListStatus.FAILED);
        }
    }

    public void checkIsInWatchlist(int movieId, User loggedUser) {
        // checking input
        if(movieId<0) {
            setIsInWatchlistStatus(CheckListStatus.FAILED);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_is_in_watchlist";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movie_id", String.valueOf(movieId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setIsInWatchlistStatus(CheckListStatus.IS_IN_WATCHLIST);
                            else setIsInWatchlistStatus(CheckListStatus.FAILED);
                        }
                        else setIsInWatchlistStatus(CheckListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setIsInWatchlistStatus(CheckListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setIsInWatchlistStatus(CheckListStatus.FAILED);
        }
    }

    public void checkIsInFavoritesList(int movieId, User loggedUser) {
        // checking input
        if(movieId<0) {
            setIsInFavoritesListStatus(CheckListStatus.FAILED);
            return;
        }

        // generating url request
        try {
            final String dbFunction = "fn_is_in_favorites_list";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movie_id", String.valueOf(movieId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) setIsInFavoritesListStatus(CheckListStatus.IS_IN_FAVORITES_LIST);
                            else setIsInFavoritesListStatus(CheckListStatus.FAILED);
                        }
                        else setIsInFavoritesListStatus(CheckListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setIsInFavoritesListStatus(CheckListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setIsInFavoritesListStatus(CheckListStatus.FAILED);
        }
    }

    public void checkIsInCustomLists(int movieId, User loggedUser) {
        // checking input
        if(movieId<0) {
            setCustomListsCheckStatus(CheckListStatus.FAILED);
            return;
        }
        // generating url request
        try {
            final String dbFunction = "fn_check_movie_in_my_custom_lists";
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);

            RequestBody requestBody = new FormBody.Builder()
                    .add("movie_id", String.valueOf(movieId))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            LinkedHashMap<String, Boolean> mapping = new LinkedHashMap<>();
                            String responseData = response.body().string();

                            if( ! responseData.equals("null")) {
                                JSONArray jsonArray = new JSONArray(responseData);

                                for(int i=0; i<jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    try {
                                        mapping.put(jsonObject.getString("list_name"), jsonObject.getBoolean("movie_exists"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }// for

                                setCustomListsCheckResult(mapping);
                                setCustomListsCheckStatus(CheckListStatus.CUSTOM_LISTS_CHECKED);
                            }
                            else setCustomListsCheckStatus(CheckListStatus.FAILED);
                        }
                        else setCustomListsCheckStatus(CheckListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setCustomListsCheckStatus(CheckListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setCustomListsCheckStatus(CheckListStatus.FAILED);
        }
    }

}// end MovieDetailsViewModel class
