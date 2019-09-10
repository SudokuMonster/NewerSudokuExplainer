package SudokuExplainer.solver.rules.unique;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.units.Cell;


public abstract class BugHint extends IndirectHint implements Rule {

    public BugHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials) {
        super(rule, removablePotentials);
    }

    public String getClueHtml(boolean isBig) {
        if (isBig) {
            return "Look for a " + getName();
        } else {
            return "Look for a Bivalue Universal Grave (BUG)";
        }
    }

}
