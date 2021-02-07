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

    public enum AddToListStatus {
        SUCCESS,
        FAILED,
        IDLE
    }



    //----------------------------------------------- CONSTRUCTORS
    public MovieDetailsViewModel() {
        movie = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
        addToListStatus = new MutableLiveData<>(AddToListStatus.IDLE);
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

    public void postAddToListStatus(AddToListStatus addToListStatus) {
        this.addToListStatus.postValue(addToListStatus);
    }

    public LiveData<AddToListStatus> getAddToListStatus() {
        return addToListStatus;
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
            postAddToListStatus(AddToListStatus.IDLE);
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
                    postAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) postAddToListStatus(AddToListStatus.SUCCESS);
                            else postAddToListStatus(AddToListStatus.FAILED);
                        }
                        else postAddToListStatus(AddToListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        postAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            postAddToListStatus(AddToListStatus.FAILED);
        }
    }


    public void addMovieToFavouriteList(int movieId, String email, String accessToken) {
        if(movieId<0) {
            postAddToListStatus(AddToListStatus.IDLE);
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
                    postAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) {
                                postAddToListStatus(AddToListStatus.SUCCESS);
                            }
                            else {
                                postAddToListStatus(AddToListStatus.FAILED);
                            }
                        }
                        else {
                            postAddToListStatus(AddToListStatus.FAILED);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        postAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postAddToListStatus(AddToListStatus.FAILED);
        }
    }

    private void addToWatchedList(int movieId, String email, String accessToken) {
        // checking input
        if(movieId<0) {
            postAddToListStatus(AddToListStatus.IDLE);
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
                    postAddToListStatus(AddToListStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if(responseData.equals("true")) postAddToListStatus(AddToListStatus.SUCCESS);
                            else postAddToListStatus(AddToListStatus.FAILED);
                        }
                        else postAddToListStatus(AddToListStatus.FAILED);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        postAddToListStatus(AddToListStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            postAddToListStatus(AddToListStatus.FAILED);
        }
    }

}// end MovieDetailsViewModel class
