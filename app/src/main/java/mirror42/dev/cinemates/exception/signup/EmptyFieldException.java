package mirror42.dev.cinemates.exception.signup;

public class EmptyFieldException extends Exception {
    public EmptyFieldException(String message, Throwable err) {
        super(message, err);
    }

    public EmptyFieldException(String message) {
        super(message);
    }

}
