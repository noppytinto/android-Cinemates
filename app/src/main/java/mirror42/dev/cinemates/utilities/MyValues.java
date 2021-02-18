package mirror42.dev.cinemates.utilities;

public class MyValues {

    public enum DownloadStatus {
        IDLE,
        PROCESSING,
        NOT_INITILIZED,
        FAILED,
        SUCCESS,
        NO_RESULT,
        NONE
    }

    public enum FetchStatus {
        PROCESSING,
        NOT_INITILIZED,
        FAILED,
        NOT_EXISTS,
        SUCCESS,
        MOVIES_DETAILS_DOWNLOADED,
        REFETCH,
        EMPTY,
        IDLE
    }

    static final String SEARCH_QUERY = "SEARCH_QUERY";
    static final String MOVIE_DETAILS_TRANSFER = "MOVIE_DETAILS_TRANSFER";

}
