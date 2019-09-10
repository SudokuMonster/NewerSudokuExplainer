package SudokuExplainer.tools;

import java.util.*;

/**
 * Factory for <code>BitSet</code>s containing only
 * one element.
 */
public class SingletonBitSet {

    public static BitSet create(int value) {
        BitSet result = new BitSet(10);
        result.set(value);
        return result;
    }
    public static BitSet create(Iterable<Integer> values) {
        BitSet result = new BitSet(10);
        for (int value : values) {
            result.set(value);
        }
        return result;
    }
}
