package edu.cmu.causality2.model.command;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraphXml;
import edu.cmu.command.AbstractCommand;
import nu.xom.Element;

/**
 * Command for editing a previously created hypothesis.
 *
 * @author mattheweasterday
 */
public class EditHypothesisCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "editHypothesisCommand";

    private HypotheticalGraph newGraph;
    private HypotheticalGraph oldGraph;

    /**
     * Constructor.
     *
     * @param hg the newly edited hypothesis.
     */
    public EditHypothesisCommand(HypotheticalGraph hg) {
        newGraph = hg;
    }

    /**
     * Executes the moves by creating the hypothesis given and replacing the old
     * hypothesis of the same name.
     */
    public void justDoIt() {
        oldGraph = CausalityLabModel.getModel().getHypotheticalGraphCopy(newGraph.getName());
        CausalityLabModel.getModel().setHypotheticalGraph(newGraph, true);
    }


    /**
     * Undoes the moves by replacing the hypothesis with the old hypothesis.
     */
    public void undo() {
        newGraph = CausalityLabModel.getModel().getHypotheticalGraphCopy(newGraph.getName());
        CausalityLabModel.getModel().setHypotheticalGraph(oldGraph, true);
    }


    /**
     * @return string representation of the moves used for display in moves
     *         history.
     */
    public String toString() {
        return "Hypothesis edited";
    }


    /**
     * Name of the moves  used in xml representation.
     *
     * @return "editHypothesisCommand".
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * @return xml child node that represents they hypothetical graph used in xml
     *         representation of the moves.
     */
    protected Element[] renderChildren() {
        Element[] elms = new Element[1];
        elms[0] = HypotheticalGraphXml.renderHypotheticalGraph(newGraph);
        return elms;
    }
}
