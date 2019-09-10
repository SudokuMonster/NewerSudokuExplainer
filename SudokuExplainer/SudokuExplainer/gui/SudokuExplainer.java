package SudokuExplainer.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import SudokuExplainer.units.*;
import SudokuExplainer.Settings;
import SudokuExplainer.io.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;


public class SudokuExplainer {

    private Grid grid;
    private Solver solver;
    private SudokuFrame frame;
    private SudokuPanel panel;

    private List<Hint> unfilteredHints = null;
    private List<Hint> filteredHints = null;
    private boolean isFiltered = true;
    private List<Hint> selectedHints = new ArrayList();

    private Stack<Grid> gridStack = new Stack();

    Set<Cell> givenCells = new HashSet();
    Map<Cell, BitSet> removedPotentials = new HashMap();

    public SudokuExplainer() {
        this.grid = new Grid();
        this.solver = new Solver(this.grid);
        this.solver.rebuildPotentialValues();
        this.frame = new SudokuFrame();
        this.frame.setEngine(this);
        this.panel = this.frame.getSudokuPanel();
        this.panel.setSudokuGrid(this.grid);
        this.panel.setEngine(this);
        repaintHintsTree();
        this.frame.pack();
        Dimension screenSize = frame.getToolkit().getScreenSize();
        Insets insets = frame.getToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        Dimension windowSize = frame.getSize();
        int screenWidth = screenSize.width - insets.left - insets.right;
        int screenHeight = screenSize.height - insets.top - insets.bottom;
        this.frame.setLocation(
                (screenWidth - windowSize.width) / 2 + insets.left,
                (screenHeight - windowSize.height) / 2 + insets.top);
        this.frame.setVisible(true);
    }

    private void resetFilterCache() {
        this.givenCells = new HashSet();
        this.removedPotentials = new HashMap();
    }

    private boolean isWorth(Hint hint) {
        if (!this.isFiltered) {
            return true;
        }
        boolean isWorth;
        if ((hint instanceof DirectHint)) {
            isWorth = isWorth(this.givenCells, (DirectHint)hint);
        } else {
            isWorth = isWorth(this.removedPotentials, this.givenCells, (IndirectHint)hint);
        }
        return isWorth;
    }

    private boolean isWorth(Set<Cell> givenCells, DirectHint hint) {
        return !givenCells.contains(hint.getCell());
    }

    private boolean isWorth(Map<Cell, BitSet> removedPotentials, Set<Cell> givenCells, IndirectHint hint) {
        if ((hint instanceof WarningHint)) {
            return true;
        }
        Map<Cell, BitSet> removablePotentials = hint.getRemovablePotentials();
        for (Cell cell : removablePotentials.keySet())
        {
            BitSet removable = removablePotentials.get(cell);
            BitSet previous = removedPotentials.get(cell);
            if (previous == null) {
                return true;
            }
            BitSet newRemove = (BitSet)removable.clone();
            newRemove.andNot(previous);
            if (!newRemove.isEmpty()) {
                return true;
            }
        }
        Cell cell = hint.getCell();
        return (cell != null) && (!givenCells.contains(cell));
    }

    private void filterHints() {
        this.filteredHints = null;
        if (this.unfilteredHints != null) {
            this.filteredHints = new ArrayList();
            if (this.isFiltered) {
                for (Hint hint : this.unfilteredHints) {
                    if (isWorth(hint)) {
                        addFilteredHintAndUpdateFilter(hint);
                    }
                }
            } else {
                this.filteredHints.addAll(this.unfilteredHints);
            }
        }
    }

    private void addFilteredHintAndUpdateFilter(Hint hint) {
        this.filteredHints.add(hint);
        if ((hint instanceof DirectHint)) {
            DirectHint dHint = (DirectHint)hint;
            this.givenCells.add(dHint.getCell());
        } else {
            IndirectHint indirectHint = (IndirectHint)hint;
            Map<Cell, BitSet> removablePotentials = indirectHint.getRemovablePotentials();
            for (Cell cell : removablePotentials.keySet()) {
                BitSet removable = removablePotentials.get(cell);
                BitSet current = this.removedPotentials.get(cell);
                if (current == null) {
                    current = new BitSet(10);
                    this.removedPotentials.put(cell, current);
                }
                current.or(removable);
            }
            Cell cell = indirectHint.getCell();
            if (cell != null) {
                this.givenCells.add(cell);
            }
        }
    }

    public void setFiltered(boolean value) {
        resetFilterCache();
        this.isFiltered = value;
        filterHints();
        repaintAll();
    }

    public boolean isFiltered() {
        return this.isFiltered;
    }

    private void repaintHints() {
        if (this.selectedHints.size() == 1) {
            this.frame.setCurrentHint(this.selectedHints.get(0), true);
        } else {
            this.frame.setCurrentHint(null, !this.selectedHints.isEmpty());
            if (this.selectedHints.size() > 1) {
                paintMultipleHints(this.selectedHints);
            }
        }
    }

    private void paintMultipleHints(List<Hint> hints) {
        Map<Cell, BitSet> redPotentials = new HashMap();
        Map<Cell, BitSet> greenPotentials = new HashMap();
        for (Hint hint : hints) {
            Cell cell = hint.getCell();
            if (cell != null) {
                greenPotentials.put(cell, SingletonBitSet.create(hint.getValue()));
            }
            if ((hint instanceof IndirectHint)) {
                IndirectHint indirectHint = (IndirectHint)hint;
                Map<Cell, BitSet> removable = indirectHint.getRemovablePotentials();
                for (Cell rCell : removable.keySet()) {
                    BitSet values = removable.get(rCell);
                    if (redPotentials.containsKey(rCell)) {
                        redPotentials.get(rCell).or(values);
                    } else {
                        redPotentials.put(rCell, (BitSet)values.clone());
                    }
                }
            }
        }
        this.panel.setRedPotentials(redPotentials);
        this.panel.setGreenPotentials(greenPotentials);
        this.panel.setBluePotentials(null);
        this.panel.setGreenCells(greenPotentials.keySet());
        this.panel.repaint();
        this.frame.setExplanations(HtmlLoader.loadHtml(this, "Multiple.html"));
    }

    public void cellValueTyped(Cell cell, int value) {
        if (cell.getCellType() != CellType.Given) {
            pushGrid();
            int oldValue = cell.getValue();
            cell.setValue(value);
            if ((value == 0) || (oldValue != 0)) {
                this.solver.rebuildPotentialValues();
            } else {
                this.solver.cancelPotentialValues();
            }
            boolean needRepaintHints = this.filteredHints != null;
            clearHints0();
            this.selectedHints.clear();
            if (needRepaintHints) {
                repaintHintsTree();
                repaintHints();
            }
        }
    }

    public void candidateTyped(Cell cell, int candidate) {
        pushGrid();
        if (cell.hasPotentialValue(candidate)) {
            cell.removePotentialValue(candidate);
        } else {
            cell.addPotentialValue(candidate);
        }
        this.solver.cancelPotentialValues();
    }

    public void hintsSelected(Collection<HintNode> nodes) {
        this.selectedHints.clear();
        for (HintNode node : nodes) {
            Hint hint = node.getHint();
            if (hint != null) {
                this.selectedHints.add(hint);
            }
        }
        repaintHints();
    }

    private void repaintAll() {
        repaintHintsTree();
        repaintHints();
        this.panel.repaint();
    }

    public void clearGrid() {
        this.grid = new Grid();
        this.solver = new Solver(this.grid);
        this.solver.rebuildPotentialValues();
        this.panel.setSudokuGrid(this.grid);
        clearHints();
        this.frame.showWelcomeText();
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
        this.solver = new Solver(grid);
        this.solver.rebuildPotentialValues();
        this.panel.setSudokuGrid(grid);
        this.panel.clearSelection();
        clearHints();
        this.frame.setExplanations("");
    }

    public Grid getGrid() {
        return this.panel.getSudokuGrid();
    }

    public void clearHints() {
        this.unfilteredHints = null;
        resetFilterCache();
        filterHints();
        this.selectedHints.clear();
        this.panel.clearSelection();
        repaintAll();
    }

    public void clearHints0() {
        this.unfilteredHints = null;
        resetFilterCache();
        filterHints();
        this.selectedHints.clear();
        this.panel.clearFocus();
        repaintAll();
    }

    public void rebuildSolver() {
        this.solver = new Solver(this.grid);
    }

    private void displayError(Throwable ex) {
        ex.printStackTrace();
        try {
            repaintAll();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.frame.setExplanations("<html><body><font color=\"red\">" +
                ex.toString().replace("\n", "<br>") +
                "</font></body></html>");
    }

    public boolean checkValidity() {
        this.selectedHints.clear();
        this.unfilteredHints = new ArrayList();
        Hint hint = this.solver.checkValidity();
        if (hint != null) {
            this.unfilteredHints.add(hint);
            this.selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
        return hint == null;
    }

    public void resetPotentials() {
        this.solver.rebuildPotentialValues();
        clearHints();
    }

    private Hint getNextHintImpl() {
        if (this.unfilteredHints == null) {
            this.unfilteredHints = new ArrayList();
            filterHints();
        }
        final List<Hint> buffer = new ArrayList();
        final StrongReference<Hint> newHint = new StrongReference();
        this.solver.gatherHints(this.unfilteredHints, buffer, hint -> {
            if (!buffer.contains(hint)) {
                buffer.add(hint);
                boolean isNew = buffer.size() > SudokuExplainer.this.unfilteredHints.size();
                if (isNew) {
                    SudokuExplainer.this.unfilteredHints.add(hint);
                    if (SudokuExplainer.this.isWorth(hint)) {
                        newHint.setValue(hint);
                        throw new InterruptedException();
                    }
                }
            }
        }, this.frame);
        this.selectedHints.clear();
        Hint hint = null;
        if (newHint.isValueSet()) {
            hint = newHint.getValue();
        }
        return hint;
    }

    public void getNextHint() {
        try {
            Hint hint = getNextHintImpl();
            if (hint != null) {
                addFilteredHintAndUpdateFilter(hint);
                this.selectedHints.add(hint);
            }
            repaintAll();
        } catch (Throwable ex) {
            displayError(ex);
        }
    }

    public void getAllHints() {
        try {
            this.unfilteredHints = this.solver.getAllHints(this.frame);
            this.selectedHints.clear();
            resetFilterCache();
            filterHints();
            if (!this.filteredHints.isEmpty()) {
                this.selectedHints.add(this.filteredHints.get(0));
            }
            repaintAll();
        } catch (Throwable ex) {
            displayError(ex);
        }
    }

    public void applySelectedHints() {
        pushGrid();
        for (Hint hint : this.selectedHints) {
            hint.apply();
        }
        clearHints();
        repaintAll();
    }

    public void undoStep() {
        popGrid();
    }

    public void applySelectedHintsAndContinue() {
        applySelectedHints();
        getNextHint();
    }

    private void repaintHintsTree() {
        if (this.filteredHints == null) {
            List<Hint> noHints = Collections.emptyList();
            HintNode root = new HintsTreeBuilder().buildHintsTree(noHints);
            this.frame.setHintsTree(root, null, false);
        } else {
            HintNode root = new HintsTreeBuilder().buildHintsTree(this.filteredHints);
            HintNode selected = null;
            if ((root != null) && (this.selectedHints.size() == 1)) {
                selected = root.getNodeFor(this.selectedHints.get(0));
            }
            this.frame.setHintsTree(root, selected, this.unfilteredHints.size() > 1);
        }
    }

    public void pasteGrid() {
        Grid copy = new Grid();
        this.grid.copyTo(copy);
        clearGrid();
        ErrorMessage message = SudokuIO.loadFromClipboard(this.grid);
        if ((message == null) || (!message.isFatal())) {
            this.solver.rebuildPotentialValues();
        } else {
            copy.copyTo(this.grid);
        }
        if (message != null) {
            JOptionPane.showMessageDialog(this.frame, message.toString(), "Paste",
                    (message.isFatal() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE));
        }
    }

    public void pushGrid() {
        Grid copy = new Grid();
        this.grid.copyTo(copy);
        this.gridStack.push(copy);
    }

    private void popGrid() {
        if (!this.gridStack.isEmpty()) {
            Grid prev = this.gridStack.pop();
            prev.copyTo(this.grid);
            this.solver.rebuildPotentialValues();
            clearHints();
            repaintAll();
        }
    }

    public void copyGrid() {
        SudokuIO.saveToClipboard(this.grid);
    }

    public void loadGrid(File file) {
        Grid copy = new Grid();
        this.grid.copyTo(copy);
        clearGrid();
        ErrorMessage message = SudokuIO.loadFromFile(this.grid, file);
        if ((message == null) || (!message.isFatal())) {
            this.solver.rebuildPotentialValues();
        } else {
            copy.copyTo(this.grid);
        }
        if (message != null) {
            JOptionPane.showMessageDialog(this.frame, message.toString(), "Paste",
                    (message.isFatal() ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE));
        }
    }

    public void saveGrid(File file) {
        ErrorMessage message = SudokuIO.saveToFile(this.grid, file);
        if (message != null) {
            JOptionPane.showMessageDialog(this.frame, message.toString(), "Paste",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

//    public void saveGridAsPicture() {
//        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
//        Transferable trans = new Transferable() {
//            public DataFlavor[] getTransferDataFlavors() {
//                return new DataFlavor[] { DataFlavor.imageFlavor };
//            }
//
//            public boolean isDataFlavorSupported(DataFlavor flavor) {
//                return DataFlavor.imageFlavor.equals(flavor);
//            }
//
//            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
//                if (isDataFlavorSupported(flavor)) {
//                    try {
//                        if (SudokuExplainer.this.panel != null) {
//                            return new Robot().createScreenCapture(SudokuExplainer.this.panel.getVisibleRect());
//                        }
//                    } catch (AWTException ignored) { }
//                }
//                throw new UnsupportedFlavorException(flavor);
//            }
//        };
//        clip.setContents(trans, null);
//    }

    public void solve() {
        clearHints();
        this.unfilteredHints = new ArrayList();
        Hint hint = this.solver.bruteForceSolve();
        if (hint != null) {
            this.unfilteredHints.add(hint);
            this.selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
    }

    public Hint analyse() {
        try {
            clearHints();
            this.unfilteredHints = new ArrayList();
            Hint hint = this.solver.analyse(this.frame);
            if (hint != null) {
                this.unfilteredHints.add(hint);
                this.selectedHints.add(hint);
            }
            filterHints();
            return hint;
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Throwable ex) {
            displayError(ex);
            return null;
        } finally {
            repaintAll();
        }
    }

    public void showHint(Hint hint) {
        clearHints();
        this.unfilteredHints = new ArrayList();
        if (hint != null) {
            this.unfilteredHints.add(hint);
            this.selectedHints.add(hint);
        }
        filterHints();
        repaintAll();
    }

    public void getClue(boolean isBig) {
        clearHints();
        Hint hint = getNextHintImpl();
        if (hint != null) {
            if ((hint instanceof Rule)) {
                Rule rule = (Rule)hint;
                String clueFile = isBig ? "BigClue.html" : "SmallClue.html";
                String htmlText = HtmlLoader.loadHtml(this, clueFile);
                String clueHtml = rule.getClueHtml(isBig);
                htmlText = htmlText.replace("{0}", clueHtml);
                if (htmlText.contains("<b1>")) {
                    this.panel.setBlueRegions(hint.getRegions());
                }
                htmlText = HtmlLoader.formatColors(htmlText);
                this.frame.setExplanations(htmlText);
                this.unfilteredHints = null;
                resetFilterCache();
                filterHints();
            } else {
                addFilteredHintAndUpdateFilter(hint);
                this.selectedHints.add(hint);
                repaintAll();
            }
        }
    }

    public static void main(String[] args) {
        try {
            String lookAndFeelClassName = Settings.getInstance().getLookAndFeelClassName();
            if (lookAndFeelClassName == null) {
                lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            }
            UIManager.setLookAndFeel(lookAndFeelClassName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SudokuExplainer());
    }

}
