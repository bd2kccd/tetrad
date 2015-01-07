package edu.cmu.causality;

import edu.cmu.causality.sample.BayesSample;

/**
 * This table model provides the data needed to create the sample cases
 * table.
 */
public class SampleCasesTableModel extends AbstractSampleTable {
    final BayesSample sample;
    final String experimentName;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     *
     * @param seed sample seed.
     */
    public SampleCasesTableModel(CausalityLabModel causalityLabModel, SampleSeed seed) {
        this.causalityLabModel = causalityLabModel;
        sample = new BayesSample(CausalityLabModel.getModel().getCorrectBayesIm(), causalityLabModel.getExperiment(seed
                .getExpName()), seed.getSampleSize(), seed
                .getSeed());
        experimentName = seed.getExpName();
    }

    /**
     * @return the name of the column with the given column number.
     */
    public String getColumnName(int col) {
        return sample.getSampleCaseColumnNames()[col];
    }

    /**
     * @return the number of rows in the table.
     */
    public int getRowCount() {
        return sample.getNumSampleCases();
    }

    /**
     * @return the number of columns.
     */
    public int getColumnCount() {
        return sample
                .getSampleCaseColumnNames().length;
    }

    /**
     * @return the value at the given row and column.
     */
    public Object getValueAt(int row, int column) {
        return sample.getValueCombination(row, causalityLabModel.getExperiment(experimentName))[column];
    }

    /**
     * @return the longest values to calculate the column size.
     */
    public Object[] getLongestValues() {
        String[] varNames = causalityLabModel.getExperiment(experimentName)
                .getVariableNames();
        Object[] longestValues = new Object[getColumnCount() + 1];
        longestValues[0] = 8888;
        for (int i = 0; i < varNames.length; i++) {
            longestValues[i + 1] = causalityLabModel.getLongestVariableParameter(varNames[i]);
        }
        return longestValues;
    }

}
