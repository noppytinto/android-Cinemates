package mirror42.dev.cinemates.ui.search.model;

public class MovieSearchResult extends SearchResult {
    private final int tmdbID;
    private final String title;
    private final String overview;
    private final String posterURL;

    public MovieSearchResult(Builder builder) {
        searchType     = SearchResult.SearchType.MOVIE;
        this.tmdbID    = builder.tmdbID;
        this.title     = builder.title;
        this.overview  = builder.overview;
        this.posterURL = builder.posterURL;
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

    // builder class
    public static class Builder {
        // required parameters
        private final int tmdbID;
        private final String title;

        // optional parameters
        private String posterURL  = null;
        private String overview  = null;

        // constructor
        public Builder(int tmdbID, String title) {
            this.tmdbID = tmdbID;
            this.title = title;
        }

        //

        public Builder setPosterURL(String val) {
            this.posterURL = val;
            return this;
        }

        public Builder setOverview(String val) {
            this.overview = val;
            return this;
        }


        public MovieSearchResult build() {
            return new MovieSearchResult(this);
        }

    }// end Builder class





}// end MovieSearchResult class
