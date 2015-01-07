package edu.cmu.causality.sample;

import edu.cmu.tetrad.data.DataSet;

/**
 * Represents a sample drawn from a population over
 * continuous or discrete variables. The mixed case
 * (of continuous and discrete variables) is not
 * considered.
 *
 * @author mattheweasterday
 */
public interface Sample {

    /**
     * This method returns a <code>RectangularDataSet</code>.
     * This dataset is a simulated sample.
     * NOTE: RectangularDataSet is deprecated in the latest
     * Tetrad.
     *
     * @return RectangularDataSet. This dataset is a simulated sample.
     * @see edu.cmu.causality.sample.BayesSample BayesSample
     * @see edu.cmu.causality.sample.SemSample SemSample
     */
    public DataSet getDataSet();
}
