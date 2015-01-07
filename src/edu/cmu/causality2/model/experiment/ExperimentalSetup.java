package edu.cmu.causality2.model.experiment;

import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;

import java.util.*;


/**
 * Keeps track of (qualitative) manipulations for each variable
 *
 * @author Matthew Easterday
 */
public class ExperimentalSetup {
    private List<ExperimentalSetupVariable> variables;
    private String name;        // The name of this experimental setup
    private Graph graph;
    private Map<Node, Manipulation> manipulations;


    /**
     * Constructor.
     *
     * @param name  the name of the experimental setup
     * @param graph the graph on which this experimental setup is supposed to manipulated
     */
    public ExperimentalSetup(String name, Graph graph) {
        this.name = name;
        this.graph = graph;
        this.manipulations = new HashMap<Node, Manipulation>();

        for (Node node : graph.getNodes()) {
            this.manipulations.put(node, new NoManipulation());
        }
    }

    /**
     * Gets the name of the experimental setup
     *
     * @return variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of variables in the experimental setup.
     *
     * @return num variables.
     */
    public int getNumVariables() {
        return variables.size();
    }

    public Graph getGraph() {
        return new EdgeListGraph(graph);
    }

    public void setManipulation(Node node, Manipulation manipulation) {
        this.manipulations.put(node, manipulation);
    }

    public Manipulation getManipulation(Node node) {
        return manipulations.get(node);
    }

    //=========================================================================
    //
    //  PRIVATE METHODS
    //
    //=========================================================================
}
