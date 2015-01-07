package edu.cmu.causalityApp.dataEditors.sampleEditor;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;

/**
 * This is the sample frequency table that will be displayed in each tab in the
 * editor.
 *
 * @author mattheweasterday
 */
class SampleFrequenciesTable extends SampleTable {

    /**
     * Constructor.
     */
    public SampleFrequenciesTable(AbstractTableModel model) {
        super(model);

        this.getColumnModel().getColumn(this.getColumnCount() - 1).setCellRenderer(new NumberRenderer());
    }

    /**
     * Inner class to render the numbers in each cell.
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
         * Set the value at this cell.
         */
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = new DecimalFormat();
                formatter.setMaximumIntegerDigits(2);
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
}
