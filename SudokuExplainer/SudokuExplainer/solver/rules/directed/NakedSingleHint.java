package SudokuExplainer.solver.rules.directed;

import SudokuExplainer.units.Cell;
import SudokuExplainer.units.Grid.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;


public class NakedSingleHint extends DirectHint implements Rule {

    public NakedSingleHint(DirectHintProducer rule, Region region, Cell cell, int value) {
        super(rule, region, cell, value);
    }

    public double getDifficulty() {
        return 2.3;
    }

    public String getName() {
        return "Naked Single";
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " in the cell <b>" + getCell().toString() + "</b>";
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
        String result = HtmlLoader.loadHtml(this, "NakedSingleHint.html");
        return HtmlLoader.format(result, Integer.toString(super.getValue()),
                super.getCell().toString());
    }

}
