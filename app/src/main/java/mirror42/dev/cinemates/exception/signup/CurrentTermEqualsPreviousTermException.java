package mirror42.dev.cinemates.exception.signup;

public class CurrentTermEqualsPreviousTermException extends SearchException {
    public CurrentTermEqualsPreviousTermException(String message, Throwable err) {
        super(message, err);
    }

    public CurrentTermEqualsPreviousTermException(String message) {
        super(message);
    }
}
