package edu.cmu.causality;

import edu.cmu.causality.sample.SemSample;

/**
 * This table model provides data for the SEM sample frequencies table.
 */
public class SemSampleFrequenciesTableModel extends AbstractSampleTable {
    final SemSample sample;
    final String experimentName;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     *
     * @param seed sample seed.
     */
    public SemSampleFrequenciesTableModel(CausalityLabModel causalityLabModel, SampleSeed seed) {
        this.causalityLabModel = causalityLabModel;
        sample = new SemSample(CausalityLabModel.getModel().getCorrectSemIm(),
                causalityLabModel.getExperiment(seed.getExpName()), seed.getSampleSize(),
                seed.getSeed());
        experimentName = seed.getExpName();
    }

    /**
     * @return the name of the column with the given index.
     */
    public String getColumnName(int col) {
        return (col == 0) ? " " : causalityLabModel.getExperiment(experimentName)
                .getNamesOfStudiedVariables()[col - 1];
    }

    /**
     * @return the number of rows.
     */
    public int getRowCount() {
        return sample.getRowCount();
    }

    /**
     * @return the number of columns.
     */
    public int getColumnCount() {
        return sample.getColumnCount(causalityLabModel.getExperiment(experimentName));
    }

    /**
     * @return the value at the given row and column.
     */
    public Object getValueAt(int row, int column) {
        return sample
                .getValueAt(row, column);
    }

    /**
     * @return the value of the longest values to calculate the column size.
     */
    public Object[] getLongestValues() {
        int i;
        String[] varNames = causalityLabModel.getExperiment(experimentName)
                .getNamesOfStudiedVariables();
        Object[] longestValues = new Object[getColumnCount()];

        longestValues[0] = "";
        for (i = 0; i < varNames.length; i++) {
            if (varNames[i].length() > ((String) longestValues[0]).length()) {
                longestValues[0] = varNames[i];
            }
        }
        for (int j = 1; j < longestValues.length; j++) {
            longestValues[j] = 88888888;
        }
        return longestValues;
    }
}
