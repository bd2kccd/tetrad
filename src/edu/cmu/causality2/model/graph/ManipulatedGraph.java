package edu.cmu.causality2.model.graph;

import edu.cmu.causality2.model.experiment.ExperimentalSetup;
import edu.cmu.causality2.model.experiment.ManipulationType;
import edu.cmu.tetrad.graph.*;

import java.util.*;

/**
 * This class is the correct manipulated graph that is derived from a correct
 * graph and an experiment setup.
 *
 * @author Matthew Easterday
 */
public class ManipulatedGraph extends EdgeListGraph {

    private Map<Node, ManipulationType> variableManipulations = new HashMap<Node, ManipulationType>();
    private List<EdgeInfo> brokenEdges;
    private List<EdgeInfo> frozenEdges;
    private Graph manipulatedGraph;
    private ExperimentalSetup experimentalSetup;

    /**
     * Creates a new manipulated graph given a correct graph and an experimental setup.
     * Variables that have been disabled will be removed from the graph, and any
     * edges that are connected
     */
    public ManipulatedGraph(Graph graph, ExperimentalSetup experimentalSetup) throws IllegalArgumentException {
        //copy constructor
        super(graph);
        this.manipulatedGraph = graph;
        this.experimentalSetup = experimentalSetup;

        if (experimentalSetup == null) {
            throw new IllegalArgumentException("experimental setup was null");
        }

        Iterator<Node> i;
        Node variable;
        String variableName;
        ManipulationType manipulation;

        //Keep track of the manipulations on each variable
        for (i = graph.getNodes().listIterator(); i.hasNext(); ) {


            variable = i.next();
            variableName = variable.getName();


            manipulation = null;
            NodeType nodeType = variable.getNodeType();
            if (nodeType == NodeType.MEASURED) {
                manipulation = experimentalSetup.getManipulation(variable).getType();
            } else if (nodeType == NodeType.LATENT) {
                manipulation = ManipulationType.NONE;
            }
            variableManipulations.put(variable, manipulation);

            if (manipulation == ManipulationType.RANDOMIZED) {
                breakEdges(findEdgesTo(variableName));
            } else if (manipulation == ManipulationType.LOCKED) {
                breakEdges(findEdgesTo(variableName));
                freezeEdges(findEdgesFrom(variableName));
            }
        }
    }


    private List<Edge> findEdgesTo(String variableName) {
        return findEdges(variableName, true);
    }

    private List<Edge> findEdgesFrom(String variableName) {
        return findEdges(variableName, false);
    }

    private List<Edge> findEdges(String variableName, boolean isToVariable) {
        Edge edge;
        List<Edge> edgesFound = new ArrayList<Edge>();
        Node node;

        //for(Iterator edgesWithVariable = edgeIterator(); edgesWithVariable.hasNext();){
        for (Edge edge1 : getEdges(getNode(variableName))) {
            edge = edge1;
            //node = (isEdgeToVariable) ? edge.getNodeB() : edge.getNodeA();
            node = (isToVariable) ? edge.getNode2() : edge.getNode1();

            if (node.getName().equals(variableName)) {
                //mark the edge for removal
                edgesFound.add(edge);
            }
        }
        return edgesFound;
    }

    public boolean existsUndirectedPathFromTo(Node node, Node node1) {
        return false;
    }


    public java.util.Set<edu.cmu.tetrad.graph.Triple> getAmbiguousTriples() {
        return null;
    }

    public boolean isDefNoncollider(Node node, Node node1, Node node2) {
        return false;
    }

    public boolean isDefCollider(Node node, Node node1, Node node2) {
        return false;
    }

    public Set<Triple> getUnderLines() {
        return null;
    }

    public Set<Triple> getDottedUnderlines() {
        return null;
    }

    public boolean isAmbiguousTriple(Node node, Node node1, Node node2) {
        return false;
    }

    public boolean isDottedUnderlineTriple(Node node, Node node1, Node node2) {
        return false;
    }

    public void addAmbiguousTriple(Node node, Node node1, Node node2) {
    }

    public void addDottedUnderlineTriple(Node node, Node node1, Node node2) {
    }

    public void removeAmbiguousTriple(Node node, Node node1, Node node2) {
    }

    public void removeDottedUnderlineTriple(Node node, Node node1, Node node2) {
    }

    public void setAmbiguousTriples(Set<Triple> triples) {
    }

    public List<Node> getTierOrdering() {
        return null;
    }

    public void setHighlighted(Edge edge, boolean highlighted) {

    }

    public boolean isHighlighted(Edge edge) {
        return false;
    }

    public boolean isParameterizable(Node node) {
        return false;
    }

    public boolean isTimeLagModel() {
        return false;
    }

    public TimeLagGraph getTimeLagGraph() {
        return null;
    }

    /**
     * Breaks the list of edges.
     *
     * @param edges list of edges to break.
     */
    void breakEdges(List edges) {
        Edge edge;
        for (Object edge1 : edges) {
            edge = (Edge) edge1;
            brokenEdges.add(new EdgeInfo(edge, ManipulatedEdgeType.BROKEN));
            super.removeEdge(edge);
        }
    }

    /**
     * Freezes the list of edges.
     *
     * @param edges list of edges to freeze.
     */
    void freezeEdges(List edges) {
        Edge edge;
        for (Object edge1 : edges) {
            edge = (Edge) edge1;
            frozenEdges.add(new EdgeInfo(edge, ManipulatedEdgeType.FROZEN));
            //you don't remove the frozen edge because this would mean you have to redefine
            //the probabilities for the toNode,  easier to just leave the probabilities
            //and change the values of the from node
        }
    }

    public Graph getManipulatedGraph() {
        return manipulatedGraph;
    }

    public ExperimentalSetup getExperimentalSetup() {
        return experimentalSetup;
    }

    public Map<Node, ManipulationType> getVariableManipulations() {
        return variableManipulations;
    }

    public List<EdgeInfo> getBrokenEdges() {
        return brokenEdges;
    }

    public List<EdgeInfo> getFrozenEdges() {
        return frozenEdges;
    }
}
