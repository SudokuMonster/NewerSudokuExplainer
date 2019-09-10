package SudokuExplainer.applet;

import java.applet.*;
import javax.swing.*;
import SudokuExplainer.gui.*;

/**
 * Minimal applet support for the sudoku explainer.
 */
public class SudokuApplet extends Applet {

    private static final long serialVersionUID = -1770658360372460892L;

    @Override
    public void init() {
        super.init();
        SwingUtilities.invokeLater(() -> new Thread(() -> {
            try {
                // It seems that IE want to get the focus just after the applet
                // has started, which result in bringing our main window to the
                // back. This small delay is a hack that solves this problem.
                Thread.sleep(500);
            } catch (InterruptedException ignored) { }
            SudokuExplainer.main(null);
        }).start());
    }
}
