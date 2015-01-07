package edu.cmu.causalityApp.dataEditors.sampleEditor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;

/**
 * @author mattheweasterday
 *         Date: Jun 7, 2004
 *         Time: 8:47:16 PM
 *         To change this template use File | Settings | File Templates.
 *         <p/>
 *         This is the SEM sample frequency table that will be displayed in each tab in the
 *         editor.
 */
public class SampleFrequenciesTableSem extends SampleTable {


    /**
     * Constructor.
     */
    public SampleFrequenciesTableSem(AbstractTableModel model) {
        super(model);
        for (int i = 1; i < this.getColumnCount(); i++) {
            this.getColumnModel().getColumn(i).setCellRenderer(new NumberRenderer());
        }
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
                //formatter.setMaximumIntegerDigits(4);
                formatter.setMaximumFractionDigits(2);
                formatter.setMinimumFractionDigits(2);
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            setText((value == null) ? "" : formatter.format(value));
        }
    }
}