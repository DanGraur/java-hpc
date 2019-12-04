package benchmark.hardcoded.types;

public class Tuple<T, E> {
    private T first;
    private E second;


    public Tuple(T first, E second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return this.first;
    }

    public E getSecond() {
        return this.second;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

}
