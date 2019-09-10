package SudokuExplainer.solver.rules.wing;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.HasParentPotentialHint;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;
import SudokuExplainer.units.Link;


/**
 * WXYZ-Wing hints
 */
@SuppressWarnings("unused")
public class WXYZWingHint extends IndirectHint implements Rule, HasParentPotentialHint {
    private final Cell wxyzCell;
    private final Cell wzCell;
    private final Cell xzCell;
    private final Cell yzCell;
    private final int value;
    private final boolean isIncompletedPivot;

    public WXYZWingHint(WXYZWing rule, Map<Cell, BitSet> removablePotentials,
            Cell wxyzCell, Cell wzCell, Cell xzCell, Cell yzCell, int value, boolean isIncompletedPivot) {
        super(rule, removablePotentials);
        this.wxyzCell = wxyzCell;
        this.wzCell = wzCell;
        this.xzCell = xzCell;
        this.yzCell = yzCell;
        this.value = value;
        this.isIncompletedPivot = isIncompletedPivot;
    }

    private int getX() {
        BitSet wxyzPotentials = wxyzCell.getPotentialValues();
        int x = wxyzPotentials.nextSetBit(0);
        if (x == this.value)
            x = wxyzPotentials.nextSetBit(x + 1);
        return x;
    }

    private int getY() {
        BitSet wxyzPotentials = wxyzCell.getPotentialValues();
        int x = getX();
        int y = wxyzPotentials.nextSetBit(x + 1);
        if (y == this.value)
            y = wxyzPotentials.nextSetBit(y + 1);
        return y;
    }

    private int getZ() {
        BitSet wxyzPotentials = wxyzCell.getPotentialValues();
        int y = getY();
        int z = wxyzPotentials.nextSetBit(y + 1);
        if (z == this.value)
            z = wxyzPotentials.nextSetBit(z + 1);
        return z;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>();
        // x, y and z of WXYZ cell (orange)
        result.put(wxyzCell, wxyzCell.getPotentialValues());
        // z value (green)
        BitSet zSet = SingletonBitSet.create(value);
        result.put(wzCell, zSet);
        result.put(xzCell, zSet);
        result.put(yzCell, zSet);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        Map<Cell, BitSet> result = new HashMap<>(super.getRemovablePotentials());
        // Add x, y and z of WXYZ cell (orange)
        BitSet wxyz = new BitSet(10);
        wxyz.set(getX());
        wxyz.set(getY());
        wxyz.set(getZ());
        result.put(wxyzCell, wxyz);
        return result;
    }

    public double getDifficulty() {
        return isIncompletedPivot ? 4.5 : 4.6;
    }

    public String getGroup() {
        return "Chaining";
    }

    public String getName() {
        return isIncompletedPivot ? "XYZ-Wing Extension" : "WXYZ-Wing";
    }

    private int getRemainingValue(Cell c) {
        BitSet result = (BitSet)c.getPotentialValues().clone();
        result.clear(value);
        return result.nextSetBit(0);
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        Collection<Link> result = new ArrayList<>();
        int wValue = getRemainingValue(wzCell);
        Link wLink = new Link(wxyzCell, wValue, wzCell, wValue);
        result.add(wLink);
        int xValue = getRemainingValue(xzCell);
        Link xLink = new Link(wxyzCell, xValue, xzCell, xValue);
        result.add(xLink);
        int yValue = getRemainingValue(yzCell);
        Link yLink = new Link(wxyzCell, yValue, yzCell, yValue);
        result.add(yLink);

        return result;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {wxyzCell, wzCell, xzCell, yzCell};
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        Cell wxyzCell = initialGrid.getCell(this.wxyzCell.getX(), this.wxyzCell.getY());
        Cell wzCell = initialGrid.getCell(this.wzCell.getX(), this.wzCell.getY());
        Cell xzCell = initialGrid.getCell(this.xzCell.getX(), this.xzCell.getY());
        Cell yzCell = initialGrid.getCell(this.yzCell.getX(), this.yzCell.getY());
        for (int p = 1; p <= 9; p++) {
            if (wxyzCell.hasPotentialValue(p) && !this.wxyzCell.hasPotentialValue(p))
                result.add(new Potential(this.wxyzCell, p, false));
            if (wzCell.hasPotentialValue(p) && !this.wzCell.hasPotentialValue(p))
                result.add(new Potential(this.wzCell, p, false));
            if (xzCell.hasPotentialValue(p) && !this.xzCell.hasPotentialValue(p))
                result.add(new Potential(this.xzCell, p, false));
            if (yzCell.hasPotentialValue(p) && !this.yzCell.hasPotentialValue(p))
                result.add(new Potential(this.yzCell, p, false));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WXYZWingHint))
            return false;
        WXYZWingHint other = (WXYZWingHint)o;
        if (this.wxyzCell != other.wxyzCell || this.value != other.value)
            return false;
        return this.wzCell == other.wzCell && this.xzCell == other.xzCell && this.yzCell == other.yzCell;
    }

    @Override
    public int hashCode() {
        return wxyzCell.hashCode() ^ wzCell.hashCode() ^ xzCell.hashCode() ^ yzCell.hashCode();
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " on the values " + getX() + ", " + getY() + ", " + getZ() + " and <b>" + value + "</b>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() +
                ": " +
                Cell.toFullString(wxyzCell, wzCell, xzCell, yzCell) +
                " on value " +
                value;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, isIncompletedPivot ? "XYZExtensionHint.html" : "WXYZWingHint.html");
        String cell1 = wxyzCell.toString();
        String cell2 = wzCell.toString();
        String cell3 = xzCell.toString();
        String cell4 = yzCell.toString();
        result = HtmlLoader.format(result, cell1, cell2, cell3, cell4, value, getX(), getY(), getZ());
        return result;
    }
}
