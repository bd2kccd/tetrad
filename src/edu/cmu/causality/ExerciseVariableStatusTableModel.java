package edu.cmu.causality;

import edu.cmu.tetrad.graph.Node;

import javax.swing.table.AbstractTableModel;

/**
 * Public inner class to provide a table model for the variable status table
 * in the instruction editor
 */
public class ExerciseVariableStatusTableModel extends AbstractTableModel {
    private final String[] varNames;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     */
    public ExerciseVariableStatusTableModel(CausalityLabModel causalityLabModel) {
        this.causalityLabModel = causalityLabModel;
        varNames = causalityLabModel.getExperimentalSetupVariableNames();
    }

    /**
     * @return the number of rows.
     */
    public int getRowCount() {
        return varNames.length;
    }

    /**
     * @return the number of columns.
     */
    public int getColumnCount() {
        return 3;
    }

    /**
     * @return the name of the given column.
     */
    public String getColumnName(int col) {
        // todo: translation
        if (col == 0) {
            return "Name";
        } else if (col == 1) {
            return "Values";
        } else if (col == 2) {
            return "Intervene on?";
        } else {
            throw new IllegalArgumentException(
                    "FinanceHistoryTableModel: No such column number "
                            + col);
        }
    }

    /**
     * @return the value at the given row and column.
     */
    public Object getValueAt(int row, int col) {
        String varName = varNames[row];

        if (col == 0) {
            return varName;
        } else if (col == 1) {

            if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
                Node node = causalityLabModel.getCorrectGraphBayesPmCopy().getDag().getNode(
                        varName);

                String values = "";
                for (int i = 0; i < causalityLabModel.getCorrectGraphBayesPmCopy()
                        .getNumCategories(node); i++) {
                    if (values.equals(""))
                        values += causalityLabModel.getCorrectGraphBayesPmCopy().getCategory(
                                node, i);
                    else
                        values += (", " + causalityLabModel.getCorrectGraphBayesPmCopy()
                                .getCategory(node, i));
                }
                return values;
            } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
                return "Continuous";
            }

        } else if (col == 2) {
            return causalityLabModel.getVariableIntervenable(varName) ? "Yes" : "No";
        } else {
            throw new IllegalArgumentException(
                    "FinanceHistoryTableModel: No such column number "
                            + col);
        }

        return null;
    }
}
