package exception;

public class ResponderUnavailableException extends Exception {
    private final String responderName;

    public ResponderUnavailableException(String message) {
        super(message);
        this.responderName = "Unknown";
    }

    public ResponderUnavailableException(String responderName, String message) {
        super(message);
        this.responderName = responderName;
    }

    public String getResponderName() {
        return responderName;
    }
}
