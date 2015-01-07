package edu.cmu.causalityApp.dataEditors.sampleEditor;

import edu.cmu.causality.AbstractSampleTable;
import edu.cmu.causalityApp.util.Misc;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

/**
 * This is the superclass for all the tables used in the sample editor.
 *
 * @author mattheweasterday
 */
abstract class SampleTable extends JTable {

    /**
     * The background color of the table header.
     */
    private static final Color headerBackground = Misc.lightAqua;

    /**
     * Constructor.
     */
    SampleTable(AbstractTableModel model) {
        super(model);
        installDefaults();
        initColumnSizes(this);
    }

    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table) {
        AbstractSampleTable model = (AbstractSampleTable) getModel();
        TableColumn column;
        Component comp;
        int headerWidth;
        int cellWidth;
        Object[] longValues = model.getLongestValues();
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < model.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                    getTableCellRendererComponent(
                            table, longValues[i],
                            false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }


    private void installDefaults() {
        //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setGridColor(new Color(240, 240, 240));
        getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        //getTableHeader().addMouseMotionListener(new JCausalityTable.HeaderTooltipHandler());

        setIntercellSpacing(new Dimension(1, 0));

        //setColumnModel(new CustomColumnModel(2));
        setModel(getModel());

    }


    class CustomHeaderRenderer extends JLabel implements TableCellRenderer {
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
}
