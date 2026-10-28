package exception;

public class NoRouteException extends Exception {
    private final String originZone;
    private final String targetZone;

    public NoRouteException(String message) {
        super(message);
        this.originZone = "";
        this.targetZone = "";
    }

    public NoRouteException(String originZone, String targetZone, String message) {
        super(message);
        this.originZone = originZone;
        this.targetZone = targetZone;
    }

    public String getOriginZone() {
        return originZone;
    }

    public String getTargetZone() {
        return targetZone;
    }
}
