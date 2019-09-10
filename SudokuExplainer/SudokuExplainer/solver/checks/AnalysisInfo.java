package SudokuExplainer.solver.checks;

import java.text.*;
import java.util.*;

import SudokuExplainer.tools.tuples.Pair;
import SudokuExplainer.tools.tuples.Quad;
import SudokuExplainer.units.Grid.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;

/**
 * A information hint produced by the {@link SudokuExplainer.solver.checks.Analyser}
 * class. Contains an approximate rating of the sudoku, and the list of hints that
 * have been used to solve it. The actual solution is not shown, and the grid
 * is not modified by applying this hint.
 * @see SudokuExplainer.solver.checks.Analyser
 */
public class AnalysisInfo extends WarningHint {

    private final Map<Rule,Integer> rules;
    private final Map<String,Integer> ruleNames;
    private final Quad<Double,Double,Double,Integer> difficulties;

    public AnalysisInfo(WarningHintProducer rule, Map<Rule,Integer> rules,
            Map<String,Integer> ruleNames,Quad<Double,Double,Double,Integer> difficulties) {
        super(rule);
        this.rules = rules;
        this.ruleNames = ruleNames;
        this.difficulties = difficulties;
    }

    @Override
    public Region[] getRegions() {
        return null;
    }

    @Override
    public String toHtml() {
        final String normalPuzzleType = "Normal Puzzle";
        Pair<Double, Double> difficultyInfoPair = getDifficulty();
        DecimalFormat format = new DecimalFormat("#0.0");
        StringBuilder details = new StringBuilder();
        for (String ruleName : ruleNames.keySet()) {
            int count = ruleNames.get(ruleName);
            details.append(count);
            details.append(" * ");
            details.append(ruleName);
            details.append("<br>\n");
        }
        String result = HtmlLoader.loadHtml(this, "Analysis.html");
        double difficultyValue = difficultyInfoPair.getValue1();
        double totalValue = difficultyInfoPair.getValue2();
        double pearlRating = difficulties.getValue2();
        double diamondRating = difficulties.getValue3();
        String puzzleType;
        int puzzleInt = (int)(difficultyValue * 10);
        int pearlInt = (int)(pearlRating * 10);
        int diamondInt = (int)(diamondRating * 10);
        if (puzzleInt == pearlInt && puzzleInt == diamondInt) {
            puzzleType = "Diamond Puzzle";
        } else if (puzzleInt == pearlInt) {
            puzzleType = "Pearl Puzzle";
        } else {
            puzzleType = normalPuzzleType;
        }

        result = HtmlLoader.format(
                result,
                format.format(difficultyValue),
                format.format(totalValue),
                format.format(pearlRating),
                format.format(diamondRating),
                difficulties.getValue4(),
                getDifficultyType(difficultyValue),
                details,
                puzzleType.equals(normalPuzzleType) ? "" : ("Puzzle Type: " + puzzleType + "<br>"));
        return result;
    }

    public Pair<Double, Double> getDifficulty() {
        double difficulty = 0;
        double total = 0;
        for (Rule rule : rules.keySet()) {
            int times = rules.get(rule);
            double value = rule.getDifficulty();
            total += times * value;
            if (value > difficulty)
                difficulty = value;
        }
        return new Pair<>(difficulty, total);
    }

    public String getDifficultyType(double difficulty) {
        if (difficulty >= 1.0 && difficulty <= 1.2) {
            return "Very Easy";
        } else if (difficulty >= 1.3 && difficulty <= 1.5) {
            return "Easy";
        } else if (difficulty >= 1.6 && difficulty <= 2.3) {
            return "Moderate";
        } else if (difficulty >= 2.4 && difficulty <= 2.8) {
            return "Advanced";
        } else if (difficulty >= 2.9 && difficulty <= 3.4) {
            return "Hard";
        } else if (difficulty >= 3.5 && difficulty <= 4.4) {
            return "Very hard";
        } else if (difficulty >= 4.5 && difficulty <= 6.2) {
            return "Fiendish";
        } else if (difficulty >= 6.3 && difficulty <= 7.6) {
            return "Diabolical";
        } else if (difficulty >= 7.7 && difficulty <= 8.9) {
            return "Crazy";
        } else if (difficulty >= 9.0 && difficulty <= 10.0) {
            return "Nightmare";
        } else if (difficulty >= 10.1 && difficulty <= 12.0) {
            return "Beyond nightmare";
        } else return "Unknown"; // Only for 20.0 (SE cannot solve)
    }

    @Override
    public String toString() {
        return "Sudoku Rating";
    }

}
