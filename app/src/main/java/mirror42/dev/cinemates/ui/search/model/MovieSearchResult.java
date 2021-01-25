package mirror42.dev.cinemates.ui.search.model;

public class MovieSearchResult extends SearchResult {
    private int tmdbID;
    private String title;
    private String overview;
    private String posterURL;

    public MovieSearchResult() {
        searchType = SearchResult.SearchType.MOVIE;
    }

    public MovieSearchResult(int tmdbID, String title, String overview, String posterURL) {
        this();
        this.tmdbID = tmdbID;
        this.title = title;
        this.overview = overview;
        this.posterURL = posterURL;
    }

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }
}
