package edu.cmu.causalityApp.dataEditors.independenciesEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * This class describes how each independency cell is rendered.
 *
 * @author Adrian Tang
 */
class CellRenderer extends DefaultTableCellRenderer {

    /**
     * Indicates if this cell is selected.
     */
    boolean isSelected;

    /**
     * The icons for the independent, dependent and none statuses.
     */
    Icon independentIcon;
    Icon dependentIcon;
    Icon noneIcon;


    static final Color noneColor = new Color(220, 220, 200);
    //noneIconSelected = new IndependenceCellRenderer.IndependenceIcon(noneSelectedColor, noneSelectedColor),

    /**
     * Constructor. Set any labels in the cell to be center-aligned.
     */
    CellRenderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * Matt: one thing this does is make the background of some of the
     * independencies cells pink (which i disabled)
     *
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return the rendered cell.
     */
    public Component getTableCellRendererComponent
    (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.isSelected = isSelected;

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        /*
		if (table.getModel() instanceof SortableTableModel) {
			SortableTableModel sortableTableModel = (SortableTableModel) table.getModel();
			if (sortableTableModel.getModel() instanceof IndepTableModel) {
				IndepTableModel model = (IndepTableModel) sortableTableModel.getModel();


				final int modelColumn = table.convertColumnIndexToModel(column);
				int correct = model.getCorrectAt(row, modelColumn);

				if (isSelected)
					setBackground(table.getSelectionBackground());
				else
					setBackground(Color.white);

                String tableName = table.getName();
                //System.out.println("Table name: " + tableName);

                // todo: add a check for a predicted indep table here?? 02/24
				if ((correct == -1) && (!tableName.startsWith("Predicted"))) {
					Color background = getBackground();
					Color color = mixColor(background);

					//setBackground(color);
				}
//				else if (correct == 0)
//					setBackground(Color.white);
//				else if (correct == 1) setBackground(Color.white);
			}
		}
        */

        return this;
    }

/*	private Color mixColor (Color background) {
		Color color = new Color ((background.getRed() + wrongColor.getRed())/2,
		(background.getGreen() + wrongColor.getGreen())/2,
		(background.getBlue() + wrongColor.getBlue())/2);
		return color;
	}*/


    /**
     * Set the value for this cell.
     *
     * @param value
     */
    public void setValue(Object value) {
        if (value == null) {
            setIcon(noneIcon);
        } else if (!(Boolean) value) {
            setIcon(dependentIcon);
        } else {
            setIcon(independentIcon);
        }
        /*else if (value == null){ //Independence.none) {
			setIcon(isSelected ? noneIconSelected : noneIcon);
        } else {
        }*/
    }

}
