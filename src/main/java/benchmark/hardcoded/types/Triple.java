package benchmark.hardcoded.types;

public class Triple<T, E, F> {
    private T first;
    private E second;
    private F third;


    public Triple(T first, E second, F third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
        return this.first;
    }

    public E getSecond() {
        return this.second;
    }

    public F getThird() {
        return this.third;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
