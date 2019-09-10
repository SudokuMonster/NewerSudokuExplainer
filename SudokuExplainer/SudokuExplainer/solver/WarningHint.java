package SudokuExplainer.solver;

import java.util.*;

import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Link;

/**
 * A hint that is not really a hint for solving a sudoku, but rather
 * to give an information on the sudoku, such as the fact that the sudoku
 * is not valid.
 */
public abstract class WarningHint extends IndirectHint {

    public WarningHint(WarningHintProducer rule) {
        super(rule, new HashMap<>());
    }

    @Override
    public void apply() {
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return null;
    }

    public Collection<Cell> getRedCells() {
        return Collections.emptyList();
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public boolean isWorth() {
        return true;
    }

}
