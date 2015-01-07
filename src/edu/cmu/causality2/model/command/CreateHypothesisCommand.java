package edu.cmu.causality2.model.command;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraph;
import edu.cmu.causality.hypotheticalGraph.HypotheticalGraphXml;
import edu.cmu.command.AbstractCommand;
import nu.xom.Element;

import java.text.MessageFormat;

/**
 * This moves creates a hypothesis.
 *
 * @author mattheweasterday
 */
public class CreateHypothesisCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "createHypothesisCommand";

    private final HypotheticalGraph newGraph;

    /**
     * Constructor.
     *
     * @param hg the hypothesis created by the user.
     */
    public CreateHypothesisCommand(HypotheticalGraph hg) {
        newGraph = hg;
    }

    /**
     * Executes the moves by adding the hypothesis (specified in the
     * constructor) to the application's model.
     */
    public void justDoIt() {
        CausalityLabModel.getModel().addNewHypotheticalGraph(newGraph);
    }


    /**
     * Undoes the commmand by deleting the hypothesis.
     */
    public void undo() {
        CausalityLabModel.getModel().removeHypotheticalGraph(newGraph.getName());
    }


    /**
     * A string representation of the moves used for display in the history.
     *
     * @return "hypothesis 1 created".
     */
    public String toString() {
        Object[] args = {newGraph.getName()};
        return MessageFormat.format("Hypothesis {0} created", args);
    }


    /**
     * The name of the moves used in the xml representation.
     *
     * @return "createHypothesisCommand".
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * A child xml node representing the hypothesis in the moves's xml
     * representation.
     */
    protected Element[] renderChildren() {
        Element[] elms = new Element[1];
        elms[0] = HypotheticalGraphXml.renderHypotheticalGraph(newGraph);
        return elms;
    }
}
