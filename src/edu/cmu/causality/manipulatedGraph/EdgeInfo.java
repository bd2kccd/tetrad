package edu.cmu.causality.manipulatedGraph;

import edu.cmu.tetrad.graph.Edge;

/**
 * @author Matthew Easterday
 */
public class EdgeInfo {

    private final ManipulatedEdgeType type;
    private final String fromNode;
    private final String toNode;

    /**
     * Default constructor setting the Edge as a normal one.
     */
    public EdgeInfo(Edge edge) {
        this(edge, ManipulatedEdgeType.NORMAL);
    }

    /**
     * Use this constructor if you want to specify what type of edge this is (eg
     * normal, broken or frozen).
     */
    public EdgeInfo(Edge edge, ManipulatedEdgeType type) {
        this(edge.getNode1().getName(), edge.getNode2().getName(), type);
    }

    /**
     * Use this constructor if you want to specify the start and end node, as well
     * as the manipulation type of the edge.
     */
    public EdgeInfo(String fromNode, String toNode, ManipulatedEdgeType type) {
        this.type = type;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    /**
     * @return start node of edge.
     */
    public String getFromNode() {
        return fromNode;
    }

    /**
     * @return end node of edge.
     */
    public String getToNode() {
        return toNode;
    }

    /**
     * @return manipulation type of this edge.
     */
    public ManipulatedEdgeType getType() {
        return type;
    }

    /**
     * @return a string representation of this edge.
     */
    public String toString() {
        return fromNode + " --> " + toNode;
    }

    /**
     * Verify that object o is the same as this EdgeInfo.
     *
     * @return true if so.
     */
    public boolean equals(Object o) {
        return o instanceof EdgeInfo && toString().equals(o.toString());
    }
}
