package mirror42.dev.cinemates.exception;

public class EmptyValueException extends Exception {
    public EmptyValueException(String message, Throwable err) {
        super(message, err);
    }

    public EmptyValueException(String message) {
        super(message);
    }

}
