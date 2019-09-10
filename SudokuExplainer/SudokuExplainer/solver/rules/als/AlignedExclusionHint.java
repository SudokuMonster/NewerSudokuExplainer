package SudokuExplainer.solver.rules.als;

import java.util.*;
import SudokuExplainer.units.*;
import SudokuExplainer.units.Grid.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Link;


public class AlignedExclusionHint extends IndirectHint implements Rule {

    private final Cell[] cells;
    private final Map<int[], Cell> lockedCombinations;


    public AlignedExclusionHint(AlignedExclusion rule, Map<Cell, BitSet> removables,
            Cell[] cells, Map<int[], Cell> lockedCombinations) {
        super(rule, removables);
        this.cells = cells;
        this.lockedCombinations = lockedCombinations;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return cells;
    }

    private BitSet getRelevantCombinationValues() {
        BitSet result = new BitSet(10);
        for (int[] combination : lockedCombinations.keySet()) {
            if (isRelevant(combination)) {
                for (int value : combination) {
                    result.set(value);
                }
            }
        }
        return result;
    }

    /**
     * Check whether all the bit of the subset <tt>subSet</tt>
     * are contained in the set <tt>set</tt>
     * @param set the set
     * @param subSet the subset
     * @return if the bits of <tt>set</tt> cover the bits of <tt>subSet</tt>
     */
    private boolean contains(BitSet set, BitSet subSet) {
        BitSet temp = (BitSet)set.clone();
        temp.or(subSet);
        return temp.cardinality() == set.cardinality();
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(int viewNum) {
        BitSet relevantValues = getRelevantCombinationValues();
        Map<Cell, BitSet> result = new HashMap<>();
        for (Cell cell : lockedCombinations.values()) {
            if (cell != null) {
                BitSet values = (BitSet)cell.getPotentialValues().clone();
                if (contains(relevantValues, values))
                    result.put(cell, values);
            }
        }
        return appendOranges(result);
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(int viewNum) {
        return appendOranges(super.getRemovablePotentials());
    }

    private Map<Cell, BitSet> appendOranges(Map<Cell, BitSet> values) {
        Map<Cell, BitSet> result = new HashMap<>(values);
        Map<Cell, BitSet> removables = super.getRemovablePotentials();
        for (Cell cell : cells) {
            if (!removables.containsKey(cell)) {
                if (result.containsKey(cell))
                    result.get(cell).or(cell.getPotentialValues());
                else
                    result.put(cell, (BitSet)cell.getPotentialValues().clone());
            }
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(int viewNum) {
        return null;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    public String getName() {
        final String[] names = { "", "", "Pair", "Triple", "Quadruple", "Quintuple", "Sextuple", "Septuple" };
        int degree = cells.length;
        return "Aligned" + names[degree] + "Exclusion";
    }

    public double getDifficulty() {
        int degree = cells.length;
        if (degree == 2)
            return 6.2;
        else if (degree == 3)
            return 7.5;
        else if (degree == 4)
            return 8.3;
        else if (degree == 5)
            return 9.0;
        else
            throw new UnsupportedOperationException();
    }

    public String getClueHtml(boolean isBig) {
        if (isBig)
            return "Look for an " + getName() + " on the cells " + Cell.toString(cells);
        else
            return "Look for an " + getName();
    }

    @Override
    public String toString() {
        return getName() + ": " + Cell.toString(cells);
    }

    /**
     * Test if the given combination of values for the cells
     * are relevant for this rule.
     * A combination is relevant if it includes one of the
     * removable potential.
     * @param combination the combination of values
     * @return whether this combination is relevant
     */
    private boolean isRelevant(int[] combination) {
        assert combination.length == cells.length;
        Map<Cell, BitSet> removables = super.getRemovablePotentials();
        for (int i = 0; i < combination.length; i++) {
            Cell cell = cells[i];
            int value = combination[i];
            if (removables.containsKey(cell) && removables.get(cell).get(value))
                return true;
        }
        return false;
    }

    //private String getColor(Cell cell, int value) {
    //    Map<Cell, BitSet> removables = super.getRemovablePotentials();
    //    if (removables.containsKey(cell)) {
    //        if (removables.get(cell).get(value))
    //            return "r"; // red
    //        else
    //            return null; // no color
    //    } else if (Arrays.asList(cells).contains(cell)) {
    //        return "o"; // Orange
    //    } else {
    //        return null; // no color
    //    }
    //}

    private BitSet getRemovableValues() {
        BitSet result = new BitSet(10);
        for (BitSet values : getRemovablePotentials().values())
            result.or(values);
        return result;
    }

    private void appendCombination(StringBuilder builder, int[] combination,
            Cell lockCell) {
        for (int i = 0; i < combination.length; i++) {
            if (i == combination.length - 1)
                builder.append(" and ");
            else if (i > 0)
                builder.append(", ");
            //String color = getColor(cells[i], combination[i]);
            //if (color != null)
            //    builder.append("<").append(color).append(">");
            builder.append("<b>");
            builder.append(combination[i]);
            builder.append("</b>");
            //if (color != null)
            //    builder.append("</").append(color).append(">");
        }
        builder.append(" because ");
        if (lockCell == null) {
            builder.append("the same value cannot occur twice in the same row, column or block");
        } else {
            builder.append("the cell <b>").append(lockCell.toString()).append("</b> must already contain <b>");
            builder.append(ValuesFormatter.formatValues(lockCell.getPotentialValues(), " or "));
            builder.append("</b>");
        }
        builder.append("<br>");
    }

    @Override
    public String toHtml() {
        String result;
        if (cells.length == 2)
            result = HtmlLoader.loadHtml(this, "AlignedPairExclusionHint.html");
        else
            result = HtmlLoader.loadHtml(this, "AlignedExclusionHint.html");
        String[] names = {"Pair", "Triple", "Quadruple", "Quintuple", "Sextuple", "Septuple"};
        int degree = cells.length;
        String name = names[degree - 2];
        String cellNames = ValuesFormatter.formatCells(cells, " and ");
        StringBuilder rules = new StringBuilder();
        for (int[] combination : lockedCombinations.keySet()) {
            Cell lockCell = lockedCombinations.get(combination);
            if (isRelevant(combination))
                appendCombination(rules, combination, lockCell);
        }
        String ruleList = HtmlLoader.formatColors(rules.toString());
        Cell[] exclCells = new Cell[super.getRemovablePotentials().size()];
        super.getRemovablePotentials().keySet().toArray(exclCells);
        String exclCellNames = ValuesFormatter.formatCells(exclCells, " and ");
        String exclValues = ValuesFormatter.formatValues(getRemovableValues(), ", ");
        return HtmlLoader.format(result, name, cellNames, ruleList, exclCellNames, exclValues);
    }

    private Set<Cell> cellSet() {
        return new HashSet<>(Arrays.asList(cells));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AlignedExclusionHint))
            return false;
        AlignedExclusionHint other = (AlignedExclusionHint)o;
        if (!this.cellSet().equals(other.cellSet()))
            return false;
        return this.getRemovablePotentials().equals(other.getRemovablePotentials());
    }

    @Override
    public int hashCode() {
        int result = getRemovablePotentials().hashCode();
        for (Cell cell : cells)
            result ^= cell.hashCode();
        return result;
    }

}
