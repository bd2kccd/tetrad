package edu.cmu.causality.manipulatedGraph;

/**
 * This is a typesafe enum of the type of Manipulated Edges in a graph.
 *
 * @author Matthew Easterday
 */
public class ManipulatedEdgeType {

    /**
     * An edge is "normal" if the edge is not affected by any manipulation of its
     * start and end nodes.
     */
    public static final ManipulatedEdgeType NORMAL = new ManipulatedEdgeType("Normal");

    /**
     * An edge is "frozen" if its start node is locked.
     */
    public static final ManipulatedEdgeType FROZEN = new ManipulatedEdgeType("Frozen");

    /**
     * An edge is "broken" if its end node is randomized or locked.
     */
    public static final ManipulatedEdgeType BROKEN = new ManipulatedEdgeType("Broken");

    private final String name;

    /**
     * Constructor.
     */
    private ManipulatedEdgeType(String name) {
        this.name = name;
    }

    /**
     * @return the type name of the edge manipulation.
     */
    public String toString() {
        return name;
    }
}
