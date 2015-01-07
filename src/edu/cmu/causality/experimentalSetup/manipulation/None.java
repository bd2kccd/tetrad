package edu.cmu.causality.experimentalSetup.manipulation;


/**
 * One of the typesafe enums in manipulation.  None indicates the variable is
 * not manipulated in the experimental setup.
 *
 * @author Matthew Easterday
 */
public class None extends Manipulation {

    /**
     * Constructor.
     */
    public None() {
        super(ManipulationType.NONE);
    }

}
