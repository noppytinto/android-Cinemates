package mirror42.dev.cinemates.ui.search.model;

public class SearchResult {
    protected SearchType searchType;

    public enum SearchType {
        MOVIE,
        USER,
        CAST,
        DIRECTOR,
        UNIVERSAL,
        NONE
    }



    public SearchResult() {

    }

    public SearchType getResultType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }
}
