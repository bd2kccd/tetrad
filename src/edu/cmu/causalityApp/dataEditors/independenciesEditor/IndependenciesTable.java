package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import edu.cmu.causality.IndependenciesTableModel;
import edu.cmu.causalityApp.dataEditors.HypotheticalManipulatedGraphIconMaker;
import edu.cmu.causalityApp.util.ImageUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class is used to represent the Independencies Table.
 * <p/>
 * Adrian: The trick to highlighting any differences when user wants to compare between
 * more than one column is that the row with differences will be "selected",
 * thereby creating a highlighted background to show the difference.
 *
 * @author mattheweasterday
 */
class IndependenciesTable extends JTable {

    /**
     * The grid color of the independencies table.
     */
    private static final Color gridColor = new Color(240, 240, 240);

    /**
     * Independent label.
     */
    final static String INDEPENDENT = "Independent";

    /**
     * Dependent label.
     */
    final static String DEPENDENT = "Dependent";

    /* Adrian: This hashmap keeps track of the original column index of each table column object
     * so that when the user swap the columns around, the correct original column
     * index can be retrieved
     */
    private final HashMap<TableColumn, Integer> col2Index;

    /**
     * Constructor.
     */
    public IndependenciesTable(
            IndependenciesTableModel model,
            String expName,
            HypotheticalManipulatedGraphIconMaker iconMaker,
            boolean isPopulationHidden,
            boolean isStudentGuessEnabled) {

        super(model);

        TableColumn tableColumn;
        col2Index = new HashMap<TableColumn, Integer>();

        // Selection Mode needs to be multiple interval to accommodate multiple
        // selections (multiple differences when independencies are compared)
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        setGridColor(gridColor);
        setIntercellSpacing(new Dimension(1, 0));
        setModel(getModel());

        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
            tableColumn = getColumnModel().getColumn(columnIndex);
            int type = model.getIndependenceColumnType(columnIndex);

            if (columnIndex >= 3) {

                if (type == IndependenciesTableModel.SAMPLE_COLUMN) {
                    tableColumn.setCellRenderer(new SampleIndependenciesCellRenderer(isStudentGuessEnabled));
                } else {
                    tableColumn.setCellRenderer(new IndependenceCellRenderer(isStudentGuessEnabled));
                }
            }


            if (type == IndependenciesTableModel.VARIABLE_NAME_COLUMN) {


                tableColumn.setHeaderRenderer(new HeaderRenderer(null));

            } else if (type == IndependenciesTableModel.POPULATION_COLUMN) {
                makeHeader(tableColumn, "population.gif");
                if (isPopulationHidden)
                    tableColumn.setCellRenderer(new HiddenIndependenceCellRenderer());

            } else if (type == IndependenciesTableModel.SAMPLE_COLUMN) {
                makeHeader(tableColumn, "sample.gif");

            } else if (type == IndependenciesTableModel.SAMPLE_GUESS_COLUMN) {
                makeHeader(tableColumn, "sample_guess.gif");
                tableColumn.setCellEditor(getEditor());
                tableColumn.setMaxWidth(85);
                tableColumn.setCellRenderer(new GuessesTableCellRenderer());
                col2Index.put(tableColumn, columnIndex);

            } else if (type == IndependenciesTableModel.HYPOTHESIS_COLUMN) {
                if (model.getNumOfHypGraph() <= 12) {
                    ImageIcon icon = iconMaker.getHypotheticalManipulatedGraphIcon(expName, model.getHypotheticalGraphGraphNameForColumn(columnIndex));
                    tableColumn.setHeaderRenderer(new HeaderRenderer(icon));
                } else {
                    tableColumn.setHeaderRenderer(new HeaderRenderer(null));
                }
            } else if (type == IndependenciesTableModel.HYP_GUESS_COLUMN) {
                makeHeader(tableColumn, "hyp_manip_graph_guess.gif");
                tableColumn.setCellEditor(getEditor());
                tableColumn.setMaxWidth(85);
                tableColumn.setCellRenderer(new GuessesTableCellRenderer());
                col2Index.put(tableColumn, columnIndex);
            } else {
                //error
            }
        }
        initColumnSizes(this);
    }

    /* public boolean isCellEditable(int row, int col){
        return ((IndependenciesTableModel) getModel()).isCellEditable(row, col) ;
    }*/

    private void makeHeader(TableColumn col, String img) {

        Image image = ImageUtils.getImage(this, img);
        //Misc.makeIcon(this.getClass(), img).getImage();;
        ImageIcon icon = new ImageIcon(image);
        col.setHeaderRenderer(new HeaderRenderer(icon));
    }

    /**
     * Set the value of the cell at given row and column.
     */
    public void setValueAt(Object value, int row, int col) {
        IndependenciesTableModel model = (IndependenciesTableModel) getModel();
        TableColumn tableColumn = getColumnModel().getColumn(col);
        int colIndex = col2Index.get(tableColumn);


        int type = model.getIndependenceColumnType(col);
        if ((type == IndependenciesTableModel.HYP_GUESS_COLUMN) ||
                (type == IndependenciesTableModel.SAMPLE_GUESS_COLUMN)) {

            boolean val = INDEPENDENT.equals(value);


            (new SetIndependenceCommand(this, val, row, colIndex)).doIt();

        } else {
            setValue(model, value, row, colIndex);
        }
    }

    private void setValue(IndependenciesTableModel model, Object value, int row, int colIndex) {
        if (INDEPENDENT.equals(value)) {
            model.setIndependence(Boolean.TRUE, row, colIndex);
        } else if (DEPENDENT.equals(value)) {
            model.setIndependence(false, row, colIndex);
        } else {
            //error
        }
        repaint();
    }

    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table) {
        IndependenciesTableModel model = (IndependenciesTableModel) getModel();
        TableColumn column;
        Component comp;
        int headerWidth;
        int cellWidth;
        TableCellRenderer headerRenderer;

        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < model.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);

            headerRenderer = column.getHeaderRenderer();

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            cellWidth = 0;

            column.setMinWidth(Math.max(headerWidth + 10, cellWidth + 10));
            column.setPreferredWidth(Math.max(headerWidth + 10, cellWidth + 10));
        }

    }

    private DefaultCellEditor getEditor() {
        JComboBox box = new JComboBox();
        box.addItem(INDEPENDENT);
        box.addItem(DEPENDENT);
        return new DefaultCellEditor(box);
    }

    /**
     * Given the column names selected, process through the data and find rows
     * with differences in the independencies. Rows with differences are selected
     * and highlighted.
     *
     * @param columnNames A vector of Strings of the column names to compare
     */
    public void highlightIndependenciesDifferences(Vector columnNames) {
        Vector<Integer> differenceRowIndex = ((IndependenciesTableModel) getModel()).
                getRowIndexWithIndepDifferences(columnNames);

        getSelectionModel().clearSelection();
        for (Integer aDifferenceRowIndex : differenceRowIndex) {
            getSelectionModel().addSelectionInterval(aDifferenceRowIndex, aDifferenceRowIndex);
        }
    }
}
