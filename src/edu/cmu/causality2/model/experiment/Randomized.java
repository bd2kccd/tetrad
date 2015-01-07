package edu.cmu.causality2.model.experiment;

import edu.cmu.tetrad.util.dist.Distribution;

/**
 * One of the typesafe enums in Manipulation. Randomization indicates that the
 * variable is assigned random values across members of the sample.
 *
 * @author Matthew Easterday
 */
public class Randomized extends Manipulation {

    private final Distribution distribution;

    /**
     * Constructor.
     */
    public Randomized(Distribution distribution) {
        super(ManipulationType.RANDOMIZED);
        this.distribution = distribution;
    }

    public Distribution getDistribution() {
        return distribution;
    }
}
