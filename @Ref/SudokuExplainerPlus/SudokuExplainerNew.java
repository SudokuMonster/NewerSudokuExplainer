package SudokuExplainer.gui;

import SudokuExplainer.Cell;
import SudokuExplainer.Grid;
import SudokuExplainer.Settings;
import SudokuExplainer.io.ErrorMessage;
import SudokuExplainer.io.SudokuIO;
import SudokuExplainer.solver.DirectHint;
import SudokuExplainer.solver.Hint;
import SudokuExplainer.solver.HintsAccumulator;
import SudokuExplainer.solver.IndirectHint;
import SudokuExplainer.solver.Rule;
import SudokuExplainer.solver.Solver;
import SudokuExplainer.solver.WarningHint;
import SudokuExplainer.tools.HtmlLoader;
import SudokuExplainer.tools.SingletonBitSet;
import SudokuExplainer.tools.StrongReference;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SudokuExplainerNew
{
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
	
	public SudokuExplainerNew()
	{
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
		Dimension screenSize = this.frame.getToolkit().getScreenSize();
		Insets insets = this.frame.getToolkit().getScreenInsets(this.frame.getGraphicsConfiguration());
		Dimension windowSize = this.frame.getSize();
		int screenWidth = screenSize.width - insets.left - insets.right;
		int screenHeight = screenSize.height - insets.top - insets.bottom;
		this.frame.setLocation((screenWidth - windowSize.width) / 2 + insets.left, 
			(screenHeight - windowSize.height) / 2 + insets.top);
		this.frame.setVisible(true);
	}
	
	private void resetFilterCache()
	{
		this.givenCells = new HashSet();
		this.removedPotentials = new HashMap();
	}
	
	private boolean isWorth(Hint hint)
	{
		if (!this.isFiltered) {
			return true;
		}
		boolean isWorth;
		boolean isWorth;
		if ((hint instanceof DirectHint)) {
			isWorth = isWorth(this.givenCells, (DirectHint)hint);
		} else {
			isWorth = isWorth(this.removedPotentials, this.givenCells, (IndirectHint)hint);
		}
		return isWorth;
	}
	
	private boolean isWorth(Set<Cell> givenCells, DirectHint hint)
	{
		return !givenCells.contains(hint.getCell());
	}
	
	private boolean isWorth(Map<Cell, BitSet> removedPotentials, Set<Cell> givenCells, IndirectHint hint)
	{
		if ((hint instanceof WarningHint)) {
			return true;
		}
		Map<Cell, BitSet> removablePotentials = hint.getRemovablePotentials();
		for (Cell cell : removablePotentials.keySet())
		{
			BitSet removable = (BitSet)removablePotentials.get(cell);
			BitSet previous = (BitSet)removedPotentials.get(cell);
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
		if ((cell != null) && (!givenCells.contains(cell))) {
			return true;
		}
		return false;
	}
	
	private void filterHints()
	{
		this.filteredHints = null;
		if (this.unfilteredHints != null)
		{
			this.filteredHints = new ArrayList();
			if (this.isFiltered) {
				for (Hint hint : this.unfilteredHints) {
					if (isWorth(hint)) {
						addFilteredHintAndUpdateFilter(hint);
					}
				}
			} else {
				for (Hint hint : this.unfilteredHints) {
					this.filteredHints.add(hint);
				}
			}
		}
	}
	
	private void addFilteredHintAndUpdateFilter(Hint hint)
	{
		this.filteredHints.add(hint);
		if ((hint instanceof DirectHint))
		{
			DirectHint dHint = (DirectHint)hint;
			this.givenCells.add(dHint.getCell());
		}
		else
		{
			IndirectHint iHint = (IndirectHint)hint;
			Map<Cell, BitSet> removablePotentials = iHint.getRemovablePotentials();
			for (Cell cell : removablePotentials.keySet())
			{
				BitSet removable = (BitSet)removablePotentials.get(cell);
				BitSet current = (BitSet)this.removedPotentials.get(cell);
				if (current == null)
				{
					current = new BitSet(10);
					this.removedPotentials.put(cell, current);
				}
				current.or(removable);
			}
			Cell cell = iHint.getCell();
			if (cell != null) {
				this.givenCells.add(cell);
			}
		}
	}
	
	public void setFiltered(boolean value)
	{
		resetFilterCache();
		this.isFiltered = value;
		filterHints();
		repaintAll();
	}
	
	public boolean isFiltered()
	{
		return this.isFiltered;
	}
	
	private void repaintHints()
	{
		if (this.selectedHints.size() == 1)
		{
			this.frame.setCurrentHint((Hint)this.selectedHints.get(0), true);
		}
		else
		{
			this.frame.setCurrentHint(null, !this.selectedHints.isEmpty());
			if (this.selectedHints.size() > 1) {
				paintMultipleHints(this.selectedHints);
			}
		}
	}
	
	private void paintMultipleHints(List<Hint> hints)
	{
		Map<Cell, BitSet> redPotentials = new HashMap();
		Map<Cell, BitSet> greenPotentials = new HashMap();
		for (Hint hint : hints)
		{
			Cell cell = hint.getCell();
			if (cell != null) {
				greenPotentials.put(cell, SingletonBitSet.create(hint.getValue()));
			}
			if ((hint instanceof IndirectHint))
			{
				IndirectHint ihint = (IndirectHint)hint;
				Map<Cell, BitSet> removable = ihint.getRemovablePotentials();
				for (Cell rCell : removable.keySet())
				{
					BitSet values = (BitSet)removable.get(rCell);
					if (redPotentials.containsKey(rCell)) {
						((BitSet)redPotentials.get(rCell)).or(values);
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
	
	public void cellValueTyped(Cell cell, int value)
	{
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
		if (needRepaintHints)
		{
			repaintHintsTree();
			repaintHints();
		}
	}
	
	public void candidateTyped(Cell cell, int candidate)
	{
		pushGrid();
		if (cell.hasPotentialValue(candidate)) {
			cell.removePotentialValue(candidate);
		} else {
			cell.addPotentialValue(candidate);
		}
		this.solver.cancelPotentialValues();
	}
	
	public void hintsSelected(Collection<HintNode> nodes)
	{
		this.selectedHints.clear();
		for (HintNode node : nodes)
		{
			Hint hint = node.getHint();
			if (hint != null) {
				this.selectedHints.add(hint);
			}
		}
		repaintHints();
	}
	
	private void repaintAll()
	{
		repaintHintsTree();
		repaintHints();
		this.panel.repaint();
	}
	
	public void clearGrid()
	{
		this.grid = new Grid();
		this.solver = new Solver(this.grid);
		this.solver.rebuildPotentialValues();
		this.panel.setSudokuGrid(this.grid);
		clearHints();
		this.frame.showWelcomeText();
	}
	
	public void setGrid(Grid grid)
	{
		this.grid = grid;
		this.solver = new Solver(grid);
		this.solver.rebuildPotentialValues();
		this.panel.setSudokuGrid(grid);
		this.panel.clearSelection();
		clearHints();
		this.frame.setExplanations("");
	}
	
	public Grid getGrid()
	{
		return this.panel.getSudokuGrid();
	}
	
	public void clearHints()
	{
		this.unfilteredHints = null;
		resetFilterCache();
		filterHints();
		this.selectedHints.clear();
		this.panel.clearSelection();
		repaintAll();
	}
	
	public void clearHints0()
	{
		this.unfilteredHints = null;
		resetFilterCache();
		filterHints();
		this.selectedHints.clear();
		this.panel.clearFocus();
		repaintAll();
	}
	
	public void rebuildSolver()
	{
		this.solver = new Solver(this.grid);
	}
	
	private void displayError(Throwable ex)
	{
		ex.printStackTrace();
		try
		{
			repaintAll();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		this.frame.setExplanations("<html><body><font color=\"red\">" + 
			ex.toString().replace("\n", "<br>") + 
			"</font></body></html>");
	}
	
	public boolean checkValidity()
	{
		this.selectedHints.clear();
		this.unfilteredHints = new ArrayList();
		Hint hint = this.solver.checkValidity();
		if (hint != null)
		{
			this.unfilteredHints.add(hint);
			this.selectedHints.add(hint);
		}
		filterHints();
		repaintAll();
		return hint == null;
	}
	
	public void resetPotentials()
	{
		this.solver.rebuildPotentialValues();
		clearHints();
	}
	
	private Hint getNextHintImpl()
	{
		if (this.unfilteredHints == null)
		{
			this.unfilteredHints = new ArrayList();
			filterHints();
		}
		final List<Hint> buffer = new ArrayList();
		final StrongReference<Hint> newHint = new StrongReference();
		this.solver.gatherHints(this.unfilteredHints, buffer, new HintsAccumulator()
		{
			public void add(Hint hint)
				throws InterruptedException
			{
				if (!buffer.contains(hint))
				{
					buffer.add(hint);
					boolean isNew = buffer.size() > SudokuExplainerNew.this.unfilteredHints.size();
					if (isNew)
					{
						SudokuExplainerNew.this.unfilteredHints.add(hint);
						if (SudokuExplainerNew.this.isWorth(hint))
						{
							newHint.setValue(hint);
							throw new InterruptedException();
						}
					}
				}
			}
		}, this.frame);
		this.selectedHints.clear();
		Hint hint = null;
		if (newHint.isValueSet()) {
			hint = (Hint)newHint.getValue();
		}
		return hint;
	}
	
	public void getNextHint()
	{
		try
		{
			Hint hint = getNextHintImpl();
			if (hint != null)
			{
				addFilteredHintAndUpdateFilter(hint);
				this.selectedHints.add(hint);
			}
			repaintAll();
		}
		catch (Throwable ex)
		{
			displayError(ex);
		}
	}
	
	public void getAllHints()
	{
		try
		{
			this.unfilteredHints = this.solver.getAllHints(this.frame);
			this.selectedHints.clear();
			resetFilterCache();
			filterHints();
			if (!this.filteredHints.isEmpty()) {
				this.selectedHints.add((Hint)this.filteredHints.get(0));
			}
			repaintAll();
		}
		catch (Throwable ex)
		{
			displayError(ex);
		}
	}
	
	public void applySelectedHints()
	{
		pushGrid();
		for (Hint hint : this.selectedHints) {
			hint.apply();
		}
		clearHints();
		repaintAll();
	}
	
	public void undoStep()
	{
		popGrid();
	}
	
	public void applySelectedHintsAndContinue()
	{
		applySelectedHints();
		getNextHint();
	}
	
	private void repaintHintsTree()
	{
		if (this.filteredHints == null)
		{
			List<Hint> noHints = Collections.emptyList();
			HintNode root = new HintsTreeBuilder().buildHintsTree(noHints);
			this.frame.setHintsTree(root, null, false);
		}
		else
		{
			HintNode root = new HintsTreeBuilder().buildHintsTree(this.filteredHints);
			HintNode selected = null;
			if ((root != null) && (this.selectedHints.size() == 1)) {
				selected = root.getNodeFor((Hint)this.selectedHints.get(0));
			}
			this.frame.setHintsTree(root, selected, this.unfilteredHints.size() > 1);
		}
	}
	
	public void pasteGrid()
	{
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
				message.isFatal() ? 0 : 2);
		}
	}
	
	public void pushGrid()
	{
		Grid copy = new Grid();
		this.grid.copyTo(copy);
		this.gridStack.push(copy);
	}
	
	private void popGrid()
	{
		if (!this.gridStack.isEmpty())
		{
			Grid prev = (Grid)this.gridStack.pop();
			prev.copyTo(this.grid);
			this.solver.rebuildPotentialValues();
			clearHints();
			repaintAll();
		}
	}
	
	public void copyGrid()
	{
		SudokuIO.saveToClipboard(this.grid);
	}
	
	public void loadGrid(File file)
	{
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
				message.isFatal() ? 0 : 2);
		}
	}
	
	public void saveGrid(File file)
	{
		ErrorMessage message = SudokuIO.saveToFile(this.grid, file);
		if (message != null) {
			JOptionPane.showMessageDialog(this.frame, message.toString(), "Paste", 
				0);
		}
	}
	
	public void solve()
	{
		clearHints();
		this.unfilteredHints = new ArrayList();
		Hint hint = this.solver.bruteForceSolve();
		if (hint != null)
		{
			this.unfilteredHints.add(hint);
			this.selectedHints.add(hint);
		}
		filterHints();
		repaintAll();
	}
	
	public Hint analyse()
	{
		try
		{
			clearHints();
			this.unfilteredHints = new ArrayList();
			Hint hint = this.solver.analyse(this.frame);
			if (hint != null)
			{
				this.unfilteredHints.add(hint);
				this.selectedHints.add(hint);
			}
			filterHints();
			return hint;
		}
		catch (UnsupportedOperationException ex)
		{
			throw ex;
		}
		catch (Throwable ex)
		{
			displayError(ex);
			return null;
		}
		finally
		{
			repaintAll();
		}
	}
	
	public void showHint(Hint hint)
	{
		clearHints();
		this.unfilteredHints = new ArrayList();
		if (hint != null)
		{
			this.unfilteredHints.add(hint);
			this.selectedHints.add(hint);
		}
		filterHints();
		repaintAll();
	}
	
	public void getClue(boolean isBig)
	{
		clearHints();
		Hint hint = getNextHintImpl();
		if (hint != null) {
			if ((hint instanceof Rule))
			{
				Rule rule = (Rule)hint;
				String clueFile = isBig ? "BigClue.html" : "SmallClue.html";
				String htmlText = HtmlLoader.loadHtml(this, clueFile);
				String clueHtml = rule.getClueHtml(isBig);
				htmlText = htmlText.replace("{0}", clueHtml);
				if (htmlText.indexOf("<b1>") >= 0) {
					this.panel.setBlueRegions(hint.getRegions());
				}
				htmlText = HtmlLoader.formatColors(htmlText);
				this.frame.setExplanations(htmlText);
				this.unfilteredHints = null;
				resetFilterCache();
				filterHints();
			}
			else
			{
				addFilteredHintAndUpdateFilter(hint);
				this.selectedHints.add(hint);
				repaintAll();
			}
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			String lookAndFeelClassName = Settings.getInstance().getLookAndFeelClassName();
			if (lookAndFeelClassName == null) {
				lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
			}
			UIManager.setLookAndFeel(lookAndFeelClassName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				new SudokuExplainerNew();
			}
		});
	}
}
