package edu.cmu.causality2.model.experiment;

import edu.cmu.causality.experimentalSetup.manipulation.*;
//import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;

/**
 * One of the typesafe enums in Manipulation.  Locked variables are set to a
 * specific value across all members of a sample.
 *
 * @author Matthew Easterday
 */
public class Locked extends Manipulation {

    private String lockedAtValue;

    /**
     * Constructor.
     */
    public Locked() {
        super(ManipulationType.LOCKED);
    }


    /**
     * Set the locked value to the specified value
     *
     * @param value
     */
    public void setLockedAt(String value) throws IllegalArgumentException {
        lockedAtValue = value;
    }

    /**
     * Getter
     *
     * @return the value at which the variable is locked
     */
    public String getLockedAtValue() {
        return lockedAtValue;
    }
}
