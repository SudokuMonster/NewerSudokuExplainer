package SudokuExplainer.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import SudokuExplainer.*;
import SudokuExplainer.generator.*;
import SudokuExplainer.solver.*;
import SudokuExplainer.tools.*;
import SudokuExplainer.units.Grid;

public class GenerateDialog extends JDialog {

    private static final long serialVersionUID = 8620081149465721387L;

    private enum Difficulty {
        VeryEasy {
            @Override
            public double getMinDifficulty() {
                return 1.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 1.2;
            }

            @Override
            public String getFieldFullName() {
                return "Very easy";
            }
        },
        Easy {
            @Override
            public double getMinDifficulty() {
                return 1.3;
            }

            @Override
            public double getMaxDifficulty() {
                return 1.5;
            }

            @Override
            public String getFieldFullName() {
                return "Easy";
            }
        },
        Moderate {
            @Override
            public double getMinDifficulty() {
                return 1.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 2.3;
            }

            @Override
            public String getFieldFullName() {
                return "Moderate";
            }
        },
        Advanced {
            @Override
            public double getMinDifficulty() {
                return 2.4;
            }

            @Override
            public double getMaxDifficulty() {
                return 2.8;
            }

            @Override
            public String getFieldFullName() {
                return "Advanced";
            }
        },
        Hard {
            @Override
            public double getMinDifficulty() {
                return 2.9;
            }

            @Override
            public double getMaxDifficulty() {
                return 3.4;
            }

            @Override
            public String getFieldFullName() {
                return "Hard";
            }
        },
        VeryHard {
            @Override
            public double getMinDifficulty() {
                return 3.5;
            }

            @Override
            public double getMaxDifficulty() {
                return 4.4;
            }

            @Override
            public String getFieldFullName() {
                return "Very hard";
            }
        },
        Fiendish {
            @Override
            public double getMinDifficulty() {
                return 4.5;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.2;
            }

            @Override
            public String getFieldFullName() {
                return "Fiendish";
            }
        },
        Diabolical {
            @Override
            public double getMinDifficulty() {
                return 6.3;
            }

            @Override
            public double getMaxDifficulty() {
                return 7.6;
            }

            @Override
            public String getFieldFullName() {
                return "Diabolical";
            }
        },
        Crazy {
            @Override
            public double getMinDifficulty() {
                return 7.7;
            }

            @Override
            public double getMaxDifficulty() {
                return 8.9;
            }

            @Override
            public String getFieldFullName() {
                return "Crazy";
            }
        },
        Nightmare {
            @Override
            public double getMinDifficulty() {
                return 9.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 10.0;
            }

            @Override
            public String getFieldFullName() {
                return "Nightmare";
            }
        },
        BeyondNightmare {
            @Override
            public double getMinDifficulty() {
                return 10.1;
            }

            @Override
            public double getMaxDifficulty() {
                return 20.0;
            }

            @Override
            public String getFieldFullName() {
                return "Beyond Nightmare";
            }
        };

        public abstract double getMinDifficulty();
        public abstract double getMaxDifficulty();
        public abstract String getFieldFullName();

        public String getHtmlDescription() {
            return HtmlLoader.loadHtml(this, "difficultyInfo/" + this.name() + ".html");
        }
    }

    private final SudokuExplainer engine;
    private JButton btnGenerate;
    private JButton btnNext;
    private JButton btnPrev;
    private JLabel lblDifficulty;
    private JCheckBox chkAnalysis;

    private EnumSet<Symmetry> symmetries = EnumSet.noneOf(Symmetry.class);
    private Difficulty difficulty = Difficulty.VeryEasy;
    private boolean isExact = true;

    private GeneratorThread generator = null;
    private List<Grid> sudokuList = new ArrayList<>();
    private int sudokuIndex = 0;
    private Map<Grid, Hint> sudokuAnalyses = new HashMap<>();


    public GenerateDialog(JFrame owner, SudokuExplainer engine) {
        super(owner, false);
        this.engine = engine;
        initParameters();
        initGUI();
    }

    private void initParameters() {
        symmetries.add(Symmetry.Orthogonal);
        symmetries.add(Symmetry.BiDiagonal);
        symmetries.add(Symmetry.Rotational180);
        symmetries.add(Symmetry.Rotational90);
        symmetries.add(Symmetry.Full);

        sudokuList.add(engine.getGrid());
    }

    private boolean isTechniqueSetSafe() {
        Settings settings = Settings.getInstance();
        if (!settings.getTechniques().contains(SolvingTechnique.HiddenSingle))
            return false;
        if (!settings.isUsingOneOf(SolvingTechnique.DirectPointing))
            return false;
        if (!settings.isUsingAll(
                SolvingTechnique.LockedCandidates,
                SolvingTechnique.HiddenPair,
                SolvingTechnique.NakedPair,
                SolvingTechnique.XWing))
            return false;
        return settings.isUsingOneOf(SolvingTechnique.ForcingChainCycle);
    }

    private void initGUI() {
        // This
        setTitle("Generate a random Sudoku");
        setResizable(false);

        // Overall layout
        getContentPane().setLayout(new BorderLayout());
        JPanel paramPanel = new JPanel();
        JPanel commandPanel = new JPanel();
        getContentPane().add(paramPanel, BorderLayout.CENTER);
        getContentPane().add(commandPanel, BorderLayout.SOUTH);

        // Command pane
        commandPanel.setLayout(new GridLayout(1, 2));
        JPanel pnlGenerate = new JPanel();
        pnlGenerate.setLayout(new FlowLayout(FlowLayout.CENTER));
        commandPanel.add(pnlGenerate);
        JPanel pnlClose = new JPanel();
        pnlClose.setLayout(new FlowLayout(FlowLayout.CENTER));
        commandPanel.add(pnlClose);

        btnPrev = new JButton();
        btnPrev.setText("<");
        btnPrev.setEnabled(false);
        btnPrev.setMnemonic(KeyEvent.VK_LEFT);
        btnPrev.setToolTipText("Restore the previous Sudoku");
        btnPrev.addActionListener(e -> prev());
        pnlGenerate.add(btnPrev);
        btnGenerate = new JButton();
        btnGenerate.setFont(new Font("Dialog", Font.BOLD, 12));
        btnGenerate.setText("Generate");
        btnGenerate.setMnemonic(KeyEvent.VK_G);
        btnGenerate.setToolTipText("Generate a new random Sudoku that matches the given parameters");
        btnGenerate.addActionListener(e -> {
            if (generator == null) {
                if (!isTechniqueSetSafe()) {
                    int result = JOptionPane.showConfirmDialog(
                            GenerateDialog.this,
                            "<html><body>" +
                            "<b>Warning</b>: not all solving techniques are enabled.<br>" +
                            "The Sudoku Explainer may not be able to generate<br>" +
                            "a Sudoku with the selected parameters (it may loop<br>" +
                            "for ever until you stop it).<br><br>" +
                            "Do you want to continue anyway?" +
                            "</body></html>",
                            GenerateDialog.this.getTitle(),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result != JOptionPane.YES_OPTION)
                        return;
                }
                generate();
            } else
                stop();
        });
        pnlGenerate.add(btnGenerate);
        btnNext = new JButton();
        btnNext.setText(">");
        btnNext.setEnabled(false);
        btnNext.setMnemonic(KeyEvent.VK_RIGHT);
        btnNext.setToolTipText("Restore the next Sudoku");
        btnNext.addActionListener(e -> next());
        pnlGenerate.add(btnNext);

        JButton btnClose = new JButton();
        btnClose.setText("Close");
        btnClose.setMnemonic(KeyEvent.VK_C);
        btnClose.addActionListener(e -> close());
        pnlClose.add(btnClose);

        // Parameters pane
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
        JPanel symmetryPanel = new JPanel();
        symmetryPanel.setBorder(new TitledBorder("Allowed symmetry types"));
        paramPanel.add(symmetryPanel);
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setBorder(new TitledBorder("Difficulty"));
        paramPanel.add(difficultyPanel);

        // Parameters - Symmetry pane
        symmetryPanel.setLayout(new GridLayout(3, 4));
        for (final Symmetry symmetry : Symmetry.values()) {
            final JCheckBox chkSymmetry = new JCheckBox();
            chkSymmetry.setSelected(symmetries.contains(symmetry));
            chkSymmetry.setText(symmetry.toString());
            chkSymmetry.setToolTipText(symmetry.getDescription());
            chkSymmetry.addActionListener(e -> {
                if (chkSymmetry.isSelected()) {
                    symmetries.add(symmetry);
                } else {
                    symmetries.remove(symmetry);
                }
            });
            symmetryPanel.add(chkSymmetry);
        }

        // Parameters - Difficulty
        difficultyPanel.setLayout(new BorderLayout());
        JPanel diffChooserPanel = new JPanel();
        diffChooserPanel.setLayout(new BoxLayout(diffChooserPanel, BoxLayout.X_AXIS));
        difficultyPanel.add(diffChooserPanel, BorderLayout.NORTH);
        final JComboBox selDifficulty = new JComboBox();
        for (Difficulty difficulty : Difficulty.values()) {
            selDifficulty.addItem(difficulty.getFieldFullName());
        }
        selDifficulty.setToolTipText("Choose the difficulty of the Sudoku to generate");
        selDifficulty.addActionListener(e -> {
            String temp = (String)selDifficulty.getSelectedItem();
            assert temp != null;
            switch (temp)
            {
                default:
                case "Very easy": difficulty = Difficulty.VeryEasy; break;
                case "Easy": difficulty = Difficulty.Easy; break;
                case "Moderate": difficulty = Difficulty.Moderate; break;
                case "Advanced": difficulty = Difficulty.Advanced; break;
                case "Hard": difficulty = Difficulty.Hard; break;
                case "Very hard": difficulty = Difficulty.VeryHard; break;
                case "Fiendish": difficulty = Difficulty.Fiendish; break;
                case "Diabolical": difficulty = Difficulty.Diabolical; break;
                case "Crazy": difficulty = Difficulty.Crazy; break;
                case "Nightmare": difficulty = Difficulty.Nightmare; break;
                case "Beyond Nightmare": difficulty = Difficulty.BeyondNightmare; break;
            }

            lblDifficulty.setText(difficulty.getHtmlDescription());
        });
        diffChooserPanel.add(selDifficulty);
        final JRadioButton chkExactDifficulty = new JRadioButton("Exact difficulty");
        chkExactDifficulty.setToolTipText("Generate a Sudoku with exactly the chosen difficulty");
        chkExactDifficulty.setMnemonic(KeyEvent.VK_E);
        chkExactDifficulty.addActionListener(e -> {
            if (chkExactDifficulty.isSelected())
                isExact = true;
        });
        diffChooserPanel.add(chkExactDifficulty);
        final JRadioButton chkMaximumDifficulty = new JRadioButton("Maximum difficulty");
        chkMaximumDifficulty.setToolTipText("Generate a Sudoku with at most the chosen difficulty");
        chkMaximumDifficulty.setMnemonic(KeyEvent.VK_M);
        chkMaximumDifficulty.addActionListener(e -> {
            if (chkMaximumDifficulty.isSelected())
                isExact = false;
        });
        diffChooserPanel.add(chkMaximumDifficulty);
        ButtonGroup group = new ButtonGroup();
        group.add(chkExactDifficulty);
        group.add(chkMaximumDifficulty);
        chkExactDifficulty.setSelected(true);

        JPanel pnlDifficulty = new JPanel();
        pnlDifficulty.setLayout(new BorderLayout());
        pnlDifficulty.setBorder(new TitledBorder("Description"));
        difficultyPanel.add(pnlDifficulty, BorderLayout.CENTER);
        lblDifficulty = new JLabel();
        lblDifficulty.setText("<html><body><b>W</b><br>W<br>W</body></html>");
        lblDifficulty.setToolTipText("Explanations of the chosen difficulty");
        pnlDifficulty.add(lblDifficulty, BorderLayout.NORTH);
        SwingUtilities.invokeLater(() -> lblDifficulty.setText(difficulty.getHtmlDescription()));

        // Parameters - Warning label
        JPanel warningPanel = new JPanel();
        warningPanel.setBorder(new TitledBorder("Warning"));
        paramPanel.add(warningPanel);
        JLabel lblWarning = new JLabel();
        warningPanel.add(lblWarning);
        lblWarning.setText("<html><body>Depending on the chosen parameters, it may " +
        "take from<br>a few seconds to some minutes to generate a new Sudoku.</body></html>");

        // Parameters - options
        JPanel optionPanel = new JPanel();
        optionPanel.setBorder(new TitledBorder(""));
        optionPanel.setLayout(new GridLayout(1, 1));
        paramPanel.add(optionPanel, BorderLayout.NORTH);
        chkAnalysis = new JCheckBox("Show the analysis of the generated Sudoku");
        chkAnalysis.setToolTipText("Display the difficulty rating of the Sudoku and the " +
        "techniques that are necessary to solve it in the main window");
        chkAnalysis.setMnemonic(KeyEvent.VK_A);
        chkAnalysis.addActionListener(e -> refreshSudokuPanel());
        optionPanel.add(chkAnalysis);
    }

    private void generate() {
        if (symmetries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one symmetry",
                    "Generate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Gather parameters
        double minDifficulty = difficulty.getMinDifficulty();
        double maxDifficulty = isExact ? difficulty.getMaxDifficulty() : 1.0;
        List<Symmetry> symList = new ArrayList<>(symmetries);

        // Generate grid
        generator = new GeneratorThread(symList, minDifficulty, maxDifficulty);
        generator.start();
    }

    /**
     * Thread that generates a mew grid.
     */
    private class GeneratorThread extends Thread {

        private final List<Symmetry> symmetries;
        private final double minDifficulty;
        private final double maxDifficulty;

        private Generator generator;


        public GeneratorThread(List<Symmetry> symmetries, double minDifficulty, double maxDifficulty) {
            this.symmetries = symmetries;
            this.minDifficulty = minDifficulty;
            this.maxDifficulty = maxDifficulty;
        }

        @Override
        public void interrupt() {
            generator.interrupt();
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                engine.setGrid(new Grid());
                AutoBusy.setBusy(GenerateDialog.this, true);
                AutoBusy.setBusy(btnGenerate, false);
                btnGenerate.setText("Stop");
            });
            generator = new Generator();
            final Grid result = generator.generate(symmetries, minDifficulty, maxDifficulty);
            SwingUtilities.invokeLater(() -> {
                if (result != null) {
                    sudokuList.add(result);
                    sudokuIndex = sudokuList.size() - 1;
                    refreshSudokuPanel();
                }
                if (GenerateDialog.this.isVisible()) {
                    AutoBusy.setBusy(GenerateDialog.this, false);
                    btnGenerate.setText("Generate");
                }
            });
            GenerateDialog.this.generator = null;
        }

    }

    private void stop() {
        if (generator != null && generator.isAlive()) {
            generator.interrupt();
            try {
                generator.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        generator = null;
        refreshSudokuPanel();
    }

    private void next() {
        if (sudokuIndex < sudokuList.size() - 1) {
            sudokuIndex++;
            refreshSudokuPanel();
        }
    }

    private void prev() {
        if (sudokuIndex > 0) {
            sudokuIndex--;
            refreshSudokuPanel();
        }
    }

    private void refreshSudokuPanel() {
        Grid sudoku = sudokuList.get(sudokuIndex);
        engine.setGrid(sudoku);
        btnPrev.setEnabled(sudokuIndex > 0);
        btnNext.setEnabled(sudokuIndex < sudokuList.size() - 1);

        if (chkAnalysis.isSelected()) {
            // Display analysis of the Sudoku
            Hint analysis = sudokuAnalyses.get(sudoku);
            if (analysis == null) {
                analysis = engine.analyse();
                sudokuAnalyses.put(sudoku, analysis);
            } else {
                engine.showHint(analysis);
            }
        }
    }

    private void close() {
        stop();
        super.setVisible(false);
        super.dispose();
    }

}
