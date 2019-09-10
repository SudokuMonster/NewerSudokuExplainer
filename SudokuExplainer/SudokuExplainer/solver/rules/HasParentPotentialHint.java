package SudokuExplainer.solver.rules;

import java.util.*;

import SudokuExplainer.solver.rules.chaining.*;
import SudokuExplainer.units.Grid;


/**
 * Interface for indirect hints that are able to tell what
 * {@link SudokuExplainer.solver.rules.chaining.Potential Potential}s
 * have been set to off before this rule could be applied.
 * <p>
 * Used for chaining only. See package {@link SudokuExplainer.solver.rules.chaining}.
 */
public interface HasParentPotentialHint {

    /**
     * Get the potentials that were removed from the initial grid
     * before this rule could be applied.
     * @param initialGrid the initial grid, on which this rule
     * cannot be applied.
     * @param currentGrid the current grid, on which this rule
     * is revealed.
     * @return the potentials that were removed from the initial grid.
     */
    Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid);

}
