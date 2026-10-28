package interfaces;

public interface Prioritizable extends Comparable<Prioritizable> {
    double calculateUrgency();

    @Override
    default int compareTo(Prioritizable other) {
        return Double.compare(other.calculateUrgency(), this.calculateUrgency());
    }
}
