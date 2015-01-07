///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.util.*;

/**
 * Implements the ICPC algorithm.
 *
 * @author Joseph Ramsey (this version).
 */
public class Jcpc2 implements GraphSearch {
    private int numAdded;
    private int numRemoved;

    public enum PathBlockingSet {
        LARGE, SMALL
    }

    private PathBlockingSet pathBlockingSet = PathBlockingSet.LARGE;

    /**
     * The independence test used for the PC search.
     */
    private IndependenceTest independenceTest;

    /**
     * Forbidden and required edges for the search.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * The maximum number of adjacencies that may ever be added to any node. (Note, the initial search may already have
     * greater degree.)
     */
    private int softmaxAdjacencies = 8;

    /**
     * The maximum number of iterations of the algorithm, in the major loop.
     */
    private int maxIterations = 20;

    /**
     * True if the algorithm should be started from an empty graph.
     */
    private boolean startFromEmptyGraph = false;

    /**
     * The maximum length of a descendant path. Descendant paths must be checked in the common collider search.
     */
    private int maxDescendantPath = 20;

    /**
     * An initial graph, if there is one.
     */
    private Graph initialGraph;

    /**
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();


    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;

    /**
     * The depth of the original CPC search.
     */
    private int cpcDepth = -1;

    /**
     * The depth of CPC orientation.
     */
    private int orientationDepth = -1;

    //=============================CONSTRUCTORS==========================//

    /**
     * Constructs a JPC search with the given independence oracle.
     */
    public Jcpc2(IndependenceTest independenceTest) {
        if (independenceTest == null) {
            throw new NullPointerException();
        }

        this.independenceTest = independenceTest;
    }

    //==============================PUBLIC METHODS========================//

    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }


    public IndependenceTest getIndependenceTest() {
        return independenceTest;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public int getSoftmaxAdjacencies() {
        return softmaxAdjacencies;
    }

    /**
     * Sets the maximum number of adjacencies.
     *
     * @param softmaxAdjacencies
     */
    public void setSoftmaxAdjacencies(int softmaxAdjacencies) {
        if (softmaxAdjacencies < 0) {
            throw new IllegalArgumentException("Adjacencies softmax must be at least 0.");
        }

        this.softmaxAdjacencies = softmaxAdjacencies;
    }

    public void setStartFromEmptyGraph(boolean startFromEmptyGraph) {
        this.startFromEmptyGraph = startFromEmptyGraph;
    }

    public int getMaxDescendantPath() {
        return maxDescendantPath;
    }

    /**
     * Set to 0 to turn off cycle checking.
     */
    public void setMaxDescendantPath(int maxDescendantPath) {
        this.maxDescendantPath = maxDescendantPath;
    }

    public PathBlockingSet getPathBlockingSet() {
        return pathBlockingSet;
    }

    public void setPathBlockingSet(PathBlockingSet pathBlockingSet) {
        if (pathBlockingSet == null) throw new NullPointerException();
        this.pathBlockingSet = pathBlockingSet;
    }


    /**
     * Runs PC starting with a fully connected graph over all of the variables in the domain of the independence test.
     */
    public Graph search() {
        long time1 = System.currentTimeMillis();

        List<Graph> graphs = new ArrayList<Graph>();
        IndependenceTest test = getIndependenceTest();

        Graph graph;

        if (startFromEmptyGraph) {
            graph = new EdgeListGraph(test.getVariables());
        } else {
            if (initialGraph != null) {
                graph = initialGraph;
            } else {
                Cpc search = new Cpc(test);
                search.setKnowledge(getKnowledge());
                search.setDepth(getCpcDepth());
                search.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
                graph = search.search();
            }
        }

        undirectedGraph(graph);

        // This is the list of all changed nodes from the last iteration
        List<Node> _changedNodes = graph.getNodes();

        // This collects up changed nodes in the course of the iteration.
        Set<Node> changedNodes = new HashSet<Node>();

        int count = -1;

        int minNumErrors = Integer.MAX_VALUE;
        Graph outGraph = null;

        LOOP:
        while (++count < getMaxIterations()) {
            TetradLogger.getInstance().log("info", "Round = " + (count + 1));
            System.out.println("Round = " + (count + 1));
            numAdded = 0;
            numRemoved = 0;
            int index = 0;

            int indexBackwards = 0;
            int numEdgesBackwards = graph.getNumEdges();

            int numEdges = _changedNodes.size() * (_changedNodes.size() - 1) / 2;

            for (int i = 0; i < _changedNodes.size(); i++) {
                for (int j = i + 1; j < _changedNodes.size(); j++) {
                    index++;

                    if (index % 10000 == 0) {
                        TetradLogger.getInstance().log("info", index + " of " + numEdges);
                        System.out.println(index + " of " + numEdges);
                    }

                    tryAddingEdge(test, graph, _changedNodes, changedNodes, graph, i, j);

                    Node x = _changedNodes.get(i);
                    Node y = _changedNodes.get(j);

                    if (graph.getAdjacentNodes(x).size() > getSoftmaxAdjacencies()) {
                        for (Node w : graph.getAdjacentNodes(x)) {
                            if (w == y) continue;
                            tryRemovingEdge(test, graph, changedNodes, graph, graph.getEdge(x, w));
                        }
                    }

                    if (graph.getAdjacentNodes(y).size() > getSoftmaxAdjacencies()) {
                        for (Node w : graph.getAdjacentNodes(y)) {
                            if (w == x) continue;
                            tryRemovingEdge(test, graph, changedNodes, graph, graph.getEdge(y, w));
                        }
                    }
                }
            }

            if (getSoftmaxAdjacencies() > 0) {
                for (Edge edge : graph.getEdges()) {
                    if (++indexBackwards % 10000 == 0) {
                        TetradLogger.getInstance().log("info", index + " of " + numEdgesBackwards);
                        System.out.println(index + " of " + numEdgesBackwards);
                    }

                    tryRemovingEdge(test, graph, changedNodes, graph, edge);
                }
            }

            System.out.println("Num added = " + numAdded);
            System.out.println("Num removed = " + numRemoved);
            TetradLogger.getInstance().log("info", "Num added = " + numAdded);
            TetradLogger.getInstance().log("info", "Num removed = " + numRemoved);

            System.out.println("(Reorienting...)");

            int numErrors = numAdded + numRemoved;

            // Keep track of the last graph with the fewest changes; this is returned.
            if (numErrors <= minNumErrors) {
                minNumErrors = numErrors;
                outGraph = cloneGraph(graph);
            }

            orientCpc(graph, getKnowledge(), getOrientationDepth(), test);
            graphs.add(cloneGraph(graph));

            for (int i = graphs.size() - 2; i >= 0; i--) {
                if (graphs.get(graphs.size() - 1).equals(graphs.get(i))) {
                    System.out.println("Recognized previous graph.");
                    outGraph = cloneGraph(graph);
                    break LOOP;
                }
            }

            _changedNodes = new ArrayList<Node>(changedNodes);
            changedNodes.clear();
        }

        this.logger.log("graph", "\nReturning this graph: " + graph);

        long time2 = System.currentTimeMillis();
        this.elapsedTime = time2 - time1;

        orientCpc(outGraph, getKnowledge(), getOrientationDepth(), test);

        return outGraph;
    }

    private Graph cloneGraph(Graph graph) {
        Graph _graph = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            Edge _edge = new Edge(edge.getNode1(), edge.getNode2(), edge.getEndpoint1(), edge.getEndpoint2());
            _graph.addEdge(_edge);
        }

        return _graph;
    }

    private void tryAddingEdge(IndependenceTest test, Graph graph, List<Node> _changedNodes, Set<Node> changedNodes, Graph oldGraph, int i, int j) {
        Node x = _changedNodes.get(i);
        Node y = _changedNodes.get(j);

        if (graph.isAdjacentTo(x, y)) {
            return;
        }

        List<Node> sepsetX, sepsetY;
        boolean existsSepset = false;

        if (getPathBlockingSet() == PathBlockingSet.LARGE) {
            sepsetX = pathBlockingSet(test, oldGraph, x, y);

            if (sepsetX != null) {
                existsSepset = true;
            } else {
                sepsetY = pathBlockingSet(test, oldGraph, y, x);

                if (sepsetY != null) {
                    existsSepset = true;
                }
            }
        } else if (getPathBlockingSet() == PathBlockingSet.SMALL) {
            sepsetX = pathBlockingSetSmall(test, oldGraph, x, y);
            sepsetY = pathBlockingSetSmall(test, oldGraph, y, x);
            existsSepset = sepsetX != null || sepsetY != null;
        } else {
            throw new IllegalStateException("Unrecognized sepset type.");
        }


        if (!existsSepset) {
            if (getKnowledge().edgeForbidden(x.getName(), y.getName()) && getKnowledge().edgeForbidden(y.getName(), x.getName())) {
                return;
            }

            graph.addUndirectedEdge(x, y);
            appendChangedNodes(graph, changedNodes, x, y);
            numAdded++;
        }

        return;
    }

    private void tryRemovingEdge(IndependenceTest test, Graph graph, Set<Node> changedNodes, Graph oldGraph, Edge edge) {
        Node x = edge.getNode1();
        Node y = edge.getNode2();

        List<Node> sepsetX, sepsetY;
        boolean existsSepset = false;

        if (getPathBlockingSet() == PathBlockingSet.LARGE) {
            sepsetX = pathBlockingSet(test, oldGraph, x, y);

            if (sepsetX != null) {
                existsSepset = true;
            } else {
                sepsetY = pathBlockingSet(test, oldGraph, y, x);

                if (sepsetY != null) {
                    existsSepset = true;
                }
            }
        } else if (getPathBlockingSet() == PathBlockingSet.SMALL) {
            sepsetX = pathBlockingSetSmall(test, oldGraph, x, y);
            sepsetY = pathBlockingSetSmall(test, oldGraph, y, x);
            existsSepset = sepsetX != null || sepsetY != null;
        } else {
            throw new IllegalStateException("Unrecognized sepset type.");
        }

        if (existsSepset) {
            if (!getKnowledge().noEdgeRequired(x.getName(), y.getName())) {
                return;
            }

            appendChangedNodes(graph, changedNodes, x, y);
            graph.removeEdges(edge.getNode1(), edge.getNode2());
            numRemoved++;
        }

        return;
    }

    private void appendChangedNodes(Graph graph, Set<Node> changedNodes, Node x, Node y) {
        changedNodes.add(x);
        changedNodes.add(y);

        for (Node _x : graph.getAdjacentNodes(x)) {
            if (graph.getAdjacentNodes(_x).size() > 1) {
                changedNodes.add(_x);
            }
        }

        for (Node _y : graph.getAdjacentNodes(y)) {
            if (graph.getAdjacentNodes(_y).size() > 1) {
                changedNodes.add(_y);
            }
        }

//        changedNodes.addAll(graph.getAdjacentNodes(x));
//        changedNodes.addAll(graph.getAdjacentNodes(y));
    }

    //================================PRIVATE METHODS=======================//

    private List<Node> pathBlockingSet(IndependenceTest test, Graph graph, Node x, Node y) {
        Set<Node> fullSet = pathBlockingSetExcluding(graph, x, y, new HashSet<Node>(), new HashSet<Node>());

        List<Node> commonAdjacents = graph.getAdjacentNodes(x);
        commonAdjacents.retainAll(graph.getAdjacentNodes(y));

        DepthChoiceGenerator generator = new DepthChoiceGenerator(commonAdjacents.size(), commonAdjacents.size());
        int[] choice;

        while ((choice = generator.next()) != null) {
            Set<Node> colliders = new HashSet<Node>(GraphUtils.asList(choice, commonAdjacents));

            List<Node> _descendants = graph.getDescendants(new ArrayList<Node>(colliders));
            _descendants.retainAll(fullSet);
            _descendants.removeAll(colliders);
            Set<Node> descendants = new HashSet<Node>(_descendants);

            Set<Node> sepset = pathBlockingSetExcluding(graph, x, y, colliders, descendants);
            ArrayList<Node> _sepset = new ArrayList<Node>(sepset);

            if (test.isIndependent(x, y, _sepset)) {
                return _sepset;
            }
        }

        return null;
    }

    private List<Node> pathBlockingSetSmall(IndependenceTest test, Graph graph, Node x, Node y) {
        List<Node> adjX = graph.getAdjacentNodes(x);
        adjX.removeAll(graph.getParents(x));
        adjX.removeAll(graph.getChildren(x));

        DepthChoiceGenerator gen = new DepthChoiceGenerator(adjX.size(), -1);
        int[] choice;

        while ((choice = gen.next()) != null) {
            List<Node> selection = GraphUtils.asList(choice, adjX);
            Set<Node> sepset = new HashSet<Node>(selection);
            sepset.addAll(graph.getParents(x));

            sepset.remove(x);
            sepset.remove(y);

            ArrayList<Node> sepsetList = new ArrayList<Node>(sepset);

            if (test.isIndependent(x, y, sepsetList)) {
                return sepsetList;
            }
        }

        return null;
    }

    private Set<Node> pathBlockingSetExcluding(Graph graph, Node x, Node y, Set<Node> colliders, Set<Node> descendants) {
        Set<Node> condSet = new HashSet<Node>();

        for (Node b : graph.getAdjacentNodes(x)) {
//            if (graph.getAdjacentNodes(b).size() == 1) {
//                continue;
//            }

            if (!colliders.contains(b) && !descendants.contains(b)) {
                condSet.add(b);
            }

            if (!graph.isParentOf(b, x) && !colliders.contains(b)) {
                for (Node c : graph.getParents(b)) {
                    if (!colliders.contains(c) && !descendants.contains(c)) {
                        condSet.add(c);
                    }
                }
            }

//            if (colliders.contains(b)) {
//                for (Node parent : graph.getParents(b)) {
//                    if (!colliders.contains(parent) && !descendants.contains(parent)) {
//                        condSet.add(parent);
//                    }
//                }
//            }
        }

        condSet.remove(x);
        condSet.remove(y);

        return condSet;
    }

    private void orientCpc(Graph graph, IKnowledge knowledge, int depth, IndependenceTest test) {
        undirectedGraph(graph);
        SearchGraphUtils.pcOrientbk(knowledge, graph, graph.getNodes());
        orientUnshieldedTriples(graph, test, depth, knowledge);
        MeekRules meekRules = new MeekRules();
        meekRules.setAggressivelyPreventCycles(isAggressivelyPreventCycles());
        meekRules.setKnowledge(knowledge);
        meekRules.orientImplied(graph);
    }

    private void undirectedGraph(Graph graph) {
        for (Edge edge : graph.getEdges()) {
            edge.setEndpoint1(Endpoint.TAIL);
            edge.setEndpoint2(Endpoint.TAIL);
        }
    }

    /**
     * Assumes a graph with only required knowledge orientations.
     */
    private Set<Node> orientUnshieldedTriples(Graph graph, IndependenceTest test, int depth, IKnowledge knowledge) {
        TetradLogger.getInstance().log("info", "Starting Collider Orientation:");

        List<Node> nodes = graph.getNodes();
        Set<Node> colliderNodes = new HashSet<Node>();


        for (Node y : nodes) {
            orientCollidersAboutNode(graph, test, depth, knowledge, colliderNodes, y);
        }

        TetradLogger.getInstance().log("info", "Finishing Collider Orientation.");

        return colliderNodes;
    }

    private void orientCollidersAboutNode(Graph graph, IndependenceTest test, int depth, IKnowledge knowledge,
                                          Set<Node> colliderNodes, Node y) {
        List<Node> adjacentNodes = graph.getAdjacentNodes(y);

        if (adjacentNodes.size() < 2) {
            return;
        }

        ChoiceGenerator cg = new ChoiceGenerator(adjacentNodes.size(), 2);
        int[] combination;

        while ((combination = cg.next()) != null) {
            Node x = adjacentNodes.get(combination[0]);
            Node z = adjacentNodes.get(combination[1]);

            if (graph.isAdjacentTo(x, z)) {
                continue;
            }

            SearchGraphUtils.CpcTripleType type = SearchGraphUtils.getCpcTripleType2(x, y, z, test, depth, graph);

            if (type == SearchGraphUtils.CpcTripleType.COLLIDER &&
                    isArrowpointAllowed(x, y, knowledge) &&
                    isArrowpointAllowed(z, y, knowledge)) {
                graph.setEndpoint(x, y, Endpoint.ARROW);
                graph.setEndpoint(z, y, Endpoint.ARROW);

                colliderNodes.add(y);
                TetradLogger.getInstance().log("colliderOrientations", SearchLogUtils.colliderOrientedMsg(x, y, z));
//                    System.out.println(SearchLogUtils.colliderOrientedMsg(x, y, z));
            } else if (type == SearchGraphUtils.CpcTripleType.AMBIGUOUS) {
                Triple triple = new Triple(x, y, z);
                graph.addAmbiguousTriple(triple.getX(), triple.getY(), triple.getZ());
            }
        }
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            throw new IllegalArgumentException("Number of graph correction iterations must be >= 0: " + maxIterations);
        }

        this.maxIterations = maxIterations;
    }

    public void setInitialGraph(Graph initialGraph) {
        this.initialGraph = initialGraph;
    }

    public Set<Triple> getColliderTriples(Graph graph) {
        Set<Triple> triples = new HashSet<Triple>();

        for (Node node : graph.getNodes()) {
            List<Node> nodesInto = graph.getNodesInTo(node, Endpoint.ARROW);

            if (nodesInto.size() < 2) continue;

            ChoiceGenerator gen = new ChoiceGenerator(nodesInto.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                triples.add(new Triple(nodesInto.get(choice[0]), node, nodesInto.get(choice[1])));
            }
        }

        return triples;
    }


    public boolean existsDirectedPathFromTo(Graph graph, Node node1, Node node2) {
        return existsDirectedPathVisit(graph, node1, node2, new LinkedList<Node>());
    }

    private boolean existsDirectedPathVisit(Graph graph, Node node1, Node node2,
                                            LinkedList<Node> path) {
        if (graph.getAdjacentNodes(node1).size() <= 6 && path.size() > getMaxDescendantPath()) {
            return false;
        } else if (graph.getAdjacentNodes(node1).size() > 6) {
            return false;
        }

        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseDirected(node1, edge);

            if (child == null) {
                continue;
            }

            if (child == node2) {
                return true;
            }

            if (path.contains(child)) {
                continue;
            }

            if (existsDirectedPathVisit(graph, child, node2, path)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    public void setCpcDepth(int cpcDepth) {
        if (cpcDepth < -1) {
            throw new IllegalArgumentException();
        }

        this.cpcDepth = cpcDepth;
    }

    public int getCpcDepth() {
        return cpcDepth;
    }

    public int getOrientationDepth() {
        return orientationDepth;
    }

    /**
     * Checks if an arrowpoint is allowed by background knowledge.
     */
    public static boolean isArrowpointAllowed(Object from, Object to,
                                              IKnowledge knowledge) {
        if (knowledge == null) {
            return true;
        }
        return !knowledge.edgeRequired(to.toString(), from.toString()) &&
                !knowledge.edgeForbidden(from.toString(), to.toString());
    }
}
