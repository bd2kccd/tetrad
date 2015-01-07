package edu.cmu.causality2.model.command;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

/**
 * This moves deletes a hypothetical graph.
 *
 * @author mattheweasterday
 */
public class DeleteHypothesisCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "deleteHypothesisCommand";

    private static final String HYP_NAME = "hypothesisName";
    private HypotheticalGraph oldHypGraph;
    private final String hypName;

    /**
     * Constructor.
     *
     * @param hypName name of hypothesis to delete.
     */
    public DeleteHypothesisCommand(String hypName) {
        this.hypName = hypName;
    }


    /**
     * Runs moves by deleting hypothesis.
     */
    public void justDoIt() {
        oldHypGraph = CausalityLabModel.getModel().getHypotheticalGraphCopy(hypName);
        CausalityLabModel.getModel().removeHypotheticalGraph(hypName);
    }


    /**
     * Undoes moves by restoring the deleted hypothesis.
     */
    public void undo() {
        CausalityLabModel.getModel().setHypotheticalGraph(oldHypGraph, false);
        oldHypGraph = null;
    }


    /**
     * String representation of moves for display in moves history.
     *
     * @return "Hypothesis deleted".
     */
    public String toString() {
        return "Hypothesis deleted";
    }


    /**
     * Name of moves for use in xml representation of moves.
     *
     * @return "deleteHypothesisCommand".
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * @return attributes used in xml representation of moves.
     */
    protected Attribute[] renderAttributes() {
        Attribute[] atts = new Attribute[1];
        atts[0] = new Attribute(HYP_NAME, hypName);
        return atts;
    }
}
