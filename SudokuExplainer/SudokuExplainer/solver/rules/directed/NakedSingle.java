package SudokuExplainer.solver.rules.directed;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;

/**
 * Implementation of the Naked Single solving techniques.
 */
public class NakedSingle implements DirectHintProducer {

    /**
     * Check if a cell has only one potential value, and accumulate
     * corresponding hints
     */
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        Grid.Region[] parts = grid.getRegions(Grid.Row.class);
        // Iterate on parts
        for (Grid.Region part : parts) {
            // Iterate on cells
            for (int index = 0; index < 9; index++) {
                Cell cell = part.getCell(index);
                // Get the cell's potential values
                BitSet potentialValues = cell.getPotentialValues();
                if (potentialValues.cardinality() == 1) {
                    // One potential value -> solution found
                    int uniqueValue = potentialValues.nextSetBit(0);
                    accu.add(new NakedSingleHint(this, null, cell, uniqueValue));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Naked Singles";
    }

}
