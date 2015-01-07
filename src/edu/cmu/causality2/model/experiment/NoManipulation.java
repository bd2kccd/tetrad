package edu.cmu.causality2.model.experiment;

//import edu.cmu.causality.experimentalSetup.manipulation.*;
//import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;

/**
 * One of the typesafe enums in manipulation.  None indicates the variable is
 * not manipulated in the experimental setup.
 *
 * @author Matthew Easterday
 */
public class NoManipulation extends Manipulation {

    public NoManipulation() {
        super(ManipulationType.NONE);
    }
}
