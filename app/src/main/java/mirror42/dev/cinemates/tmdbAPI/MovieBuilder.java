package mirror42.dev.cinemates.tmdbAPI;

import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class MovieBuilder {

    public static ArrayList<Movie> buildMovieList_byID(ArrayList<Integer> IDlist) {
        ArrayList<Movie> movieLs = null;

        try {
            movieLs = new ArrayList<>();
            for(int i=0; i<IDlist.size(); i++) {
                Movie tempMovie = buildMovie_byID(IDlist.get(i));
                movieLs.add(tempMovie);
            }
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return movieLs;
    }

    public static Movie buildMovie_byID(int movieID) {
        TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
        Movie mv = null;

        try {
            String title;
            String overview;
            String posterURL;

            title = tmdb.getMovieTitleById(movieID);
            overview = tmdb.getShortDescriptionById(movieID);
            posterURL = tmdb.getPosterById(movieID);

            mv = new Movie(movieID, title, posterURL, overview);
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return mv;
    }

}// end MovieBuilder class
