package SudokuExplainer.solver.checks;

import SudokuExplainer.solver.*;
import SudokuExplainer.units.Grid;

/**
 * Class that computes the solution of a sudoku using brute-force,
 * and produces an hint that allows the user to view the solution.
 */
public class Solution implements WarningHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        Grid solution = new Grid();
        grid.copyTo(solution);

        // First check for no, or multiple solution
        BruteForceAnalysis analyser = new BruteForceAnalysis(true);
        analyser.getHints(grid, accu);
    }

    @Override
    public String toString() {
        return "Brute force analysis";
    }

}
