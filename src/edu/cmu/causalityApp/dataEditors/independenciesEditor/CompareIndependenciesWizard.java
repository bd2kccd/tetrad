package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causality.IndependenciesTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This wizard allows user to select which hypothesis and sample independencies
 * to compare on the independencies window.
 * <p/>
 * This checks each row given the selected columns to look out for and selects
 * those rows with differences. The background color of those selected rows will
 * turn yellow.
 *
 * @author Adrian Tang
 */
class CompareIndependenciesWizard extends JInternalFrame {
    final private static String MY_NAME = "Compare Independencies";

    private final IndependenciesTable table;
    private final Vector independenciesToCompare;
    private final Vector<JCheckBox> checkboxes;

    /**
     * Constructor. Creates the wizard.
     */
    public CompareIndependenciesWizard(IndependenciesTable table) {
        super(MY_NAME, true, true, true, true);

        this.table = table;
        independenciesToCompare = new Vector();
        checkboxes = new Vector();

        getContentPane().add(createMainPage(), BorderLayout.PAGE_START);
        getContentPane().add(createButtons(), BorderLayout.PAGE_END);
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel();
        JButton cancelBn = new JButton("Cancel");
        JButton okBn = new JButton("OK");

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(cancelBn);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okBn);

        okBn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {

                        for (JCheckBox checkbox : checkboxes) {
                            if (checkbox.isSelected()) {
                                independenciesToCompare.add(checkbox.getText());
                            }
                        }

                        if (independenciesToCompare.size() > 1)
                            table.highlightIndependenciesDifferences(independenciesToCompare);
                        dispose();
                    }
                }
        );

        cancelBn.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }
        );
        return buttonPanel;
    }

    private JPanel createMainPage() {
        JPanel mainPage = new JPanel();
        JLabel instructions = new JLabel("Select the independencies to compare:");
        JScrollPane scrollPane = new JScrollPane(createCheckBoxes());

        mainPage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPage.setLayout(new BoxLayout(mainPage, BoxLayout.PAGE_AXIS));

        mainPage.add(instructions);
        mainPage.add(scrollPane);

        instructions.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(300, 250));
        scrollPane.setBackground(Color.white);

        return mainPage;
    }

    private JPanel createCheckBoxes() {
        JPanel listPanel = new JPanel();
        String[] names = getHypAndSampleNames();

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBackground(Color.white);

        IndependenciesTableModel model = getTableModel();
        JCheckBox checkbox;

        for (int i = 0; i < names.length; i++) {
            if (model.getIndependenceColumnType(i) != IndependenciesTableModel.VARIABLE_NAME_COLUMN
                    && model.getIndependenceColumnType(i) != IndependenciesTableModel.SAMPLE_GUESS_COLUMN
                    && model.getIndependenceColumnType(i) != IndependenciesTableModel.HYP_GUESS_COLUMN) {

                checkbox = new JCheckBox(names[i]);
                checkbox.setBackground(Color.white);

                if (model.getValueAt(0, i) == null) {
                    checkbox.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "No experiments exist");
                }

                listPanel.add(checkbox);
                checkboxes.add(checkbox);
            }
        }

        return listPanel;
    }

    private IndependenciesTableModel getTableModel() {
        return (IndependenciesTableModel) table.getModel();
    }

    private String[] getHypAndSampleNames() {
        String[] names = new String[getTableModel().getColumnCount()];

        for (int i = 0; i < names.length; i++) {
            names[i] = getTableModel().getColumnName(i);
        }

        return names;
    }

}
