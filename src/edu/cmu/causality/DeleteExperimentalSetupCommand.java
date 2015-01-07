package edu.cmu.causality;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

/**
 * Command for deleting experiment.
 *
 * @author mattheweasterday
 */
public class DeleteExperimentalSetupCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "deleteExperimentalSetupCommand";

    /**
     * The xml experimentName attribute.
     */
    private static final String EXP_NAME = "experimentName";

    private ExperimentalSetup expSetup;
    private final String experimentName;

    /**
     * Constructor.
     */
    public DeleteExperimentalSetupCommand(String expName) {
        experimentName = expName;
    }


    /**
     * Run moves by deleting experimental setup.
     */
    public void justDoIt() {
        expSetup = CausalityLabModel.getModel().getExperimentalSetupCopy(experimentName);
        CausalityLabModel.getModel().removeExperiment(experimentName);
    }


    /**
     * Undoes moves by restoring deleted experiment.
     */
    public void undo() {
        CausalityLabModel.getModel().addNewExperiment(expSetup);
    }


    /**
     * @return string representation for display in moves history.
     */
    public String toString() {
        return "Experimental setup deleted";
    }


    /**
     * Name of moves used in xml representation.
     *
     * @return "deleteExperimentalSetupCommand".
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * Attributes used in xml representaion of moves.
     *
     * @return array of attributes.
     */
    protected Attribute[] renderAttributes() {
        Attribute[] atts = new Attribute[1];
        atts[0] = new Attribute(EXP_NAME, experimentName);
        return atts;
    }

}
