package exception;

public class InvalidDispatchException extends Exception {
    public InvalidDispatchException(String message) {
        super(message);
    }

    public InvalidDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
