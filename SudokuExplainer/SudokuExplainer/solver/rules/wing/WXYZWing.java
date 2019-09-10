package SudokuExplainer.solver.rules.wing;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;

/**
 * Implementation of the "WXYZ-Wing" and its pivot-incompleted type solving techniques.
 */
public class WXYZWing implements IndirectHintProducer {

    /**
     * Stands for whether pivot cell is completed or not.
     * <ul>
     * <li>Completed: WXYZ WZ XZ YZ</li>
     * <li>Incompleted: WXY WZ XZ YZ</li>
     * </ul>
     */
    private final boolean isIncompletedPivot;

    public WXYZWing(boolean isIncompletedPivot) {
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private boolean isWXYZWing(BitSet wxyzValues, BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        if (wxyzValues.cardinality() != (isIncompletedPivot ? 3 : 4) ||
                wzValues.cardinality() != 2 ||
                xzValues.cardinality() != 2 ||
                yzValues.cardinality() != 2)
            return false;

        BitSet union = (BitSet)wxyzValues.clone();
        union.or(wzValues);
        union.or(xzValues);
        union.or(yzValues);
        BitSet inter = (BitSet)union.clone();
        if (!isIncompletedPivot)
            inter.and(wxyzValues);
        inter.and(wzValues);
        inter.and(xzValues);
        inter.and(yzValues);
        BitSet[] innerProduct = {
                (BitSet)wzValues.clone(),
                (BitSet)wzValues.clone(),
                (BitSet)xzValues.clone()
        };
        innerProduct[0].and(xzValues);
        innerProduct[1].and(yzValues);
        innerProduct[2].and(yzValues);
        BitSet[] outerProduct = {
                (BitSet)wxyzValues.clone(),
                (BitSet)wxyzValues.clone(),
                (BitSet)wxyzValues.clone()
        };
        outerProduct[0].and(wzValues);
        outerProduct[1].and(xzValues);
        outerProduct[2].and(yzValues);
        boolean hasSameBitSet = false;
        boolean hasSameDigit = true;
        for (BitSet b : innerProduct) {
            hasSameBitSet = hasSameBitSet || (b.cardinality() == 2);
        }
        for (BitSet b : outerProduct) {
            hasSameDigit = hasSameDigit && (b.cardinality() == (isIncompletedPivot ? 1 : 2));
        }

        return union.cardinality() == 4 && inter.nextSetBit(0) != -1 && !hasSameBitSet && hasSameDigit;
    }

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        final int targetCardinality = isIncompletedPivot ? 3 : 4;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell wxyzCell = grid.getCell(x, y);
                BitSet wxyzValues = wxyzCell.getPotentialValues();
                if (wxyzValues.cardinality() == targetCardinality) {
                    // Potential WXYZ cell found
                    for (Cell wzCell : wxyzCell.getHouseCells()) {
                        BitSet wzValues = wzCell.getPotentialValues();
                        if (wzValues.cardinality() == 2) {
                            // Potential WZ cell found
                            for (Cell xzCell : wxyzCell.getHouseCells()) {
                                if (!(xzCell.getX() == wzCell.getX() && xzCell.getY() == wzCell.getY())) {
                                    BitSet xzValues = xzCell.getPotentialValues();
                                    if (xzValues.cardinality() == 2) {
                                        // Potential XZ cell found
                                        for (Cell yzCell : wxyzCell.getHouseCells()) {
                                            if (!(yzCell.getX() == xzCell.getX() && yzCell.getY() == xzCell.getY()) &&
                                                    !(yzCell.getX() == wzCell.getX() && yzCell.getY() == wzCell.getY())) {
                                                BitSet yzValues = yzCell.getPotentialValues();
                                                if (yzValues.cardinality() == 2) {
                                                    // Potential YZ cell found
                                                    if (isWXYZWing(wxyzValues, wzValues, xzValues, yzValues)) {
                                                        // Found WXYZ-Wing pattern
                                                        WXYZWingHint hint = createHint(
                                                                wxyzCell, wzCell, xzCell, yzCell,
                                                                wzValues, xzValues, yzValues);
                                                        if (hint.isWorth())
                                                            accu.add(hint);
                                                    } // if isWXYZWing(wxyzValues, wzValues, xzValues, yzValues)
                                                } // if yzValues.cardinality() == 2
                                            } // if yzCell.getX() != xzCell.getX() && yzCell.getY() != xzCell.getY()
                                        } // for Cell yzCell : wxyzCell.getHouseCells()
                                    } // if xzValues.cardinality() == 2
                                } // if xzCell.getX() != wzCell.getX() && xzCell.getY() != wzCell.getY()
                            } // for Cell xzCell : wxyzCell.getHouseCells()
                        } // if wzValues.cardinality() == 2
                    } // for Cell wzCell : wxyzCell.getHouseCells()
                } // if wxyzValues.cardinality() == targetCardinality
            } // for x
        } // for y
    }

    private WXYZWingHint createHint(
            Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell,
            BitSet wzValues, BitSet xzValues, BitSet yzValues) {
        // Get the "z" value
        BitSet inter = (BitSet)wzValues.clone();
        inter.and(xzValues);
        inter.and(yzValues);
        int zValue = inter.nextSetBit(0);

        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<>();
        Set<Cell> victims = new LinkedHashSet<>(wzCell.getHouseCells());
        victims.retainAll(xzCell.getHouseCells());
        victims.retainAll(yzCell.getHouseCells());
        if (!isIncompletedPivot)
            victims.retainAll(wxyzCell.getHouseCells());
        victims.remove(wxyzCell);
        victims.remove(wzCell);
        victims.remove(xzCell);
        victims.remove(yzCell);
        for (Cell cell : victims) {
            if (cell.hasPotentialValue(zValue)) {
                removablePotentials.put(cell, SingletonBitSet.create(zValue));
            }
        }

        // Create hint
        return new WXYZWingHint(this, removablePotentials,
                wxyzCell, wzCell, xzCell, yzCell, zValue, isIncompletedPivot);
    }

    @Override
    public String toString() {
        return "WXYZ-Wings & XYZ-Wing Extensions";
    }
}
