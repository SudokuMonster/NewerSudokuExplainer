package SudokuExplainer.tools.tuples;

import java.util.Objects;

/**
 * An object pair.
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 */
public class Pair<T1, T2> {

    private final T1 value1;
    private final T2 value2;

    public Pair(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T1 getValue1() {
        return this.value1;
    }

    public T2 getValue2() {
        return this.value2;
    }

    private boolean eq(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair<?,?> other = (Pair<?,?>)o;
            return (eq(this.value1, other.value1) && eq(this.value2, other.value2));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (value1 != null ? value1.hashCode() : 0) ^ (value2 != null ? value2.hashCode() : 0);
    }

}
