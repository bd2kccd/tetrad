package edu.cmu.causality.manipulatedGraph;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class is the correct manipulated graph that is derived from a correct
 * graph and an experiment setup.
 *
 * @author Matthew Easterday
 */
public class ManipulatedGraph extends AbstractManipulatedGraph {

    /**
     * Creates a new manipulated graph given a correct graph and an experimental setup.
     * Variables that have been disabled will be removed from the graph, and any
     * edges that are connected
     */
    public ManipulatedGraph(Graph graph, ExperimentalSetup experimentalSetup) throws IllegalArgumentException {
        //copy constructor
        super(graph);

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
            if (variable.getNodeType() == NodeType.MEASURED) {
                manipulation = experimentalSetup.getVariable(variableName).getManipulation().getType();
            } else if (variable.getNodeType() == NodeType.LATENT) {
                manipulation = ManipulationType.NONE;
            }
            variableManipulations.put(variableName, manipulation);

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
}
