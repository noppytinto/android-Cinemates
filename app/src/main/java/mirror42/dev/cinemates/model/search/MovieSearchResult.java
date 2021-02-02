package mirror42.dev.cinemates.model.search;

import java.util.ArrayList;
import java.util.List;

import mirror42.dev.cinemates.model.tmdb.Movie;

public class MovieSearchResult extends SearchResult<MovieSearchResult, Movie> {
    private int tmdbID;
    private String title;
    private String overview;
    private String posterURL;


    public MovieSearchResult() {
        super(SearchType.MOVIE);

    }


    public MovieSearchResult(int tmdbID, String title) {
        super(SearchType.MOVIE);
        this.tmdbID = tmdbID;
        this.title = title;
    }


    public int getTmdbID() {
        return tmdbID;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    //    // builder class
//    public static class Builder {
//        // required parameters
//        private final int tmdbID;
//        private final String title;
//
//        // optional parameters
//        private String posterURL  = null;
//        private String overview  = null;
//
//        // constructor
//        public Builder(int tmdbID, String title) {
//            this.tmdbID = tmdbID;
//            this.title = title;
//        }
//
//        //
//
//        public Builder setPosterURL(String val) {
//            this.posterURL = val;
//            return this;
//        }
//
//        public Builder setOverview(String val) {
//            this.overview = val;
//            return this;
//        }
//
//
//        public MovieSearchResult build() {
//            return new MovieSearchResult(this);
//        }
//
//    }// end Builder class

    @Override
    public List<MovieSearchResult> buildResultList(List<Movie> movies) {
        ArrayList<MovieSearchResult> results = new ArrayList<>();
        if(movies==null) return results;

        for(Movie x: movies) {
            try {
                MovieSearchResult searchResult = buildResult(x);
                results.add(searchResult);
            } catch (NullPointerException e) {
                e.printStackTrace();
                // just skip
            }
        }
        return results;
    }


    @Override
    public MovieSearchResult buildResult(Movie mv) throws NullPointerException {
        if(mv==null) throw new NullPointerException("CINEMATES EXCEPTIONS: argomento nullo");

        MovieSearchResult searchResult = new MovieSearchResult(mv.getTmdbID(), mv.getTitle());
        searchResult.setOverview(mv.getOverview());
        searchResult.setPosterURL(mv.getPosterURL());

        return searchResult;
    }

}// end MovieSearchResult class
