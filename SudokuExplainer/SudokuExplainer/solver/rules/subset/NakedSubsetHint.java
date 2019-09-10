package SudokuExplainer.solver.rules.subset;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.solver.rules.HasParentPotentialHint;
import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;
import SudokuExplainer.units.Link;


public class NakedSubsetHint extends IndirectHint implements Rule, HasParentPotentialHint {

    private final Cell[] cells;
    private final int[] values;
    private final Map<Cell, BitSet> highlightPotentials;
    private final Grid.Region region;

    
    public NakedSubsetHint(IndirectHintProducer rule, Cell[] cells,
           int[] values, Map<Cell, BitSet> highlightPotentials,
           Map<Cell, BitSet> removePotentials, Grid.Region region) {
        super(rule, removePotentials);
        this.cells = cells;
        this.values = values;
        this.highlightPotentials = highlightPotentials;
        this.region = region;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return cells;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        return highlightPotentials;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return super.getRemovablePotentials();
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    public double getDifficulty() {
        int degree = values.length;
        if (degree == 2)
            return 3.0;
        else if (degree == 3)
            return 3.6;
        else
            return 5.0;
    }

    public String getName() {
        final String[] groupNames = new String[] {"Pair", "Triple", "Quadruple"};
        return "Naked " + groupNames[values.length - 2];
    }

    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid) {
        Collection<Potential> result = new ArrayList<>();
        BitSet myValues = new BitSet(10);
        for (int item : values)
            myValues.set(item);
        for (Cell cell : this.cells) {
            Cell initialCell = initialGrid.getCell(cell.getX(), cell.getY());
            for (int value = 1; value <= 9; value++) {
                if (initialCell.hasPotentialValue(value) && !myValues.get(value))
                    // This potential must go off before I can be applied
                    result.add(new Potential(cell, value, false));
            }
        }
        return result;
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " in the <b1>" + getRegions()[0].toFullString() + "</b1>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append(": ");
        if (cells.length <= 4)
            builder.append(Cell.toFullString(this.cells));
        else
            builder.append("Cells [...]");
        builder.append(": ");
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                builder.append(",");
            builder.append(values[i]);
        }
        builder.append(" in ");
        builder.append(region.toString());
        return builder.toString();
    }

    @Override
    public String toHtml() {
        final String[] numberNames = new String[] {"two", "three", "four"};
        String result = HtmlLoader.loadHtml(this, "NakedSubsetHint.html");
        String counter = numberNames[values.length - 2];
        String cellList = HtmlLoader.formatList(cells);
        String valueList = HtmlLoader.formatValues(values);
        String regionName = region.toString();
        String ruleName = getName();
        return HtmlLoader.format(result, counter, cellList, valueList, regionName,
                ruleName);
    }

}
