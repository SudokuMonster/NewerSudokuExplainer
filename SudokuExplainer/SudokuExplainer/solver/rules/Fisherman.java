package SudokuExplainer.solver.rules;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;


/**
 * Implementation of X-Wing, Swordfish and Jellyfish solving techniques.
 * The following techniques are implemented depending on the given degree:
 * <ul>
 * <li>Degree 2: X-Wing
 * <li>Degree 3: Swordfish
 * <li>Degree 4: Jellyfish
 * </ul>
 */
public class Fisherman implements IndirectHintProducer {

    private final int degree;


    public Fisherman(int degree) {
        this.degree = degree;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        getHints(grid, Grid.Column.class, Grid.Row.class, accu);
        getHints(grid, Grid.Row.class, Grid.Column.class, accu);
    }

    private <TBaseSet extends Grid.Region, TCoverSet extends Grid.Region> void getHints(
            Grid grid, Class<TBaseSet> baseSetType, Class<TCoverSet> coverSetType, HintsAccumulator accu)
            throws InterruptedException {
        assert !baseSetType.equals(coverSetType);

        // Get occurance count for each value
        int[] occurances = new int[10];
        for (int value = 1; value <= 9; value++)
            occurances[value] = grid.getCountOccurancesOfValue(value);

        Grid.Region[] parts = grid.getRegions(baseSetType);
        // Iterate on lines tuples
        Permutations perm = new Permutations(degree, 9);
        while (perm.hasNext()) {
            int[] indices = perm.nextBitNums();
            assert indices.length == degree;

            BitSet myIndices = new BitSet(9);
            for (int index : indices)
                myIndices.set(index);

            // Iterate on values
            for (int value = 1; value <= 9; value++) {
                // Pattern is only possible if there are at least (degree * 2) missing occurances
                // of the value.
                if (occurances[value] + degree * 2 <= 9) {
                    // Check for exactly the same positions of the value in all lines
                    BitSet[] positions = new BitSet[degree];
                    for (int i = 0; i < degree; i++)
                        positions[i] = parts[indices[i]].getPotentialPositions(value);
                    BitSet common = CommonTuples.searchCommonTuple(positions, degree);

                    if (common != null) {
                        // Potential hint found
                        IndirectHint hint = createFishHint(grid, baseSetType, coverSetType,
                                myIndices, common, value);
                        if (hint.isWorth())
                            accu.add(hint);
                    }
                }
            }
        }
    }

    private <TBaseSet extends Grid.Region, TCoverSet extends Grid.Region> IndirectHint createFishHint(
            Grid grid, Class<TBaseSet> baseSetType, Class<TCoverSet> coverSetType,
            BitSet baseIndices, BitSet coverIndices, int value) {
        Grid.Region[] coverSets = grid.getRegions(coverSetType);
        Grid.Region[] baseSets = grid.getRegions(baseSetType);
        // Build parts
        List<Grid.Region> parts1 = new ArrayList<>();
        List<Grid.Region> parts2 = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (baseIndices.get(i))
                parts1.add(baseSets[i]);
            if (coverIndices.get(i))
                parts2.add(coverSets[i]);
        }
        assert parts1.size() == parts2.size();
        Grid.Region[] allParts = new Grid.Region[parts1.size() + parts2.size()];
        for (int i = 0; i < parts1.size(); i++) {
            allParts[i * 2] = parts1.get(i);
            allParts[i * 2 + 1] = parts2.get(i);
        }

        // Build highlighted potentials and cells
        List<Cell> cells = new ArrayList<>();
        Map<Cell,BitSet> cellPotentials = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (coverIndices.get(i) && baseIndices.get(j)) {
                    Cell cell = coverSets[i].getCell(j);
                    if (cell.hasPotentialValue(value)) {
                        cells.add(cell);
                        cellPotentials.put(cell, SingletonBitSet.create(value));
                    }
                }
            }
        }
        Cell[] allCells = new Cell[cells.size()];
        cells.toArray(allCells);

        // Build removable potentials
        Map<Cell,BitSet> cellRemovablePotentials = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            if (coverIndices.get(i)) {
                // Check if value appears outside from baseIndices
                BitSet potentialPositions = coverSets[i].copyPotentialPositions(value);
                potentialPositions.andNot(baseIndices);
                if (!potentialPositions.isEmpty()) {
                    for (int j = 0; j < 9; j++) {
                        if (potentialPositions.get(j))
                            cellRemovablePotentials.put(coverSets[i].getCell(j),
                                    SingletonBitSet.create(value));
                    }
                }
            }
        }
        return new IntersectionHint(this, allCells, value, cellPotentials,
                cellRemovablePotentials, allParts);
    }

    @Override
    public String toString() {
        if (degree == 2)
            return "X-Wings";
        else if (degree == 3)
            return "Swordfishes";
        else if (degree == 4)
            return "Jellyfishes";
        return null;
    }

}
