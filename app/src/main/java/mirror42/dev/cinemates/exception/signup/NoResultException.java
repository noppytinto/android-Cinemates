package mirror42.dev.cinemates.exception.signup;

public class NoResultException extends Exception{
    public NoResultException(String message, Throwable err) {
        super(message, err);
    }

    public NoResultException(String message) {
        super(message);
    }
}
