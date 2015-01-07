package edu.cmu.causalityApp.instructions;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;

/**
 * This class describes the window containing the instructions for the exercise
 * when an exercise is first loaded.
 *
 * @author adrian tang
 */
public class InstructionsEditor extends AbstractEditor {

    private static final Color headerBackground = new Color(180, 180, 180);
    private static final Color gridColor = new Color(140, 140, 140);

    /**
     * Unique ID name for this window.
     */
    public static final String MY_NAME = "Instructions";

    /**
     * Constructor.
     */
    public InstructionsEditor() {
        super(MY_NAME);
        getContentPane().add(createMainPane());
    }

    /**
     * @return unique id name for this window
     */
    public String getEditorName() {
        return MY_NAME;
    }


    private JPanel createMainPane() {
        CausalityLabModel model = CausalityLabModel.getModel();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Variables"));
        b.add(Box.createHorizontalGlue());

        panel.add(b);

        JTable exerciseVariableStatusTable = new JTable(
                model.getExerciseVariableStatusTableModel());
        exerciseVariableStatusTable.setBackground(this.getBackground());
        exerciseVariableStatusTable.setColumnSelectionAllowed(false);
        exerciseVariableStatusTable.setRowSelectionAllowed(false);
        exerciseVariableStatusTable.setGridColor(gridColor);
        exerciseVariableStatusTable.getTableHeader().setDefaultRenderer(
                new CustomHeaderRenderer());

//            exerciseVariableStatusTable.setPreferredScrollableViewportSize(new Dimension(
//                    (int)exerciseVariableStatusTable.getPreferredSize().getWidth(),
//                    (int)exerciseVariableStatusTable.getPreferredSize().getHeight()+10));

        JScrollPane scrollPane = new JScrollPane(exerciseVariableStatusTable);
        scrollPane.setPreferredSize(new Dimension(
                (int) exerciseVariableStatusTable.getPreferredSize().getWidth(),
                (int) exerciseVariableStatusTable.getPreferredSize().getHeight() + 18));

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(20));

        if (CausalityLabModel.getModel().isLimitResource()) {
            NumberFormat nf1 = NumberFormat.getInstance();
            nf1.setMinimumFractionDigits(0);
            nf1.setMaximumFractionDigits(0);

            JLabel label1 = new JLabel("Total Money: " + nf1.format(
                    model.getTotalInitialBalance()));
            JLabel label2 = new JLabel("Cost per subject (Observation): " +
                    nf1.format(model.getCostPerObs()));
            JLabel label3 = new JLabel("Cost per subject (Intervention): " +
                    nf1.format(model.getCostPerIntervention()));
            panel.add(label1);
            panel.add(label2);
            panel.add(label3);
            panel.add(Box.createVerticalStrut(10));
        }

        JTextArea textArea = new JTextArea(model.getInstructions());
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setRows(10);
        textArea.setColumns(30);

        JScrollPane scrollPane2 =
                new JScrollPane(textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        panel.add(scrollPane2);

        return panel;
    }


    /**
     * Inner class to render the header of the JTable
     */
    private class CustomHeaderRenderer extends JLabel implements TableCellRenderer {
        public CustomHeaderRenderer() {
            super();
            setBackground(headerBackground);
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true);
        }

        /**
         * Returns the default table cell renderer.
         *
         * @param table      the <code>JTable</code>
         * @param value      the value to assign to the cell at <code>[row, column]</code>
         * @param isSelected true if cell is selected
         * @param hasFocus   true if cell has focus
         * @param row        the row of the cell to render
         * @param column     the column of the cell to render
         * @return the default table cell renderer
         */
        public Component getTableCellRendererComponent
        (JTable table, Object value, boolean isSelected, boolean hasFocus,
         int row, int column) {
            this.setText(value.toString());
            return this;
        }
    }
}
