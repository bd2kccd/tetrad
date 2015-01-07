package edu.cmu.causalityApp.finances;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.event.*;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.util.Misc;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class is the container frame for showing the financial transaction history.
 *
 * @author adrian tang
 */
public class FinancesHistoryEditor extends AbstractEditor
        implements ModelChangeListener {
    private static final Color headerBackground = Misc.lightAqua;
    private static final Color gridColor = new Color(240, 240, 240);

    /**
     * Unique name to this window.
     */
    public static final String MY_NAME = "Finances History";

    /**
     * Constructor.
     *
     * @param parent CausalitLabPanel
     */
    public FinancesHistoryEditor(InternalFrameListener parent) {
        super(MY_NAME);

        getContentPane().add(getDataTable());
        addInternalFrameListener(parent);

        if (CausalityLabModel.getModel().isLimitResource())
            CausalityLabModel.getModel().addModelChangeListener(this);
    }

    /**
     * @return the unique id name of this window.
     */
    public String getEditorName() {
        return MY_NAME;
    }

    /**
     * Update the view when the finances are changed.
     */
    public void financeChanged() {
        getContentPane().removeAll();
        getContentPane().add(getDataTable());
    }

    private JComponent getDataTable() {
        JTable financeHistoryTable = new JTable(CausalityLabModel.getModel()
                .getFinanceHistoryTableModel());
        financeHistoryTable.setGridColor(gridColor);
        financeHistoryTable.setIntercellSpacing(new Dimension(1, 0));
        financeHistoryTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());

        return new JScrollPane(financeHistoryTable);
    }

    /**
     * Inner class to render the header of the JTable
     */
    private class CustomHeaderRenderer extends JLabel implements TableCellRenderer {
        public CustomHeaderRenderer() {
            super();
            setBackground(headerBackground);
            setHorizontalAlignment(JLabel.CENTER);
            setBorder(BorderFactory.createLineBorder(Color.white, 2));
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
        (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.setText(value.toString());
            return this;
        }
    }


    public void hypothesisChanged(HypothesisChangedEvent hcEvent) {
    }

    public void experimentChanged(ExperimentChangedEvent ecEvent) {
    }

    public void sampleChanged(SampleChangedEvent scEvent) {
    }
}
