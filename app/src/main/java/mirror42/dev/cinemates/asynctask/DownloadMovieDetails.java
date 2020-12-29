package mirror42.dev.cinemates.asynctask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues.DownloadStatus;
import mirror42.dev.cinemates.api.tmdbAPI.Movie;
import mirror42.dev.cinemates.api.tmdbAPI.Person;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;


public class DownloadMovieDetails extends AsyncTask<Integer, Void, Movie> {
    private final int MAX_NUM_CREDITS = 10;
    private DownloadStatus downloadStatus;
    private TheMovieDatabaseApi tmdb;
    private final DownloadListener callbackCaller;

    // this ensures that the entity which uses this class
    // will implement OnDownloadComplete()
    public interface DownloadListener {
        void onDownloadComplete(Movie movie, DownloadStatus status);
    }


    //------------------------------------------------------------ CONSTRUCTORS

    public DownloadMovieDetails(DownloadListener callback) {
        this.callbackCaller = callback;
        this.downloadStatus = DownloadStatus.IDLE;
        tmdb = new TheMovieDatabaseApi();
    }




    //------------------------------------------------------------ GETTERS

    @Override
    protected Movie doInBackground(Integer... integers) {
        int movieID = integers[0];
        Movie result = null;

        // checking input
        if(movieID<0) {
            downloadStatus = DownloadStatus.NOT_INITILIZED;
            return null;
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
            downloadStatus = DownloadStatus.OK;

        } catch (Exception e) {
            // if the search return nothing
            // result will be null
            e.getMessage();
            e.printStackTrace();
            downloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        }

        return result;
    }// end doInBackground()

    private ArrayList<Person> getNCredits(int movieID, int numItems) {
        ArrayList<Person> result = null;

        // checking input
        if((movieID <0) && (numItems >0)) {
            downloadStatus = DownloadStatus.NOT_INITILIZED;
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
            downloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        }

        return result;
    }







    @Override
    protected void onPostExecute(Movie movie) {
        super.onPostExecute(movie);

        // return download status to the caller
        if(callbackCaller != null) {
            callbackCaller.onDownloadComplete(movie, downloadStatus);
        }
    }



    //------------------------------------------------------------ METHODS

}
