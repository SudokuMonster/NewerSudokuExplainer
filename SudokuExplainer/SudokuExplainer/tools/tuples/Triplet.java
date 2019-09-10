package SudokuExplainer.tools.tuples;

import java.util.Objects;


/**
 * An object triplet.
 * @param <T1> the type of the first value
 * @param <T2> the type of the second value
 * @param <T3> the type of the third value
 */
public class Triplet<T1, T2, T3> {
    private T1 value1;
    private T2 value2;
    private T3 value3;

    public Triplet(T1 value1, T2 value2, T3 value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public T1 getValue1() {
        return value1;
    }

    public T2 getValue2() {
        return value2;
    }

    public T3 getValue3() {
        return value3;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Triplet) {
            Triplet<?,?,?> other = (Triplet<?,?,?>)o;
            return (eq(this.value1, other.value1) &&
                    eq(this.value2, other.value2) &&
                    eq(this.value3, other.value3));
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
                (value3 != null ? value3.hashCode() : 0);
    }
}
