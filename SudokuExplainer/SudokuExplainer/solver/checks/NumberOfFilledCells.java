package SudokuExplainer.solver.checks;

import SudokuExplainer.solver.*;
import SudokuExplainer.units.Grid;

/**
 * Check for invalid sudoku based on the number of clues.
 * If less than 17 clues are given, an appropriate warning hint is produced.
 */
public class NumberOfFilledCells implements WarningHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        int countEmpty = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid.getCell(x, y).getValue() == 0)
                    countEmpty++;
            }
        }
        if (countEmpty == 0) {
            WarningMessage message = new WarningMessage(this, "The sudoku has been solved",
            "SudokuSolved.html");
            accu.add(message);
        } else if (countEmpty > 81 - 17) {
            int given = 81 - countEmpty;
            WarningMessage message = new WarningMessage(this,
                    "Sudoku is not valid",
                    "TooFewCells.html", given);
            accu.add(message);
        }
    }

    @Override
    public String toString() {
        return "Number of clues";
    }

}
