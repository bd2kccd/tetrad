package edu.cmu.causality2.model.sample;

import edu.cmu.causality2.model.experiment.ExperimentalSetup;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.IM;

/**
 * Stores a data set, possibly as an IM with a sample size and random seed.
 *
 * @author Joseph Ramsey
 */
public class Sample {
    private DataSet dataSet;
    private IM im;
    private ExperimentalSetup experimentalSetup = null;
    private long seed;
    private int sampleSize;

    public Sample(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public Sample(BayesIm im, long seed, int sampleSize) {
        this.im = im;
        this.seed = seed;
        this.sampleSize = sampleSize;
    }

    public Sample(SemIm im, long seed, int sampleSize) {
        this.im = im;
        this.seed = seed;
        this.sampleSize = sampleSize;
    }

    public DataSet getDataSet() {
        if (dataSet != null) {
            return dataSet;
        }
        else if (im instanceof BayesIm) {
            return ((BayesIm) im).simulateData(sampleSize, seed, false);
        }
        else if (im instanceof SemIm) {
            return ((SemIm) im).simulateData(sampleSize, seed, false);
        }

        throw new IllegalStateException();
    }

    public IM getIm() {
        return im;
    }

    public ExperimentalSetup getExperimentalSetup() {
        return experimentalSetup;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        DataSet dataSet = getDataSet();

        buf.append("Sample " + dataSet.getNumRows() + " x " + dataSet.getNumColumns());

        return buf.toString();
    }
}
