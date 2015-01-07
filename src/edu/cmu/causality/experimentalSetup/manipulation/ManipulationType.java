package edu.cmu.causality.experimentalSetup.manipulation;


/**
 * A typesafe enum of the types of manipulations in ExperimentalSetup
 *
 * @author Matthew Easterday
 */
public class ManipulationType {

    /**
     * No manipulation has been done on the variable.
     */
    public static final ManipulationType NONE = new ManipulationType("none");

    /**
     * The variable has been "randomized" by randomly setting its value in the
     * sample.
     */
    public static final ManipulationType RANDOMIZED = new ManipulationType("randomized");

    /**
     * The variable has been "locked" by setting it to a specific value across
     * the sample.
     */
    public static final ManipulationType LOCKED = new ManipulationType("locked");

    /**
     * The variable is unobserved.
     */
    public static final ManipulationType LATENT = new ManipulationType("latent");

    /**
     * The variable specifies measurement error.
     */
    public static final ManipulationType ERROR = new ManipulationType("error");


    private final String name;

    /**
     * Prints out the name of the type.
     */
    public String toString() {
        return name;
    }

    /**
     * Protected constructor, not allowed to add new types.
     *
     * @param name name of the variable
     */
    private ManipulationType(String name) {
        this.name = name;
    }

    /**
     * Protected constructor, not allowed to add new types.
     *
     * @param t the manipulationType of the variable
     */
    ManipulationType(ManipulationType t) {
        this.name = t.name;
    }

}
