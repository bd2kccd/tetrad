package edu.cmu.causality;

import edu.cmu.causality.sample.BayesSample;

/**
 * This table model provides data for the bayes sample frequencies table.
 */
public class BayesSampleFrequenciesTableModel extends AbstractSampleTable {
    final BayesSample sample;
    final String experimentName;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     *
     * @param seed sample seed.
     */
    public BayesSampleFrequenciesTableModel(CausalityLabModel causalityLabModel, SampleSeed seed) {
        this.causalityLabModel = causalityLabModel;
        sample = new BayesSample(CausalityLabModel.getModel().getCorrectBayesIm(), causalityLabModel.getExperiment(seed
                .getExpName()), seed.getSampleSize(), seed
                .getSeed());
        experimentName = seed.getExpName();
    }

    /**
     * @return the column name of the column with the given column index.
     */
    public String getColumnName(int col) {
        return sample
                .getSampleFrequenciesColumnNames(causalityLabModel.getExperiment(experimentName))[col];
    }

    /**
     * @return the number of rows.
     */
    public int getRowCount() {
        return sample
                .getNumSampleValueCombinations(causalityLabModel.getExperiment(experimentName));
    }

    /**
     * @return the number of columns.
     */
    public int getColumnCount() {
        return sample
                .getSampleFrequenciesColumnNames(causalityLabModel.getExperiment(experimentName)).length;
    }

    /**
     * @return the value at the given row and column.
     */
    public Object getValueAt(int row, int column) {
        if (column < getColumnCount() - 1) {
            return sample.getSampleCaseFrequencyCombination(row
            )[column];
        } else {
            return sample.getSampleCaseFrequency(row);
        }
    }

    /**
     * @return the value of the longest values to calculate the column size.
     */
    public Object[] getLongestValues() {
        int i;
        String[] varNames = causalityLabModel.getExperiment(experimentName)
                .getNamesOfStudiedVariables();
        Object[] longestValues = new Object[getColumnCount()];

        for (i = 0; i < varNames.length; i++) {
            longestValues[i] = causalityLabModel.getLongestVariableParameter(varNames[i]);
        }
        longestValues[i] = 8888;
        return longestValues;
    }
}
