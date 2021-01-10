package mirror42.dev.cinemates.ui.search;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.utilities.MyValues.*;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;


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
    private final int PAGE_1 = 1;
    private MutableLiveData<ArrayList<Movie>> moviesList;
    private MutableLiveData<DownloadStatus> downloadStatus;



    //----------------------------------------------- CONSTRUCTORS

    public SearchViewModel() {
        moviesList = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
    }


    //----------------------------------------------- GETTERS/SETTERS

    public void postMoviesList(ArrayList<Movie> moviesList) {
        this.moviesList.postValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getMoviesList() {
        return moviesList;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }


    //----------------------------------------------- METHODS

    public void downloadData(String givenQuery) {
        Runnable downloadTask = createDownloadTask(givenQuery);
        Thread t = new Thread(downloadTask, "THREAD: SEARCH PAGE - DOWNLOAD SEARCH RESULTS");
        t.start();
    }

    private Runnable createDownloadTask(String givenQuery) {
        return ()-> {
            String movieTitle = givenQuery;
            TheMovieDatabaseApi tmdb = new TheMovieDatabaseApi();
            ArrayList<Movie> result = null;



            // checking string
            if((movieTitle == null) || (movieTitle.isEmpty())) {
                postDownloadStatus(DownloadStatus.NOT_INITILIZED);
                postMoviesList(null);
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
                    Movie mv = new Movie(id, title, posterURL, overview);
                    result.add(mv);
                }// for


                // once finished set results
                postMoviesList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postMoviesList(null);
                postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
            }
        };
    }// end createDownloadTask()







}// end SearchViewModel class
