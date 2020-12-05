package mirror42.dev.cinemates.tmdbAPI;

import java.util.ArrayList;

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
        TheMovieDatabase_API tmdb = new TheMovieDatabase_API();
        Movie mv = null;

        try {
            String title;
            String overview;
            String posterURL;

            title = tmdb.getMovieTitle_byID(movieID);
            overview = tmdb.getShortDescription_byID(movieID);
            posterURL = tmdb.getPoster_byID(movieID);

            mv = new Movie(movieID, title, posterURL, overview);
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return mv;
    }

}// end MovieBuilder class
