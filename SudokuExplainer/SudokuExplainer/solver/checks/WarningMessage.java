package SudokuExplainer.solver.checks;

import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Grid;

/**
 * A hint that just shows an arbitrary warning or information message
 */
public class WarningMessage extends WarningHint {

    private final String message;
    private final String htmlFile;
    private final Object[] args;

    public WarningMessage(WarningHintProducer rule, String message,
            String htmlFile, Object... args) {
        super(rule);
        this.message = message;
        this.htmlFile = htmlFile;
        this.args = args;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String toHtml() {
        String result = HtmlLoader.loadHtml(this, htmlFile);
        return HtmlLoader.format(result, args);
    }

}
