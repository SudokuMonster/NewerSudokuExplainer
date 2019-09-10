package SudokuExplainer.solver;

import SudokuExplainer.units.Grid;

/**
 * Interface for solving techniques that are able to produce hints.
 * @see SudokuExplainer.solver.Hint
 */
public interface HintProducer {

    /**
     * Get all the hints that applicable of the given grid according to
     * this solving technique.
     * @param grid the sudoku grid
     * @param accu the accumulator in which to add hints
     * @throws InterruptedException if the search for hints has been interrupted.
     * This exception might be thrown by the accumulator and you must not try
     * to catch it.
     */
    void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException;

}
