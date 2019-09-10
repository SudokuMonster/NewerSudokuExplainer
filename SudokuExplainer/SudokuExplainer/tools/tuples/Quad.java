package SudokuExplainer.tools.tuples;

import java.util.Objects;

public class Quad<T1, T2, T3, T4> {
    private final T1 value1;
    private final T2 value2;
    private final T3 value3;
    private final T4 value4;

    public Quad(T1 value1, T2 value2, T3 value3, T4 value4) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
    }

    public T1 getValue1() {
        return this.value1;
    }

    public T2 getValue2() {
        return this.value2;
    }

    public T3 getValue3() {
        return this.value3;
    }

    public T4 getValue4() {
        return this.value4;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Quad) {
            Quad<?,?,?,?> other = (Quad<?,?,?,?>)o;
            return (eq(this.value1, other.value1) &&
                    eq(this.value2, other.value2) &&
                    eq(this.value3, other.value3) &&
                    eq(this.value4, other.value4));
        }
        return false;
    }

    private boolean eq(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    @Override
    public int hashCode() {
        return (value1 != null ? value1.hashCode() : 0) ^
                (value2 != null ? value2.hashCode() : 0) ^
                (value3 != null ? value3.hashCode() : 0) ^
                (value4 != null ? value4.hashCode() : 0);
    }
}
