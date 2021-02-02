package mirror42.dev.cinemates.exception;

public class RemoteDatabaseResponseErrorException extends Exception{
    public RemoteDatabaseResponseErrorException(String message, Throwable err) {
        super(message, err);
    }

    public RemoteDatabaseResponseErrorException(String message) {
        super(message);
    }
}
