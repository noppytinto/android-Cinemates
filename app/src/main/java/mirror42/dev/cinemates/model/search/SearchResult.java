package mirror42.dev.cinemates.model.search;

import java.util.List;

public abstract class SearchResult<T, S> {
    protected SearchType searchType;

    public enum SearchType {
        MOVIE,
        USER,
        CAST,
        DIRECTOR,
        UNIVERSAL,
        NONE
    }

    public SearchResult(SearchType searchType) {
        this.searchType = searchType;
    }

    public SearchType getResultType() {
        return searchType;
    }

    public abstract T  buildResult(S arg);
    public abstract List<T> buildResultList(List<S> args);
}
