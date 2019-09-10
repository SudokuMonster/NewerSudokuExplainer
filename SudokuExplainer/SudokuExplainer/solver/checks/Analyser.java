package SudokuExplainer.solver.checks;

import java.util.*;

import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.tools.tuples.Pair;
import SudokuExplainer.tools.tuples.Quad;
import SudokuExplainer.units.Grid;

/**
 * Analyze a sudoku grid.
 * <p>
 * This class tries to fully solve the sudoku using logical rules
 * only and then produce a single hint with the rating of
 * the sudoku, and the list of hints that have been used.
 * <p>
 * If the sudoku is not valid, an appropriate warning hint is
 * produced.
 * @see SudokuExplainer.solver.checks.AnalysisInfo
 */
public class Analyser implements WarningHintProducer {

    private final Solver solver;
    private final Asker asker;


    public Analyser(Solver solver, Asker asker) {
        this.solver = solver;
        this.asker = asker;
    }

    public void getHints(Grid grid, HintsAccumulator accu)
    throws InterruptedException {
        Pair<Map<Rule,Integer>, Quad<Double,Double,Double,Integer>> rules = solver.solve(asker);
        Hint hint = new AnalysisInfo(
                this,
                rules.getValue1(),
                solver.toNamedList(rules.getValue1()),
                rules.getValue2());
        accu.add(hint);
    }

    @Override
    public String toString() {
        return "Analysis";
    }

}
