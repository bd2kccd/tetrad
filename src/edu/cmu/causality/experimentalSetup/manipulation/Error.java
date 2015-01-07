package edu.cmu.causality.experimentalSetup.manipulation;


/**
 * One of the typesafe enums in Manipulation. Error variables specify measrument
 * error.
 *
 * @author Matthew Easterday
 */
class Error extends Manipulation {

    /**
     * Constructor.
     */
    public Error() {
        super(ManipulationType.ERROR);
    }

    /**
     * Copy constructor.
     */
    public Error(Error error) {
        super(error);
    }
}
