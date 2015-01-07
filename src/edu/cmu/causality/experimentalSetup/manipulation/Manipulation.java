package edu.cmu.causality.experimentalSetup.manipulation;

/**
 * This class defines the manipulations that can be done to a variable in the
 * experimental setup.
 *
 * @author Matthew Easterday
 */
public class Manipulation {

    private final ManipulationType type;

    /**
     * Constructor.
     */
    public Manipulation(ManipulationType type) {
        this.type = type;
    }


    /**
     * Copy constructor.
     */
    public Manipulation(Manipulation manipulation) {
        this.type = new ManipulationType(manipulation.getType());
    }


    /**
     * Gets the manipulation type
     *
     * @return the manipulation type.
     */
    public ManipulationType getType() {
        return type;
    }


}
