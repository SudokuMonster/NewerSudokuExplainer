package SudokuExplainer.solver;

/**
 * A "classified hint" that can be used to advance one step in the
 * solving process of a Sudoku.
 * "Pseudo" hints such as warnings, analyses and informations do not
 * implement this interface.
 */
public interface Rule {

    /**
     * Get the name of this rule.
     * <p>
     * This method will return the name of well-known rules such as
     * "naked pair", "X-Wing", etc.
     * @return the name of this rule
     */
    String getName();

    /**
     * Get the difficulty rating of this rule.
     * <p>
     * Currently, the following classification is used:
     * <ul>
     * <li>1.0: Naked Single when only one empty cell in a region
     * <li>1.2, 1.5: Hidden Single (1.5 if not in block)
     * <li>1.7: Direct Pointing
     * <li>1.9: Direct Claiming
     * <li>2.0, 2.5: Direct Hidden Pair, Direct Hidden Triple
     * <li>2.3: Naked Single
     * <li>2.6, 2.8: Pointing, Claiming
     * <li>3.0, 3.2, 3.4: Naked Pair, X-Wing, Hidden Pair
     * <li>3.6, 3.8, 4.0: Naked Triple, Swordfish, Hidden Triple
     * <li>4.2, 4.4: XY-Wing, XYZ-Wing
     * <li>4.4: W-Wing (local)
     * <li>4.5, 4.8: XYZ-Wing Extension, WXYZ-Wing Extension (local)
     * <li>4.6, 5.0: WXYZ-Wing, VWXYZ-Wing (local)
     * <li>4.5 - 5.0: Unique Rectangles and Loops
     * <li>5.0, 5.2, 5.4: Naked Quadruple, Jellyfish, Hidden Quadruple
     * <li>5.6 - 6.0: Bivalue Universal Graves
     * <li>6.2: Aligned Pair Exclusion (will be replaced)
     * <li>6.5 - 7.5: X-Cycles, Y-Cycles
     * <li>6.6 - 7.6: Forcing X-Chains
     * <li>7.0 - 8.0: Forcing Chains, XY-Cycles
     * <li>7.5: Aligned Triple Exclusion (will be replaced)
     * <li>7.5 - 8.5: Nishio Forcing Chains
     * <li>8.3: Aligned Quadruple Exclusion (local, not used)
     * <li>8.0 - 9.0: Multiple Forcing Chains
     * <li>9.0: Aligned Quintuple Exclusion (local, not used)
     * <li>8.5 - 9.5: Dynamic Forcing Chains
     * <li>9.0 - 10.0: Dynamic Forcing Chains (+)
     * <li>&gt; 9.5: Nested Forcing Chains
     * <li>20.0: Trial And Error (only appears in debugging mode)
     * </ul>
     * Upper bound for chains is actually unbounded: the longer chain, the higher rating.
     * @return the difficulty rating of this rule.
     */
    double getDifficulty();

    /**
     * Get a clue, or a "partial hint", as an HTML string.
     * @param isBig <tt>true</tt> to get a big clue, that is, a
     * nearly complete hint; <tt>false</tt> to get a small clue
     * that is, a very partial hint.
     * @return a clue, in HTML
     */
    String getClueHtml(boolean isBig);
}
