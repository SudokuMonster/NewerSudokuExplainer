package SudokuExplainer.solver;

import SudokuExplainer.units.Grid;

/**
 * Interface for techniques that are able to produce warnings and informations.
 * Typically implemented by classes that check the validity of a sudoku.
 */
public interface WarningHintProducer extends IndirectHintProducer {

    void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException;

    String toString();

}
