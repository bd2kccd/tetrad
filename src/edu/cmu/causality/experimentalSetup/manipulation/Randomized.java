package edu.cmu.causality.experimentalSetup.manipulation;

/**
 * One of the typesafe enums in Manipulation. Randomization indicates that the
 * variable is assigned random values across members of the sample.
 *
 * @author Matthew Easterday
 */
public class Randomized extends Manipulation {

    /**
     * Constructor.
     */
    public Randomized() {
        super(ManipulationType.RANDOMIZED);
    }

}
