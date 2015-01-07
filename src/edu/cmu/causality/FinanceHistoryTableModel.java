package edu.cmu.causality;

import edu.cmu.causality.finances.FinanceTransaction;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;

/**
 * Public inner class to provide a table model for the Finance History
 * table.
 */
public class FinanceHistoryTableModel extends AbstractTableModel {

    final private NumberFormat nf;
    private CausalityLabModel causalityLabModel;

    {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumFractionDigits(0);
        nf1.setMaximumFractionDigits(0);
        nf = nf1;
    }

    /**
     * Constructor.
     */
    public FinanceHistoryTableModel(CausalityLabModel causalityLabModel) {
        this.causalityLabModel = causalityLabModel;
    }

    /**
     * @return the number of rows.
     */
    public int getRowCount() {
        return causalityLabModel.getMoneyTransactions().size() + 1;
    }

    /**
     * @return the number of columns.
     */
    public int getColumnCount() {
        return 5;
    }

    /**
     * @return the name of the given column.
     */
    public String getColumnName(int col) {

        // todo: translation
        if (col == 0) {
            return "Expt Name";
        } else if (col == 1) {
            return "Sample Name";
        } else if (col == 2) {
            return "Sample Size";
        } else if (col == 3) {
            return "Expenses";
        } else if (col == 4) {
            return "Remaining Balance";
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
        if (row == 0) {
            if (col == 4) {
                return nf.format(causalityLabModel.getTotalInitialBalance());
            } else {
                return "";
            }
        }

        FinanceTransaction trans = causalityLabModel.getMoneyTransactions()
                .get(row - 1);
        if (col == 0) {
            return trans.getExpName();
        } else if (col == 1) {
            return trans.getSampleName();
        } else if (col == 2) {
            return Integer.toString(trans.getSampleSize());
        } else if (col == 3) {
            return nf.format(trans.getExpenses());
        } else if (col == 4) {
            return nf.format(trans.getBalance());
        } else {
            throw new IllegalArgumentException(
                    "FinanceHistoryTableModel: No such column number "
                            + col);
        }

    }
}
