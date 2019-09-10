package SudokuExplainer.solver;

/**
 * Interface for rules that are able to produce
 * indirect hints.
 * @see SudokuExplainer.solver.IndirectHint
 */
public interface IndirectHintProducer extends HintProducer {
    String toString();
}
