package interfaces;

public interface Saveable {
    String serialize();
    void deserialize(String data);
}
