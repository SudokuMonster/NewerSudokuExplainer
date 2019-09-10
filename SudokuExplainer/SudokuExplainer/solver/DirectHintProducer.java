package SudokuExplainer.solver;

/**
 * Interface for rules that are able to produce direct hints.
 * @see SudokuExplainer.solver.DirectHint
 */
public interface DirectHintProducer extends HintProducer {
    String toString();
}
