package edu.cmu.causality2.model.experiment;

import edu.cmu.causality.experimentalSetup.manipulation.*;

/**
 * This class defines the manipulations that can be done to a variable in the
 * experimental setup.
 *
 * @author Matthew Easterday
 */
public abstract class Manipulation {

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
        this.type = manipulation.getType();
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
