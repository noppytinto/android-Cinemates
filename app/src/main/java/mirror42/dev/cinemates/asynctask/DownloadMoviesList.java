package mirror42.dev.cinemates.asynctask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues.DownloadStatus;
import mirror42.dev.cinemates.tmdbAPI.Movie;
import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;


public class DownloadMoviesList extends AsyncTask<String, Void, ArrayList<Movie>> {
    private DownloadStatus downloadStatus;
    private TheMovieDatabaseApi tmdb;
    private final DownloadListener callbackCaller;

    // this ensures that the entity which uses this class
    // will implement OnDownloadComplete()
    public interface DownloadListener {
        void onDownloadComplete(ArrayList<Movie> moviesList, DownloadStatus status);
    }



    //------------------------------------------------------------ CONSTRUCTORS

    public DownloadMoviesList(DownloadListener callback) {
        this.callbackCaller = callback;
        this.downloadStatus = DownloadStatus.IDLE;
        tmdb = new TheMovieDatabaseApi();
    }






    //------------------------------------------------------------ GETTERS

    @Override
    protected ArrayList<Movie> doInBackground(String... strings) {
        String movieTitle = strings[0];
        ArrayList<Movie> result = null;

        // checking string
        if((movieTitle == null) || (movieTitle.isEmpty())) {
            downloadStatus = DownloadStatus.NOT_INITILIZED;
            return null;
        }

        movieTitle = movieTitle.trim();
        result = new ArrayList<>();

        try {
            // querying TBDb
            JSONObject jsonObj = tmdb.getJsonMoviesListByTitle(movieTitle, 1);
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
                downloadStatus = DownloadStatus.OK;
            }// for

        } catch (Exception e) {
            // if the search return nothing
            // moviesList will be null
            e.getMessage();
            e.printStackTrace();
            downloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        }

        return result;
    }// end doInBackground()

    @Override
    protected void onPostExecute(ArrayList<Movie> moviesList) {
        super.onPostExecute(moviesList);

        // return download status to the caller
        if(callbackCaller != null) {
            callbackCaller.onDownloadComplete(moviesList, downloadStatus);
        }
    }





    //------------------------------------------------------------ METHODS

}// end DownloadMoviesList class














