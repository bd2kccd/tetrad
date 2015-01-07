package edu.cmu.causality;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.ExperimentalSetupXml;
import edu.cmu.command.AbstractCommand;
import nu.xom.Element;

/**
 * This moves creates a new experimental setup.
 *
 * @author mattheweasterday
 */
public class CreateExperimentalSetupCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "createExperimentalSetupCommand";

    private final CausalityLabModel MODEL;
    private final ExperimentalSetup EXPERIMENT;

    /**
     * Constructor.
     */
    public CreateExperimentalSetupCommand(CausalityLabModel clm, ExperimentalSetup esvs) {
        MODEL = clm;
        EXPERIMENT = esvs;
    }


    /**
     * Makes a new experiment from the information provided in the constructor.
     * Overides method in AbstractCommand.
     */
    public void justDoIt() {
        MODEL.addNewExperiment(EXPERIMENT);
    }

    /**
     * Undoes the moves by deleting the experiment.
     * Overides method in AbstractCommand.
     */
    public void undo() {
        MODEL.removeExperiment(EXPERIMENT.getName());
    }

    /**
     * Provides a name for the moves for saving as xml.
     *
     * @return the name of the moves.
     */
    public String getCommandName() {
        return MY_NAME;
    }

    /**
     * Provides a string from representing the moves in the moves history.
     */
    public String toString() {
        return "Experimental setup created";
    }

    /**
     * Creates children node representation of the experiment for the xml
     * representation of the moves.
     */
    public Element[] renderChildren() {
        Element[] elms = new Element[1];
        elms[0] = ExperimentalSetupXml.renderStudiedVariables(EXPERIMENT);
        return elms;
    }


}
