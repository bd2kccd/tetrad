package edu.cmu.causalityApp.dataEditors.populationEditor;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * This class describes the population JTable for Bayes IM.
 *
 * @author mattheweasterday
 */
class PopulationTable extends JTable {

    /**
     * Background color of the header.
     */
    private static final Color headerBackground = new Color(220, 233, 233);

    /**
     * Constructor.
     */
    public PopulationTable(AbstractTableModel model) {
        super(model);

        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setGridColor(new Color(240, 240, 240));
        getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());

        setIntercellSpacing(new Dimension(1, 0));

        if (this.getColumnCount() - 1 >= 0) {
            this.getColumnModel().getColumn(this.getColumnCount() - 1).setCellRenderer(new NumberRenderer());
        }
    }

    /**
     * Inner class used to render the view of the custom header.
     */
    class CustomHeaderRenderer extends JLabel implements TableCellRenderer {

        /**
         * Constructor.
         */
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

            }
            setText((value == null) ? "" : formatter.format(value));

        }

    }
}
