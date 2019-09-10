package SudokuExplainer.generator;

/**
 * Describe a coordinate information.
 */
public class Point {
    /**
     * X-axis data.
     */
    public final int x;
    /**
     * Y-axis data.
     */
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
