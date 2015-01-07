package edu.cmu.causality.experimentalSetup.manipulation;

/**
 * One of the typesafe enums in Manipulation.  Latent variables are unobserved
 * but still exist in the correct graph.
 *
 * @author Matthew Easterday
 */
public class Latent extends Manipulation {

    /**
     * Constructor.
     */
    public Latent() {
        super(ManipulationType.LATENT);
    }

    /**
     * copy constructor.
     */
    public Latent(Latent latent) {
        super(latent);
    }
}
