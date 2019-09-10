package SudokuExplainer.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import static SudokuExplainer.Settings.*;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = -5231673684723681106L;

    private JPanel jContentPane = null;
    private JPanel pnlTop = null;
    private JLabel lblTitle = null;
    private JLabel lblCopyright = null;
    private JPanel pnlCenter = null;
    private JPanel pnlBottom = null;
    private JButton btnOk = null;
    private JLabel lblVersion = null;
    private JLabel txtVersion = null;
    private JLabel lblCompany = null;
    private JLabel txtCompany = null;
    private JLabel lblLicense = null;
    private JLabel txtLicense = null;

    public AboutDialog(JFrame parent) {
        super(parent);
        initialize();
    }

    private void initialize() {
        this.setSize(new Dimension(255,203));
        this.setResizable(false);
        this.setContentPane(getJContentPane());
        this.setTitle("Sudoku Explainer - About");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                AboutDialog.this.setVisible(false);
                AboutDialog.this.dispose();
            }
        });
        txtVersion.setText("" + VERSION + "." + REVISION + SUB_REV);
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getPnlTop(), BorderLayout.NORTH);
            jContentPane.add(getPnlCenter(), BorderLayout.CENTER);
            jContentPane.add(getPnlBottom(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    private JPanel getPnlTop() {
        if (pnlTop == null) {
            lblCopyright = new JLabel();
            lblCopyright.setText("(C) 2005-2006 Nicolas Juillerat");
            lblCopyright.setHorizontalAlignment(SwingConstants.CENTER);
            lblTitle = new JLabel();
            lblTitle.setText("Sudoku Explainer");
            lblTitle.setFont(new Font("Comic Sans MS", Font.BOLD, 24));
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
            lblTitle.setHorizontalTextPosition(SwingConstants.TRAILING);
            lblTitle.setPreferredSize(new Dimension(234,48));
            lblTitle.setIcon(new ImageIcon(getClass().getResource("/SudokuExplainer/gui/Sudoku.gif")));
            pnlTop = new JPanel();
            pnlTop.setLayout(new BorderLayout());
            pnlTop.add(lblTitle, BorderLayout.NORTH);
            pnlTop.add(lblCopyright, BorderLayout.SOUTH);
        }
        return pnlTop;
    }

    private JPanel getPnlCenter() {
        if (pnlCenter == null) {
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 1;
            gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.weightx = 1.0D;
            gridBagConstraints6.gridy = 4;
            txtLicense = new JLabel();
            txtLicense.setText("Lesser General Public License");
            txtLicense.setFont(new Font("Dialog", Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.weightx = 1.0D;
            gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.insets = new Insets(2,10,2,0);
            gridBagConstraints5.gridy = 4;
            lblLicense = new JLabel();
            lblLicense.setText("License:");
            lblLicense.setFont(new Font("Dialog", Font.BOLD, 12));
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 1;
            gridBagConstraints4.weightx = 1.0D;
            gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.gridy = 3;
            txtCompany = new JLabel();
            txtCompany.setText("University of Fribourg (CH)");
            txtCompany.setFont(new Font("Dialog", Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.weightx = 1.0D;
            gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.insets = new Insets(2,10,2,0);
            gridBagConstraints3.gridy = 3;
            lblCompany = new JLabel();
            lblCompany.setText("Company:");
            lblCompany.setFont(new Font("Dialog", Font.BOLD, 12));
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.weightx = 1.0D;
            gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridy = 1;
            txtVersion = new JLabel();
            txtVersion.setText("");
            txtVersion.setFont(new Font("Dialog", Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new Insets(2,10,2,0);
            gridBagConstraints1.gridy = 1;
            lblVersion = new JLabel();
            lblVersion.setText("Version:");
            lblVersion.setFont(new Font("Dialog", Font.BOLD, 12));
            pnlCenter = new JPanel();
            pnlCenter.setLayout(new GridBagLayout());
            pnlCenter.add(lblVersion, gridBagConstraints1);
            pnlCenter.add(txtVersion, gridBagConstraints2);
            pnlCenter.add(lblCompany, gridBagConstraints3);
            pnlCenter.add(txtCompany, gridBagConstraints4);
            pnlCenter.add(lblLicense, gridBagConstraints5);
            pnlCenter.add(txtLicense, gridBagConstraints6);
        }
        return pnlCenter;
    }

    private JPanel getPnlBottom() {
        if (pnlBottom == null) {
            pnlBottom = new JPanel();
            pnlBottom.setLayout(new FlowLayout());
            pnlBottom.add(getBtnOk(), null);
        }
        return pnlBottom;
    }

    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText("OK");
            btnOk.addActionListener(e -> {
                AboutDialog.this.setVisible(false);
                AboutDialog.this.dispose();
            });
        }
        return btnOk;
    }

}
