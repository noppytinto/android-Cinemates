package mirror42.dev.cinemates.utilities;

public class MyValues {

    public enum DownloadStatus {
        IDLE,
        PROCESSING,
        NOT_INITILIZED,
        FAILED_OR_EMPTY,
        SUCCESS,
        NONE
    }

    public enum FetchStatus {
        PROCESSING,
        NOT_INITILIZED,
        FAILED,
        EMPTY,
        SUCCESS,
        MOVIES_DETAILS_DOWNLOADED,
        REFETCH,
        IDLE
    }

    static final String SEARCH_QUERY = "SEARCH_QUERY";
    static final String MOVIE_DETAILS_TRANSFER = "MOVIE_DETAILS_TRANSFER";

}
