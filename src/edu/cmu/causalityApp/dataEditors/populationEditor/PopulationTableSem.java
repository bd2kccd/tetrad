package edu.cmu.causalityApp.dataEditors.populationEditor;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * This class describes the population JTable for SEM IM.
 *
 * @author mattheweasterday
 */
class PopulationTableSem extends JTable {

    /**
     * Constructor.
     */
    public PopulationTableSem(AbstractTableModel model) {
        super(model);

        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setGridColor(new Color(240, 240, 240));

        setIntercellSpacing(new Dimension(1, 0));

        for (int i = 1; i < this.getColumnCount(); i++) {
            this.getColumnModel().getColumn(i).setCellRenderer(new NumberRenderer());
        }
    }

    /**
     * Inner class to render the numbers in the population table.
     */
    class NumberRenderer extends DefaultTableCellRenderer {
        DecimalFormat formatter;

        /**
         * Constructor.
         */
        public NumberRenderer() {
            super();
        }

        /**
         * Set the value at that cell.
         */
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = new DecimalFormat();
                formatter.setMaximumIntegerDigits(2);
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            setText((value == null) ? "" : formatter.format(value));

        }

    }
}

