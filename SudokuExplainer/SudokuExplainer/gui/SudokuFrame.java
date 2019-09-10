package SudokuExplainer.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;
import SudokuExplainer.*;
import SudokuExplainer.units.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.solver.checks.*;
import SudokuExplainer.tools.*;
import static SudokuExplainer.Settings.*;

/**
 * The main window of the application.
 * All the action are redirected to the {@link SudokuExplainer} class.
 */
@SuppressWarnings("CatchMayIgnoreException")
public class SudokuFrame extends JFrame implements Asker {

    private static final long serialVersionUID = 8247189707924329043L;
    private SudokuExplainer engine;
    private Hint currentHint = null;
    private int viewCount = 1;
    private int viewNum = 0;
    private GenerateDialog generateDialog = null;
    private TechniquesSelectDialog selectDialog = null;
    private JFrame dummyFrameKnife = null;
    private JPanel jContentPane = null;
    private SudokuPanel sudokuPanel = null;
    private JScrollPane hintDetailsPane = null;
    private JTree hintsTree = null;
    private JEditorPane hintDetailArea = null;
    private JPanel jPanel = null;
    private JPanel sudokuContainer = null;
    private JPanel hintDetailContainer = null;
    private JPanel buttonsPane = null;
    private JButton btnGetAllHints = null;
    private JButton btnApplyHintAndGet = null;
    private JButton btnUndoStep = null;
    private JPanel buttonsContainer = null;
    private JScrollPane hintsTreeScrollpane = null;
    private JButton btnGetNextHint = null;
    private JPanel viewSelectionPanel = null;
    private JPanel hintsTreePanel = null;
    private JCheckBox chkFilter = null;
    private JButton btnCheckValidity = null;
    private JButton btnApplyHint = null;
    private JComboBox cmbViewSelector = null;
    private JPanel hintsSouthPanel = null;
    private JPanel ratingPanel = null;
    private JLabel jLabel = null;
    private JLabel lblRating = null;
    private JLabel jLabel2 = null;
    private JMenuBar jJMenuBar = null;
    private JMenu fileMenu = null;
    private JMenuItem mitNew = null;
    private JMenuItem mitQuit = null;
    private JMenuItem mitLoad = null;
    private JMenuItem mitSave = null;
    private JMenu editMenu = null;
    private JMenuItem mitCopy = null;
    private JMenuItem mitClear = null;
    private JMenuItem mitPaste = null;
    private JMenu toolMenu = null;
    private JMenuItem mitCheckValidity = null;
    private JMenuItem mitAnalyse = null;
    private JMenuItem mitUndoStep = null;
    private JMenuItem mitSolveStep = null;
    private JMenuItem mitGetNextHint = null;
    private JMenuItem mitApplyHint = null;
    private JMenuItem mitGetAllHints = null;
    private JMenuItem mitSolve = null;
    private JMenuItem mitResetPotentials = null;
    private JMenuItem mitClearHints = null;
    private File defaultDirectory = null;
    private JRadioButton rdbView1 = null;
    private JRadioButton rdbView2 = null;
    private JMenu optionsMenu = null;
    private JCheckBoxMenuItem mitFilter = null;
    private JRadioButtonMenuItem mitMathMode = null;
    private JRadioButtonMenuItem mitChessMode = null;
    private JCheckBoxMenuItem mitAntiAliasing = null;
    private JMenu helpMenu = null;
    private JMenuItem mitAbout = null;
    private JMenuItem mitGetSmallClue = null;
    private JMenuItem mitGetBigClue = null;
    private JMenu mitLookAndFeel = null;
    private JMenuItem mitShowWelcome = null;
    private JMenuItem mitGenerate = null;
    private JCheckBoxMenuItem mitShowCandidates = null;
    private JMenuItem mitSelectTechniques = null;
    private JPanel pnlEnabledTechniques = null;
    private JLabel lblEnabledTechniques = null;
    private JMenuItem mitSaveGridPicture = null;

    public SudokuFrame() {
        initialize();
        repaintViews();
        AutoBusy.addFullAutoBusy(this);
        showWelcomeText();
        ImageIcon icon = createImageIcon("Sudoku.gif");
        assert icon != null;
        setIconImage(icon.getImage());
        SwingUtilities.invokeLater(() -> SudokuFrame.this.getSudokuPanel().requestFocusInWindow());
    }

    public void showWelcomeText() {
        String welcomeText = HtmlLoader.loadHtml(this, "Welcome.html");
        setExplanations(welcomeText);
    }

    void setEngine(SudokuExplainer explainer) {
        this.engine = explainer;
    }

    public void setHintsTree(HintNode root, HintNode selected, boolean isFilterEnabled) {
        getHintsTree().setEnabled(false);
        DefaultTreeModel model = new DefaultTreeModel(root);
        getHintsTree().setModel(model);
        if (root != null) {
            for (int i = 0; i < root.getChildCount(); i++) {
                HintNode child = (HintNode)root.getChildAt(i);
                getHintsTree().expandPath(new TreePath(child.getPath()));
            }
        }
        this.chkFilter.setSelected(this.engine.isFiltered());
        this.chkFilter.setEnabled(isFilterEnabled);
        this.mitFilter.setSelected(this.chkFilter.isSelected());
        this.mitFilter.setEnabled(this.chkFilter.isEnabled());
        if (selected != null) {
            getHintsTree().setSelectionPath(new TreePath(selected.getPath()));
        }
        getHintsTree().setEnabled(true);
    }

    private void repaintHint() {
        Set<Cell> noCells = Collections.emptySet();
        Map<Cell, BitSet> noMap = Collections.emptyMap();
        this.sudokuPanel.setRedCells(noCells);
        this.sudokuPanel.setGreenCells(noCells);
        this.sudokuPanel.setRedPotentials(noMap);
        this.sudokuPanel.setGreenPotentials(noMap);
        if (this.currentHint != null) {
            this.sudokuPanel.clearSelection();
            if ((this.currentHint instanceof DirectHint)) {
                DirectHint dHint = (DirectHint)this.currentHint;
                this.sudokuPanel.setGreenCells(Collections.singleton(dHint.getCell()));
                BitSet values = new BitSet(10);
                values.set(dHint.getValue());
                this.sudokuPanel.setGreenPotentials(Collections.singletonMap(
                        dHint.getCell(), values));
                getSudokuPanel().setLinks(null);
            } else if ((this.currentHint instanceof IndirectHint)) {
                IndirectHint indirectHint = (IndirectHint)this.currentHint;
                this.sudokuPanel.setGreenPotentials(indirectHint.getGreenPotentials(this.viewNum));
                this.sudokuPanel.setRedPotentials(indirectHint.getRedPotentials(this.viewNum));
                this.sudokuPanel.setBluePotentials(
                        indirectHint.getBluePotentials(this.sudokuPanel.getSudokuGrid(), this.viewNum));
                if (indirectHint.getSelectedCells() != null) {
                    this.sudokuPanel.setGreenCells(Arrays.asList(indirectHint.getSelectedCells()));
                }
                if ((indirectHint instanceof WarningHint)) {
                    this.sudokuPanel.setRedCells(((WarningHint)indirectHint).getRedCells());
                }
                getSudokuPanel().setLinks(indirectHint.getLinks(this.viewNum));
            }
            getSudokuPanel().setBlueRegions(this.currentHint.getRegions());
        }
        this.sudokuPanel.repaint();
    }

    public void setCurrentHint(Hint hint, boolean isApplyEnabled) {
        this.currentHint = hint;
        this.btnApplyHint.setEnabled(isApplyEnabled);
        this.mitApplyHint.setEnabled(isApplyEnabled);
        if (hint != null) {
            if ((hint instanceof IndirectHint)) {
                this.viewCount = ((IndirectHint)hint).getViewCount();
                if (this.viewNum >= this.viewCount) {
                    this.viewNum = 0;
                }
            } else {
                this.viewNum = 0;
                this.viewCount = 1;
            }
            repaintViews();

            setExplanations(hint.toHtml());
            if ((hint instanceof Rule)) {
                Rule rule = (Rule)hint;
                DecimalFormat format = new DecimalFormat("#0.0");
                this.lblRating.setText(format.format(rule.getDifficulty()));
            } else if ((hint instanceof AnalysisInfo)) {
                AnalysisInfo info = (AnalysisInfo)hint;
                DecimalFormat format = new DecimalFormat("#0.0");
                this.lblRating.setText(format.format(info.getDifficulty().getValue1()));
            }
        } else {
            getHintDetailArea().setText(null);
            getSudokuPanel().setBlueRegions();
            getSudokuPanel().setLinks(null);
            this.viewCount = 1;
            this.viewNum = 0;
            repaintViews();
        }
        repaintHint();
        repaint();
    }

    private ImageIcon createImageIcon(String path) {
        URL imgURL = SudokuFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        System.err.println("Couldn't find file: " + path);
        return null;
    }

    private String makeItem(int viewNum) {
        return "View " + (viewNum + 1);
    }

    private void repaintViews() {
        this.cmbViewSelector.setEnabled(false);
        this.cmbViewSelector.removeAllItems();
        for (int i = 0; i < this.viewCount; i++) {
            this.cmbViewSelector.addItem(makeItem(i));
        }
        this.cmbViewSelector.setSelectedIndex(this.viewNum);
        this.cmbViewSelector.setEnabled(this.viewCount >= 3);
        this.cmbViewSelector.setVisible(this.viewCount >= 3);
        this.rdbView1.setVisible(this.viewCount < 3);
        this.rdbView2.setVisible(this.viewCount < 3);
        this.rdbView1.setEnabled(this.viewCount > 1);
        this.rdbView2.setEnabled(this.viewCount > 1);
        if (this.viewNum == 0) {
            this.rdbView1.setSelected(true);
        } else {
            this.rdbView2.setSelected(true);
        }
    }

    public void setExplanations(String htmlText) {
        getHintDetailArea().setText(htmlText);
        getHintDetailArea().setCaretPosition(0);
        this.lblRating.setText("-");
    }

    public void refreshSolvingTechniques() {
        EnumSet<SolvingTechnique> all = EnumSet.allOf(SolvingTechnique.class);
        EnumSet<SolvingTechnique> enabled = Settings.getInstance().getTechniques();
        int disabled = all.size() - enabled.size();
        String message;
        if (disabled == 1) {
            message = "1 solving technique is disabled";
        } else {
            message = disabled + " solving techniques are disabled";
        }
        this.lblEnabledTechniques.setText(message);
        this.pnlEnabledTechniques.setVisible(!Settings.getInstance().isUsingAllTechniques());
    }

    public boolean ask(String message) {
        return JOptionPane.showConfirmDialog(this, message, getTitle(),
                JOptionPane.YES_NO_OPTION) == 0;
    }

    private class HintsTreeCellRenderer implements TreeCellRenderer {
        private final DefaultTreeCellRenderer target = new DefaultTreeCellRenderer();

        public HintsTreeCellRenderer() {
            ImageIcon icon = SudokuFrame.this.createImageIcon("Light.gif");
            this.target.setLeafIcon(icon);
        }

        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (!(value instanceof HintNode)) {
                return this.target.getTreeCellRendererComponent(tree, value, selected,
                        expanded, leaf, row, hasFocus);
            }
            HintNode node = (HintNode)value;
            boolean isEmptyParent = (!node.isHintNode()) && (node.getChildCount() == 0);
            return this.target.getTreeCellRendererComponent(tree, value, selected,
                    (expanded) || (isEmptyParent), (leaf) && (!isEmptyParent), row, hasFocus);
        }
    }

    private void initialize() {
        this.setTitle("Sudoku Explainer " + VERSION + "." + REVISION + SUB_REV);
        JMenuBar menuBar = getJJMenuBar();
        setupLookAndFeelMenu();
        setJMenuBar(menuBar);
        setContentPane(getJContentPane());
        try {
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } catch (SecurityException localSecurityException) { }
        getSudokuPanel().requestFocusInWindow();
    }

    private void setupLookAndFeelMenu() {
        String lookAndFeelName = Settings.getInstance().getLookAndFeelClassName();
        if (lookAndFeelName == null) {
            lookAndFeelName = UIManager.getSystemLookAndFeelClassName();
        }
        ButtonGroup group = new ButtonGroup();
        UIManager.LookAndFeelInfo[] arrayOfLookAndFeelInfo;
        int j = (arrayOfLookAndFeelInfo = UIManager.getInstalledLookAndFeels()).length;
        for (int i = 0; i < j; i++) {
            UIManager.LookAndFeelInfo laf = arrayOfLookAndFeelInfo[i];
            final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(laf.getName());
            menuItem.setName(laf.getClassName());
            try {
                Class<?> lafClass = Class.forName(laf.getClassName());
                LookAndFeel instance = (LookAndFeel)lafClass.newInstance();
                menuItem.setToolTipText(instance.getDescription());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            group.add(menuItem);
            getMitLookAndFeel().add(menuItem);
            if (laf.getClassName().equals(lookAndFeelName)) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(e -> {
                if (menuItem.isSelected()) {
                    String lafClassName = menuItem.getName();
                    try {
                        UIManager.setLookAndFeel(lafClassName);
                        Settings.getInstance().setLookAndFeelClassName(lafClassName);
                        SwingUtilities.updateComponentTreeUI(SudokuFrame.this);

                        SudokuFrame.this.hintsTree.setCellRenderer(new HintsTreeCellRenderer());
                        SudokuFrame.this.repaint();
                        if ((SudokuFrame.this.generateDialog != null) && (SudokuFrame.this.generateDialog.isVisible())) {
                            SwingUtilities.updateComponentTreeUI(SudokuFrame.this.generateDialog);
                            SudokuFrame.this.generateDialog.pack();
                            SudokuFrame.this.generateDialog.repaint();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new BorderLayout());
            this.jContentPane.add(getJPanel(), "North");
            this.jContentPane.add(getHintDetailContainer(), "Center");
            this.jContentPane.add(getButtonsContainer(), "South");
        }
        return this.jContentPane;
    }

    public SudokuPanel getSudokuPanel() {
        if (this.sudokuPanel == null) {
            this.sudokuPanel = new SudokuPanel(this);
        }
        return this.sudokuPanel;
    }

    private JScrollPane getHintsDetailScrollPane() {
        if (this.hintDetailsPane == null) {
            this.hintDetailsPane = new JScrollPane();
            if (getToolkit().getScreenSize().height < 800) {
                this.hintDetailsPane.setPreferredSize(new Dimension(700, 110));
            } else {
                this.hintDetailsPane.setPreferredSize(new Dimension(800, 200));
            }
            this.hintDetailsPane.setViewportView(getHintDetailArea());
        }
        return this.hintDetailsPane;
    }

    private JTree getHintsTree() {
        if (this.hintsTree == null) {
            this.hintsTree = new JTree();
            this.hintsTree.setShowsRootHandles(true);
            this.hintsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            this.hintsTree.setCellRenderer(new HintsTreeCellRenderer());
            this.hintsTree.setExpandsSelectedPaths(true);
            this.hintsTree.addTreeSelectionListener(e -> {
                if (SudokuFrame.this.hintsTree.isEnabled()) {
                    Collection<HintNode> selection = new ArrayList();
                    TreePath[] paths = SudokuFrame.this.hintsTree.getSelectionPaths();
                    if (paths != null) {
                        TreePath[] arrayOfTreePath1;
                        int j = (arrayOfTreePath1 = paths).length;
                        for (int i = 0; i < j; i++) {
                            TreePath path = arrayOfTreePath1[i];
                            selection.add((HintNode)path.getLastPathComponent());
                        }
                    }
                    SudokuFrame.this.engine.hintsSelected(selection);
                }
            });
        }
        return this.hintsTree;
    }

    private JEditorPane getHintDetailArea() {
        if (this.hintDetailArea == null) {
            this.hintDetailArea = new JEditorPane("text/html", null) {
                private static final long serialVersionUID = -5658720148768663350L;

                public void paint(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    super.paint(g);
                }
            };
            this.hintDetailArea.setEditable(false);
        }
        return this.hintDetailArea;
    }

    private JScrollPane getHintsTreeScrollPane() {
        if (this.hintsTreeScrollpane == null) {
            this.hintsTreeScrollpane = new JScrollPane();
            this.hintsTreeScrollpane.setPreferredSize(new Dimension(100, 100));
            this.hintsTreeScrollpane.setViewportView(getHintsTree());
        }
        return this.hintsTreeScrollpane;
    }

    private JPanel getJPanel() {
        if (this.jPanel == null) {
            this.jPanel = new JPanel();
            this.jPanel.setLayout(new BorderLayout());
            this.jPanel.add(getSudokuContainer(), "West");
            this.jPanel.add(getHintsTreePanel(), "Center");
        }
        return this.jPanel;
    }

    private JPanel getSudokuContainer() {
        if (this.sudokuContainer == null) {
            this.sudokuContainer = new JPanel();
            this.sudokuContainer.setLayout(new BorderLayout());
            this.sudokuContainer.setBorder(
                    BorderFactory.createTitledBorder(null, "Sudoku Grid",
                            0,
                            0,
                            new Font("Dialog", Font.BOLD, 12),
                            new Color(51, 51, 51)));
            this.sudokuContainer.add(getSudokuPanel(), "Center");
            this.sudokuContainer.add(getViewSelectionPanel(), "South");
        }
        return this.sudokuContainer;
    }

    private JPanel getHintDetailContainer() {
        if (this.hintDetailContainer == null) {
            this.hintDetailContainer = new JPanel();
            this.hintDetailContainer.setLayout(new BorderLayout());
            this.hintDetailContainer.setBorder(
                    BorderFactory.createTitledBorder(null, "Explanations",
                            0,
                            0,
                            new Font("Dialog", Font.BOLD, 12),
                            new Color(51, 51, 51)));
            this.hintDetailContainer.add(getHintsDetailScrollPane(), "Center");
        }
        return this.hintDetailContainer;
    }

    private JPanel getButtonsPane() {
        if (this.buttonsPane == null) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 4;
            gridBagConstraints21.weightx = 1.0D;
            gridBagConstraints21.gridy = 0;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 2;
            gridBagConstraints11.weightx = 1.0D;
            gridBagConstraints11.gridy = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.weightx = 1.0D;
            gridBagConstraints.gridy = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridy = 0;
            gridBagConstraints3.weightx = 1.0D;
            gridBagConstraints3.gridx = 6;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 5;
            gridBagConstraints2.weightx = 1.0D;
            gridBagConstraints2.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.gridy = 0;
            this.buttonsPane = new JPanel();
            this.buttonsPane.setLayout(new GridBagLayout());
            this.buttonsPane.setBorder(BorderFactory.createTitledBorder(null,
                    "Actions", 0,
                    0, new Font(
                            "Dialog", Font.BOLD, 12), null));
            this.buttonsPane.add(getBtnGetNextHint(), gridBagConstraints11);
            this.buttonsPane.add(getBtnApplyHintAndGet(), gridBagConstraints1);
            this.buttonsPane.add(getBtnGetAllHints(), gridBagConstraints2);
            this.buttonsPane.add(getBtnUndoStep(), gridBagConstraints3);
            this.buttonsPane.add(getBtnApplyHint(), gridBagConstraints21);
            this.buttonsPane.add(getBtnCheckValidity(), gridBagConstraints);
        }
        return this.buttonsPane;
    }

    private JButton getBtnGetNextHint() {
        if (this.btnGetNextHint == null) {
            this.btnGetNextHint = new JButton();
            this.btnGetNextHint.setText("Get next hint");
            this.btnGetNextHint.setToolTipText("Get another, different hint");
            this.btnGetNextHint.setMnemonic(78);
            this.btnGetNextHint.addActionListener(e -> SudokuFrame.this.engine.getNextHint());
        }
        return this.btnGetNextHint;
    }

    private JButton getBtnGetAllHints() {
        if (this.btnGetAllHints == null) {
            this.btnGetAllHints = new JButton();
            this.btnGetAllHints.setText("Get all hints");
            this.btnGetAllHints.setToolTipText("Get all hints applicable on the current situation");
            this.btnGetAllHints.setMnemonic(65);
            this.btnGetAllHints.addActionListener(e -> SudokuFrame.this.engine.getAllHints());
        }
        return this.btnGetAllHints;
    }

    JButton getBtnApplyHintAndGet() {
        if (this.btnApplyHintAndGet == null) {
            this.btnApplyHintAndGet = new JButton();
            this.btnApplyHintAndGet.setText("Solve step");
            this.btnApplyHintAndGet.setMnemonic(83);
            this.btnApplyHintAndGet.setToolTipText("Apply the current hint (if any is shown), and get an hint for the next step");
            this.btnApplyHintAndGet.setFont(new Font("Dialog", Font.BOLD, 12));
            this.btnApplyHintAndGet.addActionListener(e -> SudokuFrame.this.engine.applySelectedHintsAndContinue());
        }
        return this.btnApplyHintAndGet;
    }

    private JPanel getButtonsContainer() {
        if (this.buttonsContainer == null) {
            this.buttonsContainer = new JPanel();
            this.buttonsContainer.setLayout(new GridLayout(1, 1));
            this.buttonsContainer.add(getButtonsPane(), null);
        }
        return this.buttonsContainer;
    }

    private JPanel getViewSelectionPanel() {
        if (this.viewSelectionPanel == null) {
            this.viewSelectionPanel = new JPanel();
            this.viewSelectionPanel.setLayout(new FlowLayout());
            this.viewSelectionPanel.add(getRdbView1(), null);
            this.viewSelectionPanel.add(getCmbViewSelector(), null);
            this.viewSelectionPanel.add(getRdbView2(), null);
            ButtonGroup group = new ButtonGroup();
            group.add(getRdbView1());
            group.add(getRdbView2());
        }
        return this.viewSelectionPanel;
    }

    private JPanel getHintsTreePanel() {
        if (this.hintsTreePanel == null) {
            this.hintsTreePanel = new JPanel();
            this.hintsTreePanel.setLayout(new BorderLayout());
            this.hintsTreePanel.setBorder(BorderFactory.createTitledBorder(
                    null, "Hints classification",
                    0,
                    0, new Font(
                            "Dialog", Font.BOLD, 12), null));
            this.hintsTreePanel.add(getHintsTreeScrollPane(), "Center");
            this.hintsTreePanel.add(getHintsSouthPanel(), "South");
        }
        return this.hintsTreePanel;
    }

    private JCheckBox getChkFilter() {
        if (this.chkFilter == null) {
            this.chkFilter = new JCheckBox();
            this.chkFilter.setText("Filter hints with similar outcome");
            this.chkFilter.setMnemonic(73);
            this.chkFilter.setSelected(true);
            this.chkFilter.setEnabled(false);
            this.chkFilter.addItemListener(e -> SudokuFrame.this.engine.setFiltered(SudokuFrame.this.chkFilter.isSelected()));
        }
        return this.chkFilter;
    }

    private JButton getBtnCheckValidity() {
        if (this.btnCheckValidity == null) {
            this.btnCheckValidity = new JButton();
            this.btnCheckValidity.setText("Check validity");
            this.btnCheckValidity.setToolTipText("Verify the validity of the entered Sudoku");
            this.btnCheckValidity.setMnemonic(86);
            this.btnCheckValidity.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (SudokuFrame.this.engine.checkValidity()) {
                        SudokuFrame.this.setExplanations(HtmlLoader.loadHtml(this, "Valid.html"));
                    }
                }
            });
        }
        return this.btnCheckValidity;
    }

    private JButton getBtnUndoStep() {
        if (this.btnUndoStep == null) {
            this.btnUndoStep = new JButton();
            this.btnUndoStep.setText("Undo step");
            this.btnUndoStep.setToolTipText("Undo previous solve step or value selection");
            this.btnUndoStep.setMnemonic(85);
            this.btnUndoStep.addActionListener(e -> SudokuFrame.this.engine.undoStep());
        }
        return this.btnUndoStep;
    }

    private JButton getBtnApplyHint() {
        if (this.btnApplyHint == null) {
            this.btnApplyHint = new JButton();
            this.btnApplyHint.setText("Apply hint");
            this.btnApplyHint.setMnemonic(80);
            this.btnApplyHint.setToolTipText("Apply the selected hint(s)");
            this.btnApplyHint.setEnabled(false);
            this.btnApplyHint.addActionListener(e -> SudokuFrame.this.engine.applySelectedHints());
        }
        return this.btnApplyHint;
    }

    private JComboBox getCmbViewSelector() {
        if (this.cmbViewSelector == null) {
            this.cmbViewSelector = new JComboBox();
            this.cmbViewSelector.setToolTipText("Toggle view (only for chaining hints)");
            this.cmbViewSelector.addItemListener(e -> {
                if (SudokuFrame.this.cmbViewSelector.isEnabled()) {
                    SudokuFrame.this.viewNum = SudokuFrame.this.cmbViewSelector.getSelectedIndex();
                    SudokuFrame.this.repaintHint();
                }
            });
        }
        return this.cmbViewSelector;
    }

    private JRadioButton getRdbView1() {
        if (this.rdbView1 == null) {
            this.rdbView1 = new JRadioButton();
            this.rdbView1.setText("View 1");
            this.rdbView1.setMnemonic(49);
            this.rdbView1.setToolTipText(getCmbViewSelector().getToolTipText());
            this.rdbView1.addItemListener(e -> {
                if (SudokuFrame.this.rdbView1.isSelected()) {
                    SudokuFrame.this.viewNum = 0;
                    SudokuFrame.this.repaintHint();
                }
            });
        }
        return this.rdbView1;
    }

    private JRadioButton getRdbView2() {
        if (this.rdbView2 == null) {
            this.rdbView2 = new JRadioButton();
            this.rdbView2.setText("View 2");
            this.rdbView2.setMnemonic(50);
            this.rdbView2.setToolTipText(getCmbViewSelector().getToolTipText());
            this.rdbView2.addItemListener(e -> {
                if (SudokuFrame.this.rdbView2.isSelected()) {
                    SudokuFrame.this.viewNum = 1;
                    SudokuFrame.this.repaintHint();
                }
            });
        }
        return this.rdbView2;
    }

    private JPanel getHintsSouthPanel() {
        if (this.hintsSouthPanel == null) {
            this.hintsSouthPanel = new JPanel();
            this.hintsSouthPanel.setLayout(new BorderLayout());
            this.hintsSouthPanel.add(getPnlEnabledTechniques(), "North");
            this.hintsSouthPanel.add(getChkFilter(), "Center");
            this.hintsSouthPanel.add(getRatingPanel(), "South");
        }
        return this.hintsSouthPanel;
    }

    private JPanel getRatingPanel() {
        if (this.ratingPanel == null) {
            this.ratingPanel = new JPanel();
            this.jLabel2 = new JLabel();

            this.lblRating = new JLabel();
            this.lblRating.setText("0");
            this.jLabel = new JLabel();
            this.jLabel.setText("Hint difficulty rating: ");
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            this.ratingPanel.setLayout(flowLayout);
            this.ratingPanel.add(this.jLabel, null);
            this.ratingPanel.add(this.lblRating, null);
            this.ratingPanel.add(this.jLabel2, null);
        }
        return this.ratingPanel;
    }

    private JMenuBar getJJMenuBar() {
        if (this.jJMenuBar == null) {
            this.jJMenuBar = new JMenuBar();
            this.jJMenuBar.add(getFileMenu());
            this.jJMenuBar.add(getEditMenu());
            this.jJMenuBar.add(getToolMenu());
            this.jJMenuBar.add(getOptionsMenu());
            this.jJMenuBar.add(getHelpMenu());
        }
        return this.jJMenuBar;
    }

    private void setCommand(JMenuItem item, char cmd) {
        item.setAccelerator(KeyStroke.getKeyStroke(cmd, InputEvent.CTRL_MASK));
    }

    private JMenu getFileMenu() {
        if (this.fileMenu == null) {
            this.fileMenu = new JMenu();
            this.fileMenu.setText("File");
            this.fileMenu.setMnemonic(70);
            this.fileMenu.add(getMitNew());
            setCommand(getMitNew(), 'N');
            this.fileMenu.add(getMitGenerate());
            setCommand(getMitGenerate(), 'G');
            this.fileMenu.addSeparator();
            this.fileMenu.add(getMitLoad());
            setCommand(getMitLoad(), 'O');
            this.fileMenu.add(getMitSave());
            ///this.fileMenu.add(getMitSaveGridPicture());
            setCommand(getMitSave(), 'S');
            this.fileMenu.addSeparator();
            this.fileMenu.add(getMitQuit());
            setCommand(getMitQuit(), 'Q');
        }
        return this.fileMenu;
    }

//    private JMenuItem getMitSaveGridPicture() {
//        if (this.mitSaveGridPicture == null) {
//            this.mitSaveGridPicture = new JMenuItem();
//            this.mitSaveGridPicture.setText("Copy grid picture");
//            this.mitSaveGridPicture.setMnemonic(84);
//            this.mitSaveGridPicture.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK & InputEvent.SHIFT_MASK));
//            this.mitSaveGridPicture.setToolTipText("Save grid as picture to your clipboard");
//            this.mitSaveGridPicture.addActionListener(e -> SudokuFrame.this.engine.saveGridAsPicture());
//        }
//        return this.mitSolve;
//    }

    private JMenuItem getMitNew() {
        if (this.mitNew == null) {
            this.mitNew = new JMenuItem();
            this.mitNew.setText("New");
            this.mitNew.setAccelerator(KeyStroke.getKeyStroke(78, InputEvent.CTRL_MASK));
            this.mitNew.setMnemonic(78);
            this.mitNew.setToolTipText("Clear the grid");
            this.mitNew.addActionListener(e -> SudokuFrame.this.engine.clearGrid());
        }
        return this.mitNew;
    }

    private JMenuItem getMitQuit() {
        if (this.mitQuit == null) {
            this.mitQuit = new JMenuItem();
            this.mitQuit.setText("Quit");
            this.mitQuit.setMnemonic(81);
            this.mitQuit.setToolTipText("Bye bye");
            this.mitQuit.addActionListener(e -> SudokuFrame.this.quit());
        }
        return this.mitQuit;
    }

    private void warnAccessError(AccessControlException ex) {
        JOptionPane.showMessageDialog(this,
                "Sorry, this functionality cannot be used from an applet.\nDenied permission: " +
                        ex.getPermission().toString() + "\n" +
                        "Download the application to access this functionality.",
                "Access denied", JOptionPane.ERROR_MESSAGE);
    }

    private static class TextFileFilter extends FileFilter {
        private TextFileFilter() { }

        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            else return f.getName().toLowerCase().endsWith(".txt");
        }

        public String getDescription() {
            return "Text files (*.txt)";
        }
    }

    private JMenuItem getMitLoad() {
        if (this.mitLoad == null) {
            this.mitLoad = new JMenuItem();
            this.mitLoad.setText("Load...");
            this.mitLoad.setMnemonic(79);
            this.mitLoad.setToolTipText("Open the file selector to load the grid from a file");
            this.mitLoad.addActionListener(e -> {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileFilter(new TextFileFilter());
                    if (SudokuFrame.this.defaultDirectory != null) {
                        chooser.setCurrentDirectory(SudokuFrame.this.defaultDirectory);
                    }
                    int result = chooser.showOpenDialog(SudokuFrame.this);
                    SudokuFrame.this.defaultDirectory = chooser.getCurrentDirectory();
                    if (result == 0) {
                        SudokuFrame.this.engine.loadGrid(chooser.getSelectedFile());
                    }
                } catch (AccessControlException ex) {
                    SudokuFrame.this.warnAccessError(ex);
                }
            });
        }
        return this.mitLoad;
    }

    private JMenuItem getMitSave() {
        if (this.mitSave == null) {
            this.mitSave = new JMenuItem();
            this.mitSave.setText("Save...");
            this.mitSave.setMnemonic(83);
            this.mitSave.setToolTipText("Open the file selector to save the grid to a file");
            this.mitSave.addActionListener(e -> {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileFilter(new TextFileFilter());
                    if (SudokuFrame.this.defaultDirectory != null) {
                        chooser.setCurrentDirectory(SudokuFrame.this.defaultDirectory);
                    }
                    int result = chooser.showSaveDialog(SudokuFrame.this);
                    SudokuFrame.this.defaultDirectory = chooser.getCurrentDirectory();
                    if (result == 0) {
                        File file = chooser.getSelectedFile();
                        try {
                            if ((!file.getName().endsWith(".txt")) &&
                                    (file.getName().indexOf('.') < 0)) {
                                file = new File(file.getCanonicalPath() + ".txt");
                            }
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if ((file.exists()) &&
                                (JOptionPane.showConfirmDialog(SudokuFrame.this,
                                        "The file \"" + file.getName() + "\" already exists.\n" +
                                                "Do you want to replace the existing file ?",
                                        "Save", JOptionPane.OK_CANCEL_OPTION) != 0)) {
                            return;
                        }
                        SudokuFrame.this.engine.saveGrid(file);
                    }
                } catch (AccessControlException ex) {
                    SudokuFrame.this.warnAccessError(ex);
                }
            });
        }
        return this.mitSave;
    }

    private JMenu getEditMenu() {
        if (this.editMenu == null) {
            this.editMenu = new JMenu();
            this.editMenu.setText("Edit");
            this.editMenu.setMnemonic(69);
            this.editMenu.add(getMitCopy());
            setCommand(getMitCopy(), 'C');
            this.editMenu.add(getMitPaste());
            setCommand(getMitPaste(), 'V');
            this.editMenu.addSeparator();
            this.editMenu.add(getMitClear());
            setCommand(getMitClear(), 'E');
        }
        return this.editMenu;
    }

    private JMenuItem getMitCopy() {
        if (this.mitCopy == null) {
            this.mitCopy = new JMenuItem();
            this.mitCopy.setText("Copy grid");
            this.mitCopy.setMnemonic(67);
            this.mitCopy.setToolTipText("Copy the grid to the clipboard as plain text");
            this.mitCopy.addActionListener(e -> {
                try {
                    SudokuFrame.this.engine.copyGrid();
                } catch (AccessControlException ex) {
                    SudokuFrame.this.warnAccessError(ex);
                }
            });
        }
        return this.mitCopy;
    }

    private JMenuItem getMitClear() {
        if (this.mitClear == null) {
            this.mitClear = new JMenuItem();
            this.mitClear.setText("Clear grid");
            this.mitClear.setMnemonic(69);
            this.mitClear.setToolTipText("Clear the grid");
            this.mitClear.addActionListener(e -> SudokuFrame.this.engine.clearGrid());
        }
        return this.mitClear;
    }

    private JMenuItem getMitPaste() {
        if (this.mitPaste == null) {
            this.mitPaste = new JMenuItem();
            this.mitPaste.setText("Paste grid");
            this.mitPaste.setMnemonic(80);
            this.mitPaste.setToolTipText("Replace the grid with the content of the clipboard");
            this.mitPaste.addActionListener(e -> {
                try {
                    SudokuFrame.this.engine.pasteGrid();
                } catch (AccessControlException ex) {
                    SudokuFrame.this.warnAccessError(ex);
                }
            });
        }
        return this.mitPaste;
    }

    private JMenu getToolMenu() {
        if (this.toolMenu == null) {
            this.toolMenu = new JMenu();
            this.toolMenu.setText("Tools");
            this.toolMenu.setMnemonic(84);
            this.toolMenu.add(getMitResetPotentials());
            setCommand(getMitResetPotentials(), 'R');
            this.toolMenu.add(getMitClearHints());
            setCommand(getMitClearHints(), 'D');
            this.toolMenu.addSeparator();
            this.toolMenu.add(getMitCheckValidity());
            getMitCheckValidity().setAccelerator(KeyStroke.getKeyStroke(118, 0));
            this.toolMenu.add(getMitSolveStep());
            getMitSolveStep().setAccelerator(KeyStroke.getKeyStroke(113, 0));
            this.toolMenu.add(getMitGetNextHint());
            getMitGetNextHint().setAccelerator(KeyStroke.getKeyStroke(114, 0));
            this.toolMenu.add(getMitApplyHint());
            getMitApplyHint().setAccelerator(KeyStroke.getKeyStroke(115, 0));
            this.toolMenu.add(getMitGetAllHints());
            getMitGetAllHints().setAccelerator(KeyStroke.getKeyStroke(116, 0));
            this.toolMenu.add(getMitUndoStep());
            getMitUndoStep().setAccelerator(KeyStroke.getKeyStroke(90, InputEvent.CTRL_MASK));
            this.toolMenu.addSeparator();
            this.toolMenu.add(getMitGetSmallClue());
            getMitGetSmallClue().setAccelerator(KeyStroke.getKeyStroke(117, 0));
            this.toolMenu.add(getMitGetBigClue());
            getMitGetBigClue().setAccelerator(KeyStroke.getKeyStroke(117, InputEvent.SHIFT_MASK));
            this.toolMenu.addSeparator();
            this.toolMenu.add(getMitSolve());
            getMitSolve().setAccelerator(KeyStroke.getKeyStroke(119, 0));
            this.toolMenu.add(getMitAnalyse());
            getMitAnalyse().setAccelerator(KeyStroke.getKeyStroke(120, 0));
        }
        return this.toolMenu;
    }

    private JMenuItem getMitCheckValidity() {
        if (this.mitCheckValidity == null) {
            this.mitCheckValidity = new JMenuItem();
            this.mitCheckValidity.setText("Check validity");
            this.mitCheckValidity.setMnemonic(86);
            this.mitCheckValidity.setToolTipText("Check if the Sudoku has exactly one solution");
            this.mitCheckValidity.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (SudokuFrame.this.engine.checkValidity()) {
                        SudokuFrame.this.setExplanations(HtmlLoader.loadHtml(this, "Valid.html"));
                    }
                }
            });
        }
        return this.mitCheckValidity;
    }

    private JMenuItem getMitAnalyse() {
        if (this.mitAnalyse == null) {
            this.mitAnalyse = new JMenuItem();
            this.mitAnalyse.setText("Analyze");
            this.mitAnalyse.setMnemonic(89);
            this.mitAnalyse.setToolTipText("List the rules required to solve the Sudoku and get its average difficulty");

            this.mitAnalyse.addActionListener(e -> {
                try {
                    SudokuFrame.this.engine.analyse();
                } catch (UnsupportedOperationException ex) {
                    JOptionPane.showMessageDialog(SudokuFrame.this,
                            "The Sudoku Explainer failed to solve this Sudoku\n" +
                                    "using the solving techniques that are currently enabled.",

                            "Analysis", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        return this.mitAnalyse;
    }

    private JMenuItem getMitUndoStep() {
        if (this.mitUndoStep == null) {
            this.mitUndoStep = new JMenuItem();
            this.mitUndoStep.setText("Undo step");
            this.mitUndoStep.setMnemonic(85);
            this.mitUndoStep.setToolTipText(getBtnUndoStep().getToolTipText());
            this.mitUndoStep.addActionListener(e -> SudokuFrame.this.engine.undoStep());
        }
        return this.mitUndoStep;
    }

    private JMenuItem getMitSolveStep() {
        if (this.mitSolveStep == null) {
            this.mitSolveStep = new JMenuItem();
            this.mitSolveStep.setText("Solve step");
            this.mitSolveStep.setMnemonic(83);
            this.mitSolveStep.setToolTipText(getBtnApplyHintAndGet().getToolTipText());
            this.mitSolveStep.addActionListener(e -> SudokuFrame.this.engine.applySelectedHintsAndContinue());
        }
        return this.mitSolveStep;
    }

    private JMenuItem getMitGetNextHint() {
        if (this.mitGetNextHint == null) {
            this.mitGetNextHint = new JMenuItem();
            this.mitGetNextHint.setText("Get next hint");
            this.mitGetNextHint.setMnemonic(78);
            this.mitGetNextHint.setToolTipText(getBtnGetNextHint().getToolTipText());
            this.mitGetNextHint.addActionListener(e -> SudokuFrame.this.engine.getNextHint());
        }
        return this.mitGetNextHint;
    }

    private JMenuItem getMitApplyHint() {
        if (this.mitApplyHint == null) {
            this.mitApplyHint = new JMenuItem();
            this.mitApplyHint.setText("Apply hint");
            this.mitApplyHint.setEnabled(false);
            this.mitApplyHint.setMnemonic(65);
            this.mitApplyHint.setToolTipText(getBtnApplyHint().getToolTipText());
            this.mitApplyHint.addActionListener(e -> SudokuFrame.this.engine.applySelectedHints());
        }
        return this.mitApplyHint;
    }

    private JMenuItem getMitGetAllHints() {
        if (this.mitGetAllHints == null) {
            this.mitGetAllHints = new JMenuItem();
            this.mitGetAllHints.setText("Get all hints");
            this.mitGetAllHints.setMnemonic(72);
            this.mitGetAllHints.setToolTipText(getBtnGetAllHints().getToolTipText());
            this.mitGetAllHints.addActionListener(e -> SudokuFrame.this.engine.getAllHints());
        }
        return this.mitGetAllHints;
    }

    private JMenuItem getMitSolve() {
        if (this.mitSolve == null) {
            this.mitSolve = new JMenuItem();
            this.mitSolve.setText("Solve");
            this.mitSolve.setMnemonic(79);
            this.mitSolve.setToolTipText("Highlight the solution");
            this.mitSolve.addActionListener(e -> SudokuFrame.this.engine.solve());
        }
        return this.mitSolve;
    }

    private JMenuItem getMitResetPotentials() {
        if (this.mitResetPotentials == null) {
            this.mitResetPotentials = new JMenuItem();
            this.mitResetPotentials.setText("Reset potential values");
            this.mitResetPotentials.setToolTipText("Recompute the remaining possible values for the empty cells");
            this.mitResetPotentials.setMnemonic(82);
            this.mitResetPotentials.addActionListener(e -> SudokuFrame.this.engine.resetPotentials());
        }
        return this.mitResetPotentials;
    }

    private JMenuItem getMitClearHints() {
        if (this.mitClearHints == null) {
            this.mitClearHints = new JMenuItem();
            this.mitClearHints.setText("Clear hint(s)");
            this.mitClearHints.setMnemonic(67);
            this.mitClearHints.setToolTipText("Clear the hint list");
            this.mitClearHints.addActionListener(e -> SudokuFrame.this.engine.clearHints());
        }
        return this.mitClearHints;
    }

    private JMenu getOptionsMenu() {
        if (this.optionsMenu == null) {
            this.optionsMenu = new JMenu();
            this.optionsMenu.setText("Options");
            this.optionsMenu.setMnemonic(79);
            this.optionsMenu.add(getMitFilter());
            this.optionsMenu.add(getMitShowCandidates());
            this.optionsMenu.add(getMitSelectTechniques());
            this.optionsMenu.addSeparator();
            this.optionsMenu.add(getMitChessMode());
            this.optionsMenu.add(getMitMathMode());
            this.optionsMenu.addSeparator();
            this.optionsMenu.add(getMitLookAndFeel());
            this.optionsMenu.add(getMitAntiAliasing());
            ButtonGroup group = new ButtonGroup();
            group.add(getMitChessMode());
            group.add(getMitMathMode());
        }
        return this.optionsMenu;
    }

    private JCheckBoxMenuItem getMitFilter() {
        if (this.mitFilter == null) {
            this.mitFilter = new JCheckBoxMenuItem();
            this.mitFilter.setText("Filter hints with similar outcome");
            this.mitFilter.setSelected(true);
            this.mitFilter.setEnabled(false);
            this.mitFilter.setMnemonic(70);
            this.mitFilter.setToolTipText(getChkFilter().getToolTipText());
            this.mitFilter.addItemListener(e -> SudokuFrame.this.engine.setFiltered(SudokuFrame.this.mitFilter.isSelected()));
        }
        return this.mitFilter;
    }

    private JRadioButtonMenuItem getMitMathMode() {
        if (this.mitMathMode == null) {
            this.mitMathMode = new JRadioButtonMenuItem();
            this.mitMathMode.setText("R1C1 - R9C9 cell notation");
            this.mitMathMode.setMnemonic(82);
            this.mitMathMode.setSelected(Settings.getInstance().isRCNotation());
            this.mitMathMode.addItemListener(e -> {
                if (SudokuFrame.this.mitMathMode.isSelected()) {
                    Settings.getInstance().setRCNotation(true);
                    SudokuFrame.this.repaint();
                }
            });
        }
        return this.mitMathMode;
    }

    private JRadioButtonMenuItem getMitChessMode() {
        if (this.mitChessMode == null) {
            this.mitChessMode = new JRadioButtonMenuItem();
            this.mitChessMode.setText("A1 - I9 cell notation");
            this.mitChessMode.setMnemonic(65);
            this.mitChessMode.setSelected(!Settings.getInstance().isRCNotation());
            this.mitChessMode.addItemListener(e -> {
                if (SudokuFrame.this.mitChessMode.isSelected()) {
                    Settings.getInstance().setRCNotation(false);
                    SudokuFrame.this.repaint();
                }
            });
        }
        return this.mitChessMode;
    }

    private JCheckBoxMenuItem getMitAntiAliasing() {
        if (this.mitAntiAliasing == null) {
            this.mitAntiAliasing = new JCheckBoxMenuItem();
            this.mitAntiAliasing.setText("High quality rendering");
            this.mitAntiAliasing.setSelected(Settings.getInstance().isAntialiasing());
            this.mitAntiAliasing.setMnemonic(72);
            this.mitAntiAliasing.setToolTipText("Use high quality (but slow) rendering");
            this.mitAntiAliasing.addItemListener(e -> {
                Settings.getInstance().setAntialiasing(SudokuFrame.this.mitAntiAliasing.isSelected());
                SudokuFrame.this.repaint();
            });
        }
        return this.mitAntiAliasing;
    }

    private JMenu getHelpMenu() {
        if (this.helpMenu == null) {
            this.helpMenu = new JMenu();
            this.helpMenu.setText("Help");
            this.helpMenu.setMnemonic(72);
            this.helpMenu.add(getMitShowWelcome());
            getMitShowWelcome().setAccelerator(KeyStroke.getKeyStroke(112, 0));
            this.helpMenu.addSeparator();
            this.helpMenu.add(getMitAbout());
        }
        return this.helpMenu;
    }

    private JMenuItem getMitAbout() {
        if (this.mitAbout == null) {
            this.mitAbout = new JMenuItem();
            this.mitAbout.setText("About");
            this.mitAbout.setToolTipText("Get information about the Sudoku Explainer application");
            this.mitAbout.setMnemonic(65);
            this.mitAbout.addActionListener(e -> {
                if (SudokuFrame.this.dummyFrameKnife == null) {
                    SudokuFrame.this.dummyFrameKnife = new JFrame();
                    ImageIcon icon = SudokuFrame.this.createImageIcon("Knife.gif");
                    assert icon != null;
                    SudokuFrame.this.dummyFrameKnife.setIconImage(icon.getImage());
                }
                AboutDialog dlg = new AboutDialog(SudokuFrame.this.dummyFrameKnife);
                SudokuFrame.this.centerDialog(dlg);
                dlg.setVisible(true);
            });
        }
        return this.mitAbout;
    }

    private JMenuItem getMitGetSmallClue() {
        if (this.mitGetSmallClue == null) {
            this.mitGetSmallClue = new JMenuItem();
            this.mitGetSmallClue.setText("Get a small clue");
            this.mitGetSmallClue.setMnemonic(77);
            this.mitGetSmallClue.setToolTipText("Get some information on the next solving step");
            this.mitGetSmallClue.addActionListener(e -> SudokuFrame.this.engine.getClue(false));
        }
        return this.mitGetSmallClue;
    }

    private JMenuItem getMitGetBigClue() {
        if (this.mitGetBigClue == null) {
            this.mitGetBigClue = new JMenuItem();
            this.mitGetBigClue.setText("Get a big clue");
            this.mitGetBigClue.setMnemonic(66);
            this.mitGetBigClue.setToolTipText("Get more information on the next solving step");
            this.mitGetBigClue.addActionListener(e -> SudokuFrame.this.engine.getClue(true));
        }
        return this.mitGetBigClue;
    }

    private JMenu getMitLookAndFeel() {
        if (this.mitLookAndFeel == null) {
            this.mitLookAndFeel = new JMenu();
            this.mitLookAndFeel.setText("Look & Feel");
            this.mitLookAndFeel.setMnemonic(76);
            this.mitLookAndFeel.setToolTipText("Change the appearance of the application by choosing one of the available schemes");
        }
        return this.mitLookAndFeel;
    }

    private JMenuItem getMitShowWelcome() {
        if (this.mitShowWelcome == null) {
            this.mitShowWelcome = new JMenuItem();
            this.mitShowWelcome.setMnemonic(87);
            this.mitShowWelcome.setToolTipText("Show the explanation text displayed when the application is started");
            this.mitShowWelcome.setText("Show welcome message");
            this.mitShowWelcome.addActionListener(e -> SudokuFrame.this.showWelcomeText());
        }
        return this.mitShowWelcome;
    }

    private JMenuItem getMitGenerate() {
        if (this.mitGenerate == null) {
            this.mitGenerate = new JMenuItem();
            this.mitGenerate.setText("Generate...");
            this.mitGenerate.setMnemonic(71);
            this.mitGenerate.setToolTipText("Open a dialog to generate a random Sudoku puzzle");
            this.mitGenerate.addActionListener(e -> {
                if ((SudokuFrame.this.generateDialog == null) ||
                        (!SudokuFrame.this.generateDialog.isVisible())) {
                    SudokuFrame.this.generateDialog = new GenerateDialog(
                            SudokuFrame.this, SudokuFrame.this.engine);
                    SudokuFrame.this.generateDialog.pack();
                    SudokuFrame.this.centerDialog(SudokuFrame.this.generateDialog);
                }
                SudokuFrame.this.generateDialog.setVisible(true);
            });
        }
        return this.mitGenerate;
    }

    private void centerDialog(JDialog dlg) {
        Point frameLocation = getLocation();
        Dimension frameSize = getSize();
        Dimension windowSize = dlg.getSize();
        dlg.setLocation(
                frameLocation.x + (frameSize.width - windowSize.width) / 2,
                frameLocation.y + (frameSize.height - windowSize.height) / 3);
    }

    private JCheckBoxMenuItem getMitShowCandidates() {
        if (this.mitShowCandidates == null) {
            this.mitShowCandidates = new JCheckBoxMenuItem();
            this.mitShowCandidates.setText("Show candidates");
            this.mitShowCandidates.setToolTipText("Display all possible values as small digits in empty cells");
            this.mitShowCandidates.setMnemonic(67);
            this.mitShowCandidates.setSelected(Settings.getInstance().isShowingCandidates());
            this.mitShowCandidates.addItemListener(e -> {
                Settings.getInstance().setShowingCandidates(SudokuFrame.this.mitShowCandidates.isSelected());
                SudokuFrame.this.repaint();
            });
        }
        return this.mitShowCandidates;
    }

    private JMenuItem getMitSelectTechniques() {
        if (this.mitSelectTechniques == null) {
            this.mitSelectTechniques = new JMenuItem();
            this.mitSelectTechniques.setMnemonic(84);
            this.mitSelectTechniques.setToolTipText("Open a dialog window to enable and disable individual solving techniques");
            this.mitSelectTechniques.setText("Solving techniques...");
            this.mitSelectTechniques.addActionListener(e -> SudokuFrame.this.selectTechniques());
        }
        return this.mitSelectTechniques;
    }

    private void selectTechniques() {
        if ((this.selectDialog == null) || (!this.selectDialog.isVisible())) {
            this.selectDialog = new TechniquesSelectDialog(this, this.engine);
            this.selectDialog.pack();
            centerDialog(this.selectDialog);
        }
        this.selectDialog.setVisible(true);
        refreshSolvingTechniques();
        this.engine.rebuildSolver();
    }

    private JPanel getPnlEnabledTechniques() {
        if (this.pnlEnabledTechniques == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(FlowLayout.LEFT);
            this.lblEnabledTechniques = new JLabel();
            this.lblEnabledTechniques.setToolTipText("<html><body>Not all the available solving techniques are enabled.<br>Use the <b>Options</b>-&gt;<b>Solving techniques</b> menu to<br>enable or disable individual solving techniques.</body></html>");
            this.lblEnabledTechniques.setIcon(new ImageIcon(getClass().getResource("/SudokuExplainer/gui/Warning.gif")));
            this.lblEnabledTechniques.setText("");
            this.lblEnabledTechniques.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        SudokuFrame.this.selectTechniques();
                    }
                }
            });
            this.pnlEnabledTechniques = new JPanel();
            this.pnlEnabledTechniques.setLayout(flowLayout1);
            this.pnlEnabledTechniques.add(this.lblEnabledTechniques, null);
            this.pnlEnabledTechniques.setVisible(false);
        }
        return this.pnlEnabledTechniques;
    }

    void quit() {
        setVisible(false);
        dispose();
        if (this.selectDialog != null) {
            this.selectDialog.dispose();
        }
        if (this.generateDialog != null) {
            this.generateDialog.dispose();
        }
        if (this.dummyFrameKnife != null) {
            this.dummyFrameKnife.dispose();
        }
    }
}
