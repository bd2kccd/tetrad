package edu.cmu.causality.sample;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.causality.population.CorrectManipulatedGraphSemIm;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.sem.SemIm;

/**
 * This describes the SEM sample.
 *
 * @author mattheweasterday
 */
public class SemSample implements Sample {
    private final DataSet data;

    /**
     * This gets the manipulated SEM IM graph given the correct SEM IM and the
     * experimental setup. The returned SEM IM graph uses the implied covariance metrix directly to
     * simulate data, instead of going tier by tier. It should work for cyclic
     * graphs as well as acyclic graphs.
     *
     * @param correctSemIm the correct SEM IM
     * @param sampleSize   The non-negative sample size
     */
    public SemSample(SemIm correctSemIm,
                     ExperimentalSetup experiment,
                     int sampleSize,
                     long sampleSeed) {
        SemIm correctManipulatedGraphIM = CorrectManipulatedGraphSemIm.createIm(correctSemIm, experiment);
        data = correctManipulatedGraphIM.simulateData(sampleSize, sampleSeed, false);
    }

    /**
     * @return the associated dataset of this SEM sample.
     */
    public DataSet getDataSet() {
        return data;
    }

    /**
     * @return the number of rows in the dataset.
     */
    public int getRowCount() {
        return data.getNumRows();//getMaxRowCount();
    }

    /**
     * @return the number of columns in the dataset table. Need to add 1 for the
     *         count.
     */
    public int getColumnCount(VariablesStudied studiedVariables) {
        return studiedVariables.getNumVariablesStudied() + 1;
    }

    /**
     * @return the value at the given row and column of the dataset.
     */
    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return row + 1;
        }

        if (column > 0) {
            return data.getObject(row, column - 1);
        } else {
            throw new IllegalArgumentException("SemSample: Invalid column value = " + column);
        }
    }

    /**
     * @return the value at a given row given a variable being set.
     */
    public Object getValueAtRowGivenColumnName(int row, String varName) {
        return data.getDouble(row, data.getColumn(data.getVariable(varName)));
    }
}
