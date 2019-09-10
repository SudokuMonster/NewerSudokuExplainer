package SudokuExplainer.solver.rules.subset;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.directed.DirectHiddenSetHint;
import SudokuExplainer.solver.rules.directed.HiddenSingle;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;

/**
 * Implementation of hidden set solving techniques
 * (Hidden Pair, Hidden Triplet, Hidden Quad).
 * <p>
 * Only used for degree 2 and below. Degree 1 (hidden single)
 * is implemented in {@link HiddenSingle}.
 */
public class HiddenSubset implements IndirectHintProducer {

    private final int degree;
    private final boolean isDirect;


    public HiddenSubset(int degree, boolean isDirect) {
        assert degree > 1 && degree <= 4;
        this.degree = degree;
        this.isDirect = isDirect;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        getHints(grid, Grid.Block.class, accu);
        getHints(grid, Grid.Column.class, accu);
        getHints(grid, Grid.Row.class, accu);
    }

    /**
     * For each parts of the given type, check if a n-tuple of cells have
     * a common n-tuple of potential values, and no other potential value.
     * @param regionType the type of the parts to check
     * //@param degree the degree of the tuples to search
     */
    private <T extends Grid.Region> void getHints(Grid grid, Class<T> regionType,
            HintsAccumulator accu) throws InterruptedException {
        Grid.Region[] regions = grid.getRegions(regionType);
        // Iterate on parts
        for (Grid.Region region : regions) {
            int nbEmptyCells = region.getEmptyCellCount();
            if (nbEmptyCells > degree * 2 || (isDirect && nbEmptyCells > degree)) {
                Permutations perm = new Permutations(degree, 9);
                // Iterate on tuple of values
                while (perm.hasNext()) {
                    int[] values = perm.nextBitNums();
                    assert values.length == degree;

                    // Build the value tuple
                    for (int i = 0; i < values.length; i++)
                        values[i] += 1; // 0..8 -> 1..9

                    // Build potential positions for each value of the tuple
                    BitSet[] potentialIndexes = new BitSet[degree];
                    for (int i = 0; i < degree; i++)
                        potentialIndexes[i] = region.getPotentialPositions(values[i]);

                    // Look for a common tuple of potential positions, with same degree
                    BitSet commonPotentialPositions =
                        CommonTuples.searchCommonTuple(potentialIndexes, degree);
                    if (commonPotentialPositions != null) {
                        // Hint found
                        IndirectHint hint = createHiddenSetHint(region, values, commonPotentialPositions);
                        if (hint != null && hint.isWorth())
                            accu.add(hint);
                    }
                }
            }
        }
    }

    private IndirectHint createHiddenSetHint(Grid.Region region, int[] values,
            BitSet commonPotentialPositions) {
        // Create set of fixed values, and set of other values
        BitSet valueSet = new BitSet(10);
        for (int value : values)
            valueSet.set(value, true);

        Cell[] cells = new Cell[degree];
        int dstIndex = 0;
        // Look for concerned potentials and removable potentials
        Map<Cell,BitSet> cellPValues = new LinkedHashMap<>();
        Map<Cell,BitSet> cellRemovePValues = new HashMap<>();
        for (int index = 0; index < 9; index++) {
            Cell cell = region.getCell(index);
            if (commonPotentialPositions.get(index)) {
                cellPValues.put(cell, valueSet);
                // Look for the potential values we can remove
                BitSet removablePotentials = new BitSet(10);
                for (int value = 1; value <= 9; value++) {
                    if (!valueSet.get(value) && cell.hasPotentialValue(value))
                        removablePotentials.set(value);
                }
                if (!removablePotentials.isEmpty())
                    cellRemovePValues.put(cell, removablePotentials);
                cells[dstIndex++] = cell;
            }
        }
        if (isDirect) {
            // Look for Hidden Single
            for (int value = 1; value <= 9; value++) {
                if (!valueSet.get(value)) {
                    BitSet positions = region.copyPotentialPositions(value);
                    if (positions.cardinality() > 1) {
                        positions.andNot(commonPotentialPositions);
                        if (positions.cardinality() == 1) {
                            // Hidden single found
                            int index = positions.nextSetBit(0);
                            Cell cell = region.getCell(index);
                            return new DirectHiddenSetHint(this, cells, values, cellPValues,
                                    cellRemovePValues, region, cell, value);
                        }
                    }
                }
            }
            // Nothing found
            return null;
        } else {
            return new HiddenSubsetHint(this, cells, values,
                    cellPValues, cellRemovePValues, region);
        }
    }

    @Override
    public String toString() {
        final String[] names = { "", "", "Pairs", "Triples", "Quadruples", "Quintuples", "Sextuples", "Septuples" };

        if (isDirect && degree <= 3) {
            return "Direct Hidden " + names[degree];
        } else {
            return "Hidden " + names[degree];
        }
    }
}
