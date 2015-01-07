package edu.cmu.causality2.model.experiment;

import edu.cmu.causality2.model.sample.Sample;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.util.IM;

/**
 * Stores an experiment.
 */
public class Experiment {
    private IM im;
    private ExperimentalSetup experimentalSetup;
    private IM manipulatedim;

    public Experiment(IM im, ExperimentalSetup setup) {
        if (im == null) throw new NullPointerException();
        this.im = im;
        this.experimentalSetup = setup;

        if (im instanceof BayesIm) {
            manipulatedim = CorrectManipulatedGraphBayesIm.createIm((BayesIm) im, experimentalSetup);
        }
        else if (im instanceof SemIm) {
            manipulatedim = CorrectManipulatedGraphSemIm.createIm((SemIm) im, experimentalSetup);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public Experiment(Experiment experiment) {
        experiment.im = im;
        experiment.experimentalSetup = experimentalSetup;
        experiment.manipulatedim = manipulatedim;
    }

    public IM getIm() {
        return this.im;
    }

    public Sample drawSample(int sampleSize) {
        if (manipulatedim instanceof BayesIm) {
            Sample sample = new Sample(new MlBayesIm((BayesIm) manipulatedim), System.currentTimeMillis(), sampleSize);
            return sample;
        }
        else if (manipulatedim instanceof SemIm) {
            Sample sample = new Sample(new SemIm((SemIm) manipulatedim), System.currentTimeMillis(), sampleSize);
            return sample;
        }

        throw new IllegalArgumentException();
    }

    public ExperimentalSetup getExperimentalSetup() {
        return experimentalSetup;
    }

    public IM getManipulatedIm() {
        return manipulatedim;
    }
}
