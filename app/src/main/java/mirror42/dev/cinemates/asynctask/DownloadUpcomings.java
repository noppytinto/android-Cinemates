package mirror42.dev.cinemates.asynctask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.Movie;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.MyValues.DownloadStatus;


public class DownloadUpcomings extends AsyncTask<Integer, Void, ArrayList<Movie>> {
    private DownloadStatus downloadStatus;
    private TheMovieDatabaseApi tmdb;
    private final DownloadListener callbackCaller;

    // this ensures that the entity which uses this class
    // will implement OnDownloadComplete()
    public interface DownloadListener {
        void onDownloadComplete(ArrayList<Movie> moviesList, DownloadStatus status);
    }



    //------------------------------------------------------------ CONSTRUCTORS

    public DownloadUpcomings(DownloadListener callbackCaller) {
        this.callbackCaller = callbackCaller;
        this.downloadStatus = DownloadStatus.IDLE;
        tmdb = new TheMovieDatabaseApi();
    }






    //------------------------------------------------------------ GETTERS

    @Override
    protected ArrayList<Movie> doInBackground(Integer... integers) {
        int page = integers[0];
        ArrayList<Movie> result = null;

        result = new ArrayList<>();

        try {
            // querying TBDb
            JSONObject jsonObj = tmdb.getJsonUpcoming(page);
            JSONArray resultsArray = jsonObj.getJSONArray("results");

            // fetching results
            for(int i=0; i<resultsArray.length(); i++) {
                JSONObject x = resultsArray.getJSONObject(i);

                int id = x.getInt("id");

                String title = x.getString("title");

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
                Movie mv = new Movie(id, title, posterURL);
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
}// end DownloadUpcomings class
