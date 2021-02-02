package mirror42.dev.cinemates.exception;

public class SearchException extends Exception {
    public SearchException(String message, Throwable err) {
        super(message, err);
    }

    public SearchException(String message) {
        super(message);
    }
}
