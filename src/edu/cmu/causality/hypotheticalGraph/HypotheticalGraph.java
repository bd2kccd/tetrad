package edu.cmu.causality.hypotheticalGraph;

import edu.cmu.tetrad.graph.*;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class describes a hypothetical graph.
 *
 * @author Matthew Easterday
 */
public class HypotheticalGraph implements Graph {

    private String name;
    private final Dag dag;

    public Edge getDirectedEdge(Node node, Node node1) {
        System.err.println("this is bad");
        return null;
    }

    /**
     * Constructs a new HypotheticalGraph from the given tetradGraph.
     *
     * @param name        specifies what to call the graph and is used as index.
     * @param graph       the original tetrad graph.
     * @param withEdges   if true, edges are copied from graph to the Hypothetical graph.
     * @param withLatents if true, latents are copied from graph to the Hypothetical graph.
     */
    public HypotheticalGraph(String name, Graph graph, boolean withEdges, boolean withLatents) {
        dag = new Dag(graph);
        Node node;
        this.name = name;
        if (!withEdges) {
            dag.removeEdges(dag.getEdges());
        }
        for (Node node1 : dag.getNodes()) {
            node = node1;
            if (withLatents && (node.getNodeType() == NodeType.LATENT)) {
                dag.removeNode(node);
            }
            if ((node.getNodeType() == NodeType.ERROR)) {
                dag.removeNode(node);
            }

        }
    }

    /**
     * Copy constructor.
     *
     * @param withEdges   if false, copies only the variables.
     * @param withLatents if false, doesn't copy latent variables.
     * @return a copy of the graph.
     */
    public HypotheticalGraph copyGraph(boolean withEdges, boolean withLatents) {
        HypotheticalGraph hg = new HypotheticalGraph(this.name, this.dag, false, withLatents);
        if (withEdges) {
            for (Object o : getEdges()) {
                Edge ed = (Edge) o;
                hg.addDirectedEdge(
                        getNode(ed.getNode1().getName()),
                        getNode(ed.getNode2().getName()));
            }
        }
        return hg;
    }

    /**
     * Add a latent variable to the hypothetical graph.
     *
     * @param name the name of the latent variable to add.
     */
    public void addLatentVariable(String name) {
        Node latent = new GraphNode(name);
        latent.setNodeType(NodeType.LATENT);
        dag.addNode(latent);
    }

    /**
     * Add a latent variable to the hypothetical graph with the coordinates (x,y).
     * This is used during the parsing of the hypothetical graph xml to draw the
     * latent variable at its original position in the graph.
     */
    public void addLatentVariable(String name, int x, int y) {
        Node latent = new GraphNode(name);
        latent.setNodeType(NodeType.LATENT);
        latent.setCenter(x, y);
        dag.addNode(latent);
    }

    /**
     * @return if a given variable is latent.
     */
    public boolean isVariableLatent(String name) {
        return (dag.getNode(name).getNodeType() == NodeType.LATENT);
    }

    /**
     * @return a string array of the names of the variables in the graph.
     */
    public String[] getVariableNamesFromGraph() {
        Node variable;
        Iterator<Node> i;
        int j;
        String[] variableNames = new String[dag.getNumNodes()];

        for (j = 0, i = dag.getNodes().listIterator(); i.hasNext(); j++) {
            variable = i.next();
            variableNames[j] = variable.getName();
        }

        return variableNames;
    }


    /**
     * Removes a latent variable to the hypothetical graph, does nothing if
     * variable doesn't exist or variable is not latent.
     *
     * @param name the name of the latent variable to add.
     */
    public void removeLatentVariable(String name) {
        Node variable = dag.getNode(name);
        if (variable == null) {
            return;
        }
        if (variable.getNodeType() != NodeType.LATENT) {
            return;
        }
        dag.removeNode(variable);
    }

    /**
     * Removes all the latents in the graph.
     */
    public void removeLatenVariables() {
        List<Node> latentNodes = new ArrayList<Node>();
        Node node;
        int i;
        for (Node node1 : dag.getNodes()) {
            node = node1;
            if (node.getNodeType() == NodeType.LATENT) {
                latentNodes.add(node);
            }
        }

        Node[] nodes = new Node[latentNodes.size()];
        for (i = 0; i < nodes.length; i++) {
            nodes[i] = latentNodes.get(i);
        }

        for (i = 0; i < nodes.length; i++) {
            dag.removeNode(nodes[i]);
        }
    }

    /**
     * @return the X coordinate of the given variable.
     */
    public int getVariableCenterX(String varName) {
        return dag.getNode(varName).getCenterX();
    }

    /**
     * @return the Y coordinate of the given variable.
     */
    public int getVariableCenterY(String varName) {
        return dag.getNode(varName).getCenterY();
    }

    /**
     * Sets the x,y center of the given variable
     */
    public void setVariableCenter(String varName, int x, int y) {
        dag.getNode(varName).setCenter(x, y);
    }

    /**
     * Gets the name of this Hypothetical Graph
     *
     * @return the name as a String
     */
    public String getName() {
        return name;
    }


    //===========================================================
    //
    //                      GRAPH METHODS
    //
    //===========================================================

    /**
     * Adds a bidirected edges <-> to the graph.
     */
    public boolean addBidirectedEdge(Node node1, Node node2) {
        return dag.addBidirectedEdge(node1, node2);
    }

    /**
     * Adds a directed edge --> to the graph.
     */
    public boolean addDirectedEdge(Node node1, Node node2) {
        return dag.addDirectedEdge(node1, node2);
    }

    /**
     * Adds an undirected edge --- to the graph.
     */
    public boolean addUndirectedEdge(Node node1, Node node2) {
        return dag.addUndirectedEdge(node1, node2);
    }

    /**
     * Adds the specified edge to the graph, provided it is not already in the
     * graph.
     *
     * @return true if the edge was added, false if not.
     */
    public boolean addEdge(Edge edge) {
        return dag.addEdge(edge);
    }

    /**
     * Adds a graph constraint.
     *
     * @return true if the constraint was added, false if not.
     */
    public boolean addGraphConstraint(GraphConstraint gc) {
        return dag.addGraphConstraint(gc);
    }

    /**
     * Adds a node to the graph. Precondition: The proposed name of the node
     * cannot already be used by any other node in the same graph.
     *
     * @return true if nodes were added, false if not.
     */
    public boolean addNode(Node node) {
        return dag.addNode(node);
    }

    /**
     * Adds a PropertyChangeListener to the graph.
     */
    public void addPropertyChangeListener(PropertyChangeListener e) {
        dag.addPropertyChangeListener(e);
    }

    /**
     * Removes all nodes (and therefore all edges) from the graph.
     */
    public void clear() {
        dag.clear();
    }

    /**
     * Determines whether this graph contains the given edge.
     *
     * @return true iff the graph contain 'edge'.
     */
    public boolean containsEdge(Edge edge) {
        return dag.containsEdge(edge);
    }

    /**
     * Determines whether this graph contains the given node.
     *
     * @return true iff the graph contains 'node'.
     */
    public boolean containsNode(Node node) {
        return dag.containsNode(node);
    }

    /**
     * Returns true iff there is a directed cycle in the graph.
     */
    public boolean existsDirectedCycle() {
        return dag.existsDirectedCycle();
    }

    /**
     * Returns true iff there is a directed path from node1 to node2 in the
     * graph.
     */
    public boolean existsDirectedPathFromTo(Node node1, Node node2) {
        return dag.existsDirectedPathFromTo(node1, node2);
    }

    public boolean existsUndirectedPathFromTo(Node node, Node node1) {
        return false;
    }


    /**
     * </p> A semi-directed path from A to B is an undirected path in which no
     * edge has an arrowhead pointing "back" towards A.
     *
     * @return true iff there is a semi-directed path from node1 to something in
     *         nodes2 in the graph
     */
    public boolean existsSemiDirectedPathFromTo(Node node1, Set<Node> nodes) {
        return dag.existsSemiDirectedPathFromTo(node1, nodes);
    }

    /**
     * Determines whether an inducing path exists between node1 and node2, given
     * a set O of observed nodes and a set sem of conditioned nodes.
     *
     * @param node1             the first node.
     * @param node2             the second node.
     * @param observedNodes     the set of observed nodes.
     * @param conditioningNodes the set of nodes conditioned upon.
     * @return true if an inducing path exists, false if not.
     */
    public boolean existsInducingPath(Node node1, Node node2, Set<Node> observedNodes,
                                      Set<Node> conditioningNodes) {
        return dag.existsInducingPath(node1, node2, observedNodes, conditioningNodes);
    }

    /**
     * Returns true iff a trek exists between two nodes in the graph.  A trek
     * exists if there is a directed path between the two nodes or else, for
     * some third node in the graph, there is a path to each of the two nodes in
     * question.
     */
    public boolean existsTrek(Node node1, Node node2) {
        return dag.existsTrek(node1, node2);
    }

    /**
     * Determines whether this graph is equal to some other graph, in the sense
     * that they contain the same nodes and the sets of edges defined over these
     * nodes in the two graphs are isomorphic typewise. That is, if node A and B
     * exist in both graphs, and if there are, e.g., three edges between A and B
     * in the first graph, two of which are directed edges and one of which is
     * an undirected edge, then in the second graph there must also be two
     * directed edges and one undirected edge between nodes A and B.
     */
    public boolean equals(Object o) {
        if (!(o instanceof HypotheticalGraph)) {
            throw new IllegalArgumentException();
        }
        return dag.equals(o);
    }

    /**
     * Removes all edges from the graph and fully connects it using #-# edges,
     * where # is the given endpoint.
     */
    public void fullyConnect(Endpoint endpoint) {
        dag.fullyConnect(endpoint);
    }

    /**
     * Reorients all edges in the graph with the given endpoint.
     */
    public void reorientAllWith(Endpoint endpoint) {
        dag.reorientAllWith(endpoint);
    }

    /**
     * Returns the list of nodes adjacent to the given node.
     */
    public List<Node> getAdjacentNodes(Node node) {
        return dag.getAdjacentNodes(node);
    }

    /**
     * Returns the list of ancestors for the given nodes.
     */
    public List<Node> getAncestors(List<Node> nodes) {
        return dag.getAncestors(nodes);
    }

    /**
     * Returns the list of children for a node.
     */
    public List<Node> getChildren(Node node) {
        return dag.getChildren(node);
    }

    /**
     * Returns the connectivity of the graph.
     */
    public int getConnectivity() {
        return dag.getConnectivity();
    }

    /**
     * Returns the list of descendants for the given nodes.
     */
    public List<Node> getDescendants(List<Node> nodes) {
        return dag.getDescendants(nodes);
    }

    /**
     * Returns the edge connecting node1 and node2, provided a unique such edge
     * exists.
     *
     * @throws UnsupportedOperationException if the graph allows multiple edges
     *                                       between node pairs.
     */
    public Edge getEdge(Node node1, Node node2) {
        return dag.getEdge(node1, node2);
    }

    /**
     * Returns the list of edges connected to a particular node. No particular
     * ordering of the edges in the list is guaranteed.
     */
    public List<Edge> getEdges(Node node) {
        return dag.getEdges(node);
    }

    /**
     * Returns the edges connecting node1 and node2.
     */
    public List<Edge> getEdges(Node node1, Node node2) {
        return dag.getEdges(node1, node2);
    }

    /**
     * Returns the list of edges in the graph.  No particular ordering of the
     * edges in the list is guaranteed.
     */
    public List<Edge> getEdges() {
        return dag.getEdges();
    }

    /**
     * Returns the endpoint along the edge from node to node2 at the node2 end.
     */
    public Endpoint getEndpoint(Node node1, Node node2) {
        return dag.getEndpoint(node1, node2);
    }

    /**
     * Returns a matrix of endpoints for the nodes in this graph, with nodes in
     * the same order as getNodes().
     */
    public Endpoint[][] getEndpointMatrix() {
        return dag.getEndpointMatrix();
    }

    /**
     * Returns the list of graph constraints for this graph.
     */
    public List<GraphConstraint> getGraphConstraints() {
        return dag.getGraphConstraints();
    }

    /**
     * Returns the number of arrow endpoints adjacent to a node.
     */
    public int getIndegree(Node node) {
        return dag.getIndegree(node);
    }

    /**
     * Returns the node with the given string name.  In case of accidental
     * duplicates, the first node encountered with the given name is returned.
     * In case no node exists with the given name, null is returned.
     */
    public Node getNode(String name) {
        return dag.getNode(name);
    }

    /**
     * Returns the list of nodes for the graph.
     */
    public List<Node> getNodes() {
        return dag.getNodes();
    }

    /**
     * Returns the number of edges in the (entire) graph.
     */
    public int getNumEdges() {
        return dag.getNumEdges();
    }

    /**
     * Returns the number of edges in the graph which are connected to a
     * particular node.
     */
    public int getNumEdges(Node node) {
        return dag.getNumEdges(node);
    }

    /**
     * Returns the number of nodes in the graph.
     */
    public int getNumNodes() {
        return dag.getNumNodes();
    }

    /**
     * Returns the number of null endpoints adjacent to an edge.
     */
    public int getOutdegree(Node node) {
        return dag.getOutdegree(node);
    }

    /**
     * Returns the list of parents for a node.
     */
    public List<Node> getParents(Node node) {
        return dag.getParents(node);
    }

    /**
     * Returns true iff node1 is adjacent to node2 in the graph.
     */
    public boolean isAdjacentTo(Node node1, Node node2) {
        return dag.isAdjacentTo(node1, node2);
    }

    /**
     * Determines whether one node is an ancestor of another.
     */
    public boolean isAncestorOf(Node node1, Node node2) {
        return dag.isAncestorOf(node1, node2);
    }

    /**
     * added by ekorber, 2004/06/12
     *
     * @return true if node1 is a possible ancestor of node2.
     */
    public boolean possibleAncestor(Node node1, Node node2) {
        return dag.possibleAncestor(node1, node2);
    }

    /**
     * Returns true iff node1 is a child of node2 in the graph.
     */
    public boolean isChildOf(Node node1, Node node2) {
        return dag.isChildOf(node1, node2);
    }

    /**
     * Determines whether node1 is a parent of node2.
     */
    public boolean isParentOf(Node node1, Node node2) {
        return dag.isParentOf(node1, node2);
    }

    /**
     * Determines whether one node is a proper ancestor of another.
     */
    public boolean isProperAncestorOf(Node node1, Node node2) {
        return dag.isProperAncestorOf(node1, node2);
    }

    /**
     * Determines whether one node is a proper decendent of another.
     */
    public boolean isProperDescendentOf(Node node1, Node node2) {
        return dag.isProperDescendentOf(node1, node2);
    }

    /**
     * Returns true iff node1 is a (non-proper) descendant of node2.
     */
    public boolean isDescendentOf(Node node1, Node node2) {
        return dag.isDescendentOf(node1, node2);
    }

    /**
     * A node Y is a definite nondescendent of a node X just in case there is no
     * semi-directed path from X to Y.
     * <p/>
     * added by ekorber, 2004/06/12.
     *
     * @return true if node 2 is a definite nondecendent of node 1
     */
    public boolean defNonDescendent(Node node1, Node node2) {
        return dag.defNonDescendent(node1, node2);
    }

    /**
     * A directed edge A->B is definitely visible if there is a node C not
     * adjacent to B such that C*->A is in the PAG. Added by ekorber,
     * 2004/06/11.
     *
     * @return true if the given edge is definitely visible (Jiji, pg 25)
     * @throws IllegalArgumentException if the given edge is not a directed edge
     *                                  in the graph
     */
    public boolean defVisible(Edge edge) {
        return dag.defVisible(edge);
    }

    /**
     * Returns true iff the given node is exogenous in the graph.
     */
    public boolean isExogenous(Node node) {
        return dag.isExogenous(node);
    }

    /**
     * Nodes adjacent to the given node with the given proximal endpoint.
     */
    public List<Node> getNodesInTo(Node node, Endpoint n) {
        return dag.getNodesInTo(node, n);
    }

    /**
     * Nodes adjacent to the given node with the given distal endpoint.
     */
    public List<Node> getNodesOutTo(Node node, Endpoint n) {
        return dag.getNodesOutTo(node, n);
    }

    /**
     * Removes the given edge from the graph.
     *
     * @return true if the edge was removed, false if not.
     */
    public boolean removeEdge(Edge edge) {
        return dag.removeEdge(edge);
    }

    /**
     * Removes the edge connecting the two given nodes, provided there is
     * exactly one such edge.
     *
     * @throws UnsupportedOperationException if multiple edges between node
     *                                       pairs are not supported.
     */
    public boolean removeEdge(Node node1, Node node2) {
        return dag.removeEdge(node1, node2);
    }

    /**
     * Removes all edges connecting node A to node B.  In most cases, this will
     * remove at most one edge, but since multiple edges are permitted in some
     * graph implementations, the number will in some cases be greater than
     * one.
     *
     * @return true if edges were removed, false if not.
     */
    public boolean removeEdges(Node node1, Node node2) {
        return dag.removeEdges(node1, node2);
    }

    /**
     * Removes a node from the graph.
     *
     * @return true if the node was removed, false if not.
     */
    public boolean removeNode(Node node) {
        return dag.removeNode(node);
    }

    /**
     * Sets the endpoint type at the 'to' end of the edge from 'from' to 'to' to
     * the given endpoint.  Note: NOT CONSTRAINT SAFE
     */
    public boolean setEndpoint(Node from, Node to, Endpoint endPoint) {
        return dag.setEndpoint(from, to, endPoint);
    }

    /**
     * Returns true iff graph constraints will be checked for future graph
     * modifications.
     */
    public boolean isGraphConstraintsChecked() {
        return dag.isGraphConstraintsChecked();
    }

    /**
     * Set whether graph constraints will be checked for future graph
     * modifications.
     */
    public void setGraphConstraintsChecked(boolean checked) {
        dag.setGraphConstraintsChecked(checked);
    }

    /**
     * Returns a string representation of the graph.
     */
    public String toString() {
        return dag.toString();
    }

    /**
     * Transfers nodes and edges from one graph to another.  One way this is
     * used is to change graph types.  One constructs a new graph based on the
     * old graph, and this method is called to transfer the nodes and edges of
     * the old graph to the new graph.
     *
     * @param graph the graph from which nodes and edges are to be pilfered.
     * @throws java.lang.IllegalArgumentException
     *          This exception is thrown if adding some node or edge violates
     *          one of the basicConstraints of this graph.
     */
    public void transferNodesAndEdges(Graph graph) throws IllegalArgumentException {
        dag.transferNodesAndEdges(graph);
    }

    public Set<Triple> getAmbiguousTriples() {
        return null;
    }

    /**
     * NOT USED.
     * Adds an nondirected edges o-o to the graph.
     */
    public boolean addNondirectedEdge(Node node1, Node node2) {
        return dag.addNondirectedEdge(node1, node2);
    }

    /**
     * NOT USED.
     * Adds a partially oriented edge o-> to the graph.
     */
    public boolean addPartiallyOrientedEdge(Node node1, Node node2) {
        return dag.addPartiallyOrientedEdge(node1, node2);
    }

    public boolean isUnderlineTriple(Node x, Node y, Node z) {
        return dag.isUnderlineTriple(x, y, z);
    }

    public void addUnderlineTriple(Node x, Node y, Node z) {
        dag.addUnderlineTriple(x, y, z);
    }

    public void removeUnderlineTriple(Node x, Node y, Node z) {
        dag.removeUnderlineTriple(x, y, z);
    }

    public void setUnderLineTriples(Set<Triple> triples) {
        dag.setUnderLineTriples(triples);
    }

    public void setDottedUnderLineTriples(Set<Triple> triples) {
        dag.setDottedUnderLineTriples(triples);
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
        return dag.getNodeNames();
    }

    //    public List<Node> getDescendants(List<Node> nodes) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
    public boolean isDefNoncollider(Node node, Node node1, Node node2) {
        return dag.isDefNoncollider(node, node1, node2);
    }

    public boolean isDefCollider(Node node, Node node1, Node node2) {
        return dag.isDefCollider(node, node1, node2);
    }

    public boolean removeEdges(List<Edge> edges) {
        return dag.removeEdges(edges);
    }

    public boolean removeNodes(List<Node> nodes) {
        return dag.removeNodes(nodes);
    }

    public Graph subgraph(List<Node> nodes) {
        return dag.subgraph(nodes);
    }

    public Set<Triple> getUnderLines() {
        return dag.getUnderLines();
    }

    public Set<Triple> getDottedUnderlines() {
        return dag.getDottedUnderlines();
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


    /**
     * Determines whether one node is d-connected to another. According to
     * Spirtes, Richardson & Meek, two nodes are d- connected given some
     * conditioning set Z if there is an acyclic undirected path U between them,
     * such that every collider on U is an ancestor of some element in Z and
     * every non-collider on U is not in Z.  Two elements are d-separated just
     * in case they are not d-connected.  A collider is a node which two edges
     * hold in common for which the endpoints leading into the node are both
     * arrow endpoints.
     */
    public boolean isDConnectedTo(Node node1, Node node2, List<Node> z) {
        return dag.isDConnectedTo(node1, node2, z);
    }

    /**
     * Determines whether one node is d-separated from another. Two elements are
     * d-separated just in case they are not d-connected.
     */
    public boolean isDSeparatedFrom(Node node1, Node node2, List<Node> z) {
        return dag.isDSeparatedFrom(node1, node2, z);
    }

    /**
     * Determines if nodes 1 and 2 are possibly d-connected given conditioning
     * set z.  A path U is possibly-d-connecting if every definite collider on U
     * is a possible ancestor of a node in z and every definite non-collider is
     * not in z.
     * <p/>
     * added by ekorber, 2004/06/15.
     *
     * @return true iff nodes 1 and 2 are possibly d-connected given z
     */
    public boolean possDConnectedTo(Node node1, Node node2, List<Node> z) {
        return dag.possDConnectedTo(node1, node2, z);
    }

    /**
     * Returns true iff there is a single directed edge from node1 to node2 in
     * the graph.
     */
    public boolean isDirectedFromTo(Node node1, Node node2) {
        return dag.isDirectedFromTo(node1, node2);
    }

    /**
     * Returns true iff there is a single undirected edge from node1 to node2 in
     * the graph.
     */
    public boolean isUndirectedFromTo(Node node1, Node node2) {
        return dag.isUndirectedFromTo(node1, node2);
    }

}
