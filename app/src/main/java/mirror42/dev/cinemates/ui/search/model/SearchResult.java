package mirror42.dev.cinemates.ui.search.model;

public class SearchResult {
    private SearchType searchType;

    public enum SearchType {
        MOVIE,
        USER,
        ACTOR,
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
