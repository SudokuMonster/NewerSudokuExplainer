package SudokuExplainer.solver.rules.directed;

import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid;
import SudokuExplainer.units.Grid.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;

/**
 * Hidden Single hint
 */
public class HiddenSingleHint extends DirectHint implements Rule {

    private final boolean isAlone; // Last empty cell in a region


    public HiddenSingleHint(DirectHintProducer rule, Region region, Cell cell, int value,
                            boolean isAlone) {
        super(rule, region, cell, value);
        this.isAlone = isAlone;
    }

    public double getDifficulty() {
        if (isAlone)
            return 1.0;
        else if (getRegion() instanceof Grid.Block)
            return 1.2;
        else
            return 1.5;
    }

    public String getName() {
        Grid.Region region = getRegion();
        if (isAlone) {
            return "Full House";
        } else if (region instanceof Grid.Block) {
            return "Hidden Single (Block)";
        } else if (region instanceof Grid.Row) {
            return "Hidden Single (Row)";
        } else {
            return "Hidden Single (Column)";
        }
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
            " in the <b1>" + getRegion().toFullString() + "</b1>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() + ": " + super.toString();
    }

    @Override
    public String toHtml() {
        String result;
        if (isAlone)
            result = HtmlLoader.loadHtml(this, "Single.html");
        else
            result = HtmlLoader.loadHtml(this, "HiddenSingleHint.html");
        return HtmlLoader.format(result, super.getCell().toString(),
                Integer.toString(super.getValue()), super.getRegions()[0].toString());
    }

}
