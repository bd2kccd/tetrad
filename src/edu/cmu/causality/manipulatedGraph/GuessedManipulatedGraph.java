package edu.cmu.causality.manipulatedGraph;

import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class describes the student guess of a manipulated graph.
 *
 * @author mattheweasterday
 */
public class GuessedManipulatedGraph extends AbstractManipulatedGraph {

    /**
     * Constructor.
     */
    public GuessedManipulatedGraph(Graph graph) {
        super(graph);

        for (Node variable : graph.getNodes()) {
            String variableName = variable.getName();
            ManipulationType manipulation = ManipulationType.NONE;
            if (variable.getNodeType() == NodeType.LATENT) {
                manipulation = ManipulationType.NONE;
            }
            variableManipulations.put(variableName, manipulation);
        }

    }

    /**
     * Set a directed edge to two valid nodes.
     *
     * @throws IllegalArgumentException
     */
    public void setEdge(String fromVariable, String toVariable) throws IllegalArgumentException {
        Node fromNode = getNode(fromVariable);
        Node toNode = getNode(toVariable);
        if (fromNode == null) {
            throw new IllegalArgumentException(fromNode + "not found");
        }
        if (toNode == null) {
            throw new IllegalArgumentException(toNode + "not found");
        }

        removeEdgeFromGuess(fromVariable, toVariable);
        addDirectedEdge(fromNode, toNode);
    }

    /**
     * Set the edge from the start node to end node as broken.
     */
    public void setEdgeBroken(String fromVariable, String toVariable) {
        removeEdgeFromGuess(fromVariable, toVariable);
        brokenEdges.add(new EdgeInfo(fromVariable, toVariable, ManipulatedEdgeType.BROKEN));
    }

    /**
     * Set a particular variable as being not manipulated.
     *
     * @throws IllegalArgumentException
     */
    public void setVariableNotManipulated(String variableName) throws IllegalArgumentException {
        setVariableManipulation(variableName, ManipulationType.NONE);
    }

    /**
     * Set a particular variable as being not locked.
     *
     * @throws IllegalArgumentException
     */
    public void setVariableLocked(String variableName) throws IllegalArgumentException {
        setVariableManipulation(variableName, ManipulationType.LOCKED);
    }

    /**
     * Set a particular variable as being not randomized.
     *
     * @throws IllegalArgumentException
     */
    public void setVariableRandomized(String variableName) throws IllegalArgumentException {
        setVariableManipulation(variableName, ManipulationType.RANDOMIZED);
    }

    /**
     * Remove the edge between the two given nodes.
     */
    public void removeEdgeFromGuess(String fromVariable, String toVariable) {
        removeEdge(getNode(fromVariable), getNode(toVariable));
        removeBrokenEdge(fromVariable, toVariable);
        removeFrozenEdge(fromVariable, toVariable);
    }

    private void setVariableManipulation(String variableName, ManipulationType type) throws IllegalArgumentException {
        if (variableManipulations.get(variableName) != null) {
            variableManipulations.put(variableName, type);
        } else {
            throw new IllegalArgumentException(variableName + " not in guess");
        }
    }


    private void removeBrokenEdge(String fromVariable, String toVariable) {
        EdgeInfo edge;
        List<EdgeInfo> edgesToRemove = new ArrayList<EdgeInfo>();

        for (EdgeInfo brokenEdge : brokenEdges) {
            edge = brokenEdge;

            if (((edge.getFromNode().equals(fromVariable)) && (edge.getToNode().equals(toVariable))) ||
                    ((edge.getFromNode().equals(toVariable)) && (edge.getToNode().equals(fromVariable)))) {
                edgesToRemove.add(edge);
            }
        }
        for (EdgeInfo anEdgesToRemove : edgesToRemove) {
            brokenEdges.remove(anEdgesToRemove);
        }
    }

    private void removeFrozenEdge(String fromVariable, String toVariable) {
        EdgeInfo edge;
        List<EdgeInfo> edgesToRemove = new ArrayList<EdgeInfo>();

        for (EdgeInfo frozenEdge : frozenEdges) {
            edge = frozenEdge;

            if (((edge.getFromNode().equals(fromVariable)) && (edge.getToNode().equals(toVariable))) ||
                    ((edge.getFromNode().equals(toVariable)) && (edge.getToNode().equals(fromVariable)))) {
                edgesToRemove.add(edge);
            }
        }
        for (EdgeInfo anEdgesToRemove : edgesToRemove) {
            frozenEdges.remove(anEdgesToRemove);
        }
    }


    public boolean existsUndirectedPathFromTo(Node node, Node node1) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public java.util.Set<edu.cmu.tetrad.graph.Triple> getAmbiguousTriples() {
        return null;
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

    public void setDottedUnderLineTriples(Set<Triple> triples) {
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

    public List<String> getNodeNames() {
        return null;
    }

    public boolean isDefNoncollider(Node node, Node node1, Node node2) {
        return false;
    }

    public boolean isDefCollider(Node node, Node node1, Node node2) {
        return false;
    }
}

