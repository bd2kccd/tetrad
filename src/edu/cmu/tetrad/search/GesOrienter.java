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

import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.log;

/**
 * GesSearch is an implentation of the GES algorithm, as specified in Chickering (2002) "Optimal structure
 * identification with greedy search" Journal of Machine Learning Research. It works for both BayesNets and SEMs.
 * <p/>
 * Some code optimization could be done for the scoring part of the graph for discrete models (method scoreGraphChange).
 * Some of Andrew Moore's approaches for caching sufficient statistics, for instance.
 *
 * @author Ricardo Silva, Summer 2003
 * @author Joseph Ramsey, Revisions 10/2005
 */

public final class GesOrienter implements GraphSearch, GraphScorer, Reorienter {

    /**
     * The data set, various variable subsets of which are to be scored.
     */
    private DataSet dataSet;

    /**
     * The covariance matrix for the data set.
     */
    private TetradMatrix covariances;

    /**
     * Sample size, either from the data set or from the variances.
     */
    private int sampleSize;

    /**
     * Specification of forbidden and required edges.
     */
    private Knowledge knowledge = new Knowledge();

    /**
     * Map from variables to their column indices in the data set.
     */
    private HashMap<Node, Integer> hashIndices;

    /**
     * Array of variable names from the data set, in order.
     */
    private String varNames[];

    /**
     * List of variables in the data set, in order.
     */
    private List<Node> variables;

    /**
     * True iff the data set is discrete.
     */
    private boolean discrete;

    /**
     * The true graph, if known. If this is provided, asterisks will be printed out next to false positive added edges
     * (that is, edges added that aren't adjacencies in the true graph).
     */
    private Graph trueGraph;

    /**
     * For formatting printed numbers.
     */
    private final NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * Caches scores for discrete search.
     */
    private final LocalScoreCache localScoreCache = new LocalScoreCache();

    /**
     * Elapsed time of the most recent search.
     */
    private long elapsedTime;

    /**
     * True if cycles are to be aggressively prevented. May be expensive for large graphs (but also useful for large
     * graphs).
     */
    private boolean aggressivelyPreventCycles = false;

    /**
     * Listeners for graph change events.
     */
    private transient List<PropertyChangeListener> listeners;

    /**
     * Penalty discount--the BIC penalty is multiplied by this (for continuous variables).
     */
    private double penaltyDiscount = 1.0;

    /**
     * The score for discrete searches.
     */
    private LocalDiscreteScore discreteScore;

    /**
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * The top n graphs found by the algorithm, where n is <code>numPatternsToStore</code>.
     */
    private SortedSet<ScoredGraph> topGraphs = new TreeSet<ScoredGraph>();

    /**
     * The number of top patterns to store.
     */
    private int numPatternsToStore = 10;

    private SortedSet<Arrow> sortedArrows = new TreeSet<Arrow>();
    private Set<Arrow>[][] lookupArrows;
    private Map<Node, Map<Set<Node>, Double>> scoreHash;
    private Map<Node, Integer> nodesHash;

    /**
     * True if graphs should be stored.
     */
    private boolean storeGraphs = true;
    private Set<Edge> meekOriented = new HashSet<Edge>();
    private Set<Edge> extraMeekOriented = new HashSet<Edge>();
    private Graph graphToOrient;
    private ICovarianceMatrix covarianceMatrix;

    //===========================CONSTRUCTORS=============================//

    public GesOrienter(DataSet dataSet) {
        setDataSet(dataSet);
        if (dataSet.isDiscrete()) {
            setDiscreteScore(new BDeuScore(dataSet, 10, .001));
//            discreteScore = new MdluScore(dataSet, .001); // Unpublished
        }
        setStructurePrior(0.001);
        setSamplePrior(10.);
    }

    public GesOrienter(ICovarianceMatrix covMatrix) {
        setCovMatrix(covMatrix);
        setStructurePrior(0.001);
        setSamplePrior(10.);
    }

    @Override
    public void orient(Graph graph) {
        this.graphToOrient = new EdgeListGraph(graph);
        this.graphToOrient = GraphUtils.undirectedGraph(this.graphToOrient);

        Graph _graph = search();

        graphToOrient.removeEdges(graphToOrient.getEdges());

        for (Edge edge : _graph.getEdges()) {
            graphToOrient.addEdge(edge);
        }
    }

//==========================PUBLIC METHODS==========================//


    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    /**
     * Greedy equivalence search: Start from the empty graph, add edges till model is significant. Then start deleting
     * edges till a minimum is achieved.
     *
     * @return the resulting Pattern.
     */
    public Graph search() {

        long startTime = System.currentTimeMillis();

        // Check for missing values.
        if (covariances != null && DataUtils.containsMissingValue(covariances)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }

        // Check for missing values.
        if (dataSet != null && DataUtils.containsMissingValue(dataSet)) {
            throw new IllegalArgumentException(
                    "Please remove or impute missing values first.");
        }


        Graph graph = new EdgeListGraph(new LinkedList<Node>(getVariables()));

        scoreHash = new WeakHashMap<Node, Map<Set<Node>, Double>>();

        for (Node node : graph.getNodes()) {
            scoreHash.put(node, new HashMap<Set<Node>, Double>());
        }


        fireGraphChange(graph);
        buildIndexing(graph);
        addRequiredEdges(graph);

        double score = scoreGraph(graph);

        storeGraph(new EdgeListGraph(graph), score);

        List<Node> nodes = graph.getNodes();

        nodesHash = new HashMap<Node, Integer>();
        int index = -1;

        for (Node node : nodes) {
            nodesHash.put(node, ++index);
        }

        // Do forward search.
        score = fes(graph, nodes, score);

        // Do backward search.
        score = bes(graph, nodes, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;
        this.logger.log("graph", "\nReturning this graph: " + graph);
        TetradLogger.getInstance().log("info", "Final Model BIC = " + nf.format(score));

        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.flush();

        return graph;
    }

    public Graph search(List<Node> nodes) {
        long startTime = System.currentTimeMillis();
        localScoreCache.clear();

        if (!dataSet().getVariables().containsAll(nodes)) {
            throw new IllegalArgumentException(
                    "All of the nodes must be in " + "the supplied data set.");
        }

        Graph graph = new EdgeListGraph(nodes);
        buildIndexing(graph);
        addRequiredEdges(graph);
        double score = 0; //scoreGraph(graph);

        // Do forward search.
        score = fes(graph, nodes, score);

        // Do backward search.
        score = bes(graph, nodes, score);

        long endTime = System.currentTimeMillis();
        this.elapsedTime = endTime - startTime;
        this.logger.log("graph", "\nReturning this graph: " + graph);
        TetradLogger.getInstance().log("info", "Final Model BIC = " + nf.format(score));

        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.flush();

        return graph;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the background knowledge.
     *
     * @param knowledge the knowledge object, specifying forbidden and required edges.
     */
    public void setKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Knowledge must not be null.");
        }
        this.knowledge = knowledge;
    }

    public void setStructurePrior(double structurePrior) {
        if (getDiscreteScore() != null) {
            getDiscreteScore().setStructurePrior(structurePrior);
        }
    }

    public void setSamplePrior(double samplePrior) {
        if (getDiscreteScore() != null) {
            getDiscreteScore().setSamplePrior(samplePrior);
        }
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getListeners().add(l);
    }

    public double getPenaltyDiscount() {
        return penaltyDiscount;
    }

    public void setPenaltyDiscount(double penaltyDiscount) {
        if (penaltyDiscount < 0) {
            throw new IllegalArgumentException("Penalty discount must be >= 0: "
                    + penaltyDiscount);
        }

        this.penaltyDiscount = penaltyDiscount;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public double getScore(Graph dag) {
        return scoreGraph(dag);
    }

    public SortedSet<ScoredGraph> getTopGraphs() {
        return topGraphs;
    }

    public int getNumPatternsToStore() {
        return numPatternsToStore;
    }

    public void setNumPatternsToStore(int numPatternsToStore) {
        if (numPatternsToStore < 1) {
            throw new IllegalArgumentException("Must store at least one pattern: " + numPatternsToStore);
        }

        this.numPatternsToStore = numPatternsToStore;
    }

    public boolean isStoreGraphs() {
        return storeGraphs;
    }

    public void setStoreGraphs(boolean storeGraphs) {
        this.storeGraphs = storeGraphs;
    }

    public LocalDiscreteScore getDiscreteScore() {
        return discreteScore;
    }

    public void setDiscreteScore(LocalDiscreteScore discreteScore) {
        if (discreteScore.getDataSet() != dataSet) {
            throw new IllegalArgumentException("Must use the same data set.");
        }
        this.discreteScore = discreteScore;
    }


    //===========================PRIVATE METHODS========================//

    /**
     * Forward equivalence search.
     *
     * @param graph The graph in the state prior to the forward equivalence search.
     * @param score The score in the state prior to the forward equivalence search
     * @return the score in the state after the forward equivelance search. Note that the graph is changed as a
     * side-effect to its state after the forward equivelance search.
     */
    private double fes(Graph graph, List<Node> nodes, double score) {
        TetradLogger.getInstance().log("info", "** FORWARD EQUIVALENCE SEARCH");
        TetradLogger.getInstance().log("info", "Initial Model BIC = " + nf.format(score));

        initializeArrowsForward(nodes, graph);

        while (!sortedArrows.isEmpty()) {
            Arrow arrow = sortedArrows.first();
            sortedArrows.remove(arrow);

            Node x = nodes.get(arrow.getX());
            Node y = nodes.get(arrow.getY());

            if (graph.isAdjacentTo(x, y)) {
                System.out.println("One slipped by with x and y adjacent! (Forward search)");
                continue;
            }

            if (!validInsert(x, y, arrow.getHOrT(), arrow.getNaYX(), graph)) {
                continue;
            }

            Set<Node> t = arrow.getHOrT();
            double bump = arrow.getBump();

            score = score + bump;
            boolean success = insert(x, y, t, graph, score, true, bump, nodes);

            if (!success) continue;

            rebuildPattern(graph);

            // Try to avoid duplicating scoring calls. First clear out all of the edges that need to be changed,
            // then change them, checking to see if they're already been changed. I know, roundabout, but there's
            // a performance boost.
            clearForward(graph, nodes, nodesHash.get(x), nodesHash.get(y));

            for (Node node : arrow.getHOrT()) {
                clearForward(graph, nodes, nodesHash.get(node), nodesHash.get(y));
            }

            for (Edge edge : extraMeekOriented) {
                int i = nodesHash.get(edge.getNode1());
                int j = nodesHash.get(edge.getNode2());
                clearForward(graph, nodes, i, j);
            }

            reevaluateForward(graph, nodes, nodesHash.get(x), nodesHash.get(y));

            for (Node node : arrow.getHOrT()) {
                reevaluateForward(graph, nodes, nodesHash.get(node), nodesHash.get(y));
            }

            for (Edge edge : extraMeekOriented) {
                int i = nodesHash.get(edge.getNode1());
                int j = nodesHash.get(edge.getNode2());
                reevaluateForward(graph, nodes, i, j);
            }

            storeGraph(graph, score);
        }

        return score;
    }

    private double bes(Graph graph, List<Node> nodes, double score) {
        TetradLogger.getInstance().log("info", "** BACKWARD EQUIVALENCE SEARCH");
        TetradLogger.getInstance().log("info", "Initial Model BIC = " + nf.format(score));

        initializeArrowsBackward(graph);

        while (!sortedArrows.isEmpty()) {
            Arrow arrow = sortedArrows.first();
            sortedArrows.remove(arrow);

            Node x = nodes.get(arrow.getX());
            Node y = nodes.get(arrow.getY());

//            checkLocalModelConsistency(graph, arrow, x, y);

            if (!validDelete(arrow.getHOrT(), arrow.getNaYX(), graph)) {
                continue;
            }

            Set<Node> h = arrow.getHOrT();
            double bump = arrow.getBump();

            score = score + bump;
            delete(x, y, h, graph, score, true, bump, nodes);
            rebuildPattern(graph);

            storeGraph(graph, score);

            initializeArrowsBackward(graph);  // Rebuilds Arrows from scratch each time. Fast enough for backwards.
        }

        return score;
    }

    // Checks to make sure the local model about y is the same now as when the arrow was created.
    private void checkLocalModelConsistency(Graph graph, Arrow arrow, Node x, Node y) {
        if (!getNaYX(x, y, graph).equals(arrow.getNaYX())) {
            System.out.println("Caught: " + x + " ...  " + y + " " + graph.getEdge(x, y));
            System.out.println("Old NaYX = " + arrow.getNaYX());
            System.out.println("New NaYX = " + getNaYX(x, y, graph));
        }

        if (!new HashSet<Node>(getTNeighbors(x, y, graph)).equals(arrow.gettNeighbors())) {
            System.out.println("Caught: " + x + " ...  " + y + " " + graph.getEdge(x, y));
            System.out.println("Old T Neighbors = " + arrow.gettNeighbors());
            System.out.println("New T Neighbors = " + getTNeighbors(x, y, graph));
        }

        if (!new HashSet<Node>(graph.getChildren(y)).equals(arrow.getChildren())) {
            System.out.println("Caught: " + x + " ...  " + y + " " + graph.getEdge(x, y));
            System.out.println("New Children = " + arrow.getChildren());
            System.out.println("New Children = " + graph.getChildren(y));
        }
    }

    private void initializeArrowsForward(List<Node> nodes, Graph graph) {
        sortedArrows.clear();
        lookupArrows = new HashSet[nodes.size()][nodes.size()];

        for (int j = 0; j < nodes.size(); j++) {
            for (int i = 0; i < nodes.size(); i++) {
                if (j == i) continue;

                Node _x = nodes.get(i);
                Node _y = nodes.get(j);

                if (graph.isAdjacentTo(_x, _y)) continue;

                if (!graphToOrient.isAdjacentTo(_x, _y)) {
                    continue;
                }

                if (getKnowledge().edgeForbidden(_x.getName(), _y.getName())) {
                    continue;
                }

                calculateArrowsForward(i, j, nodes, graph);
            }
        }
    }

    private void initializeArrowsBackward(Graph graph) {
        List<Node> nodes = graph.getNodes();
        sortedArrows.clear();
        lookupArrows = new HashSet[nodes.size()][nodes.size()];

        for (Edge edge : graph.getEdges()) {
            Node x = edge.getNode1();
            Node y = edge.getNode2();

            int i = nodesHash.get(edge.getNode1());
            int j = nodesHash.get(edge.getNode2());

            if (!getKnowledge().noEdgeRequired(x.getName(), y.getName())) {
                continue;
            }

            if (Edges.isDirectedEdge(edge)) {
                calculateArrowsBackward(i, j, nodes, graph);
            } else {
                calculateArrowsBackward(i, j, nodes, graph);
                calculateArrowsBackward(j, i, nodes, graph);
            }
        }

        List<Arrow> _sortedArrows = new ArrayList<Arrow>(sortedArrows);
        Collections.sort(_sortedArrows);

//        for (int i = 0; i < _sortedArrows.size(); i++) {
//            System.out.println((i + 1) + ". " + _sortedArrows.get(i));
//        }
//
//        System.out.println();
    }

    private void clearForward(Graph graph, List<Node> nodes, int i, int j) {
        Node x = nodes.get(i);
        Node y = nodes.get(j);

        if (!graph.isAdjacentTo(x, y)) throw new IllegalArgumentException();

        clearArrow(i, j);
        clearArrow(j, i);

        for (int _w = 0; _w < nodes.size(); _w++) {
            Node w = nodes.get(_w);
            if (w == x) continue;
            if (w == y) continue;

            if (!graph.isAdjacentTo(w, x)) {
                clearArrow(_w, i);

                if (graph.isAdjacentTo(w, y)) {
                    clearArrow(i, _w);
                }
            }

            if (!graph.isAdjacentTo(w, y)) {
                clearArrow(_w, j);

                if (graph.isAdjacentTo(w, x)) {
                    clearArrow(j, _w);
                }
            }
        }
    }

    private void reevaluateForward(Graph graph, List<Node> nodes, int i, int j) {
        Node x = nodes.get(i);
        Node y = nodes.get(j);

        if (!graph.isAdjacentTo(x, y)) throw new IllegalArgumentException();

        clearArrow(i, j);
        clearArrow(j, i);

        for (int _w = 0; _w < nodes.size(); _w++) {
            Node w = nodes.get(_w);
            if (w == x) continue;
            if (w == y) continue;

            if (!graph.isAdjacentTo(w, x)) {
                if (lookupArrows[_w][i] == null) {
                    calculateArrowsForward(_w, i, nodes, graph);
                }

                if (graph.isAdjacentTo(w, y)) {
                    if (lookupArrows[i][_w] == null) {
                        calculateArrowsForward(i, _w, nodes, graph);
                    }
                }
            }

            if (!graph.isAdjacentTo(w, y)) {
                if (lookupArrows[_w][j] == null) {
                    calculateArrowsForward(_w, j, nodes, graph);
                }

                if (graph.isAdjacentTo(w, x)) {
                    if (lookupArrows[j][_w] == null) {
                        calculateArrowsForward(j, _w, nodes, graph);
                    }
                }
            }
        }
    }

    private void reevaluateBackward(Graph graph, List<Node> nodes, int i, int j) {
        Node x = nodes.get(i);
        Node y = nodes.get(j);

        if (graph.isAdjacentTo(x, y)) {
            throw new IllegalArgumentException();
        }

        clearArrow(i, j);
        clearArrow(j, i);

        for (Node w : graph.getAdjacentNodes(x)) {
            int _w = nodesHash.get(w);

            calculateArrowsBackward(_w, i, nodes, graph);
            calculateArrowsBackward(i, _w, nodes, graph);
        }

        for (Node w : graph.getAdjacentNodes(y)) {
            int _w = nodesHash.get(w);

            calculateArrowsBackward(_w, j, nodes, graph);
            calculateArrowsBackward(j, _w, nodes, graph);
        }
    }

    private void clearArrow(int i, int j) {
        if (lookupArrows[i][j] != null) {
            sortedArrows.removeAll(lookupArrows[i][j]);
            lookupArrows[i][j] = null;
        }
    }

    private void calculateArrowsForward(int i, int j, List<Node> nodes, Graph graph) {
        if (i == j) {
            return;
        }

        Node _x = nodes.get(i);
        Node _y = nodes.get(j);

        if (graph.isAdjacentTo(_x, _y)) {
            return;
        }

        if (!graphToOrient.isAdjacentTo(_x, _y)) {
            return;
        }

        if (getKnowledge().edgeForbidden(_x.getName(), _y.getName())) {
            return;
        }

        clearArrow(i, j);

        Set<Node> naYX = new HashSet<Node>(getNaYX(_x, _y, graph));
        Set<Node> parents = new HashSet<Node>(graph.getParents(_y));
        Set<Node> children = new HashSet<Node>(graph.getChildren(_y));
        Set<Node> tNeighbors = new HashSet<Node>(getTNeighbors(_x, _y, graph));

        Set<Node> together = new HashSet<Node>();
        List<Node> _tNeighbors = new ArrayList<Node>(tNeighbors);

        lookupArrows[i][j] = new HashSet<Arrow>();

        for (Node n : _tNeighbors) {
            Set<Node> t = Collections.singleton(n);

            if (!validSetByKnowledge(_y, t)) {
                continue;
            }

            double bump = insertEval(_x, _y, t, naYX, graph);

            if (bump > 0.0) {
                together.addAll(t);
                Arrow arrow = new Arrow(bump, i, j, t, nodes, naYX, parents, children, tNeighbors);
                sortedArrows.add(arrow);
                lookupArrows[i][j].add(arrow);
            }
        }

        List<Node> _together = new ArrayList<Node>(together);

        DepthChoiceGenerator gen = new DepthChoiceGenerator(_together.size(), _together.size());
        int[] choice;

        while ((choice = gen.next()) != null) {
            if (choice.length == 1) continue;

            Set<Node> t = new HashSet<Node>(GraphUtils.asList(choice, _together));
            if (!validSetByKnowledge(_y, t)) {
                continue;
            }

            double bump = insertEval(_x, _y, t, naYX, graph);

            if (bump > 0.0) {
                Arrow arrow = new Arrow(bump, i, j, t, nodes, naYX, parents, children, tNeighbors);
                sortedArrows.add(arrow);
                lookupArrows[i][j].add(arrow);
            }
        }
    }

    private void calculateArrowsBackward(int i, int j, List<Node> nodes, Graph graph) {
        if (i == j) {
            return;
        }

        Node x = nodes.get(i);
        Node y = nodes.get(j);

        if (!graph.isAdjacentTo(x, y)) {
            return;
        }

        if (!getKnowledge().noEdgeRequired(x.getName(), y.getName())) {
            return;
        }

        Set<Node> naYX = getNaYX(x, y, graph);

        clearArrow(i, j);

        List<Node> _naYX = new ArrayList<Node>(naYX);
        DepthChoiceGenerator gen = new DepthChoiceGenerator(_naYX.size(), _naYX.size());
        int[] choice;
        lookupArrows[i][j] = new HashSet<Arrow>();

        Set<Node> parents = new HashSet<Node>(graph.getParents(y));
        Set<Node> children = new HashSet<Node>(graph.getChildren(y));
        Set<Node> tNeighbors = new HashSet<Node>(getTNeighbors(x, y, graph));

        while ((choice = gen.next()) != null) {
            Set<Node> H = new HashSet<Node>(GraphUtils.asList(choice, _naYX));

            if (!validSetByKnowledge(y, H)) {
                continue;
            }

            double bump = deleteEval(x, y, H, naYX, graph);
//            System.out.println(bump);

            if (bump > 0) {
                Arrow arrow = new Arrow(bump, i, j, H, nodes, naYX, parents, children, tNeighbors);
                sortedArrows.add(arrow);
                lookupArrows[i][j].add(arrow);
            }
        }
    }

    private static class Arrow implements Comparable {
        private double bump;
        private int x;
        private int y;
        private Set<Node> hOrT;
        private List<Node> nodes;
        private Set<Node> naYX;
        private Set<Node> parents;
        private Set<Node> tNeighbors;
        private Set<Node> children;

        public Arrow(double bump, int x, int y, Set<Node> hOrT, List<Node> nodes, Set<Node> naYX, Set<Node> parents,
                     Set<Node> children, Set<Node> tNeighbors) {
            this.bump = bump;
            this.x = x;
            this.y = y;
            this.hOrT = hOrT;
            this.nodes = nodes;
            this.naYX = naYX;
            this.parents = parents;
            this.children = children;
            this.tNeighbors = tNeighbors;
        }

        public double getBump() {
            return bump;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Set<Node> getHOrT() {
            return hOrT;
        }

        public Set<Node> getNaYX() {
            return naYX;
        }

        public Set<Node> getParents() {
            return parents;
        }

        public Set<Node> gettNeighbors() {
            return tNeighbors;
        }

        // Sorting is by bump, high to low.
        public int compareTo(Object o) {
            Arrow arrow = (Arrow) o;
            return Double.compare(arrow.getBump(), getBump());
        }

        public String toString() {
            return "Arrow<" + nodes.get(x) + "->" + nodes.get(y) + " bump = " + bump + " t/h = " + hOrT + " naYX = " + naYX
                    + " parents = " + parents + " children = " + children + " tNeighbors = " + tNeighbors + ">";
        }

        public Set<Node> getChildren() {
            return children;
        }
    }


    /**
     * Get all nodes that are connected to Y by an undirected edge and not adjacent to X.
     */
    private static List<Node> getTNeighbors(Node x, Node y, Graph graph) {
        List<Node> tNeighbors = graph.getAdjacentNodes(y);
        tNeighbors.removeAll(graph.getAdjacentNodes(x));

        for (int i = tNeighbors.size() - 1; i >= 0; i--) {
            Node z = tNeighbors.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                tNeighbors.remove(z);
            }
        }

        return tNeighbors;
    }

    /**
     * Evaluate the Insert(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double insertEval(Node x, Node y, Set<Node> t, Set<Node> naYX, Graph graph) {
        List<Node> paY = graph.getParents(y);
//        paY.remove(x);
        Set<Node> paYPlusX = new HashSet<Node>(paY);
        paYPlusX.add(x);

        Set<Node> set1 = new HashSet<Node>(naYX);
        set1.addAll(t);
        set1.addAll(paYPlusX);

        Set<Node> set2 = new HashSet<Node>(naYX);
        set2.addAll(t);
        set2.addAll(paY);

        return scoreGraphChange(y, set1, set2);
    }

    /**
     * Evaluate the Delete(X, Y, T) operator (Definition 12 from Chickering, 2002).
     */
    private double deleteEval(Node x, Node y, Set<Node> h, Set<Node> naYX, Graph graph) {
        List<Node> paY = graph.getParents(y);
//        paY.add(x);
        Set<Node> paYMinuxX = new HashSet<Node>(paY);
        paYMinuxX.remove(x);

        Set<Node> set1 = new HashSet<Node>(naYX);
        set1.removeAll(h);
        set1.addAll(paYMinuxX);

        Set<Node> set2 = new HashSet<Node>(naYX);
        set2.removeAll(h);
        set2.addAll(paY);

        return scoreGraphChange(y, set1, set2);
    }

    /*
    * Do an actual insertion
    * (Definition 12 from Chickering, 2002).
    **/

    private boolean insert(Node x, Node y, Set<Node> t, Graph graph, double score, boolean log, double bump, List<Node> nodes) {
        if (graph.isAdjacentTo(x, y)) {
            throw new IllegalArgumentException(x + " and " + y + " are already adjacent in the graph.");
        }

        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        graph.addDirectedEdge(x, y);

        if (log) {
            String label = trueGraph != null && trueEdge != null ? "*" : "";
            TetradLogger.getInstance().log("insertedEdges", graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) +
                    " " + t +
                    " (" + nf.format(score) + ") " + label);
            System.out.println(graph.getNumEdges() + ". INSERT " + graph.getEdge(x, y) +
                    " " + t +
                    " (" + nf.format(score) + ", " + bump + ") " + label);
        }

        for (Node _t : t) {
            Edge oldEdge = graph.getEdge(_t, y);

            if (oldEdge == null) throw new IllegalArgumentException("Not adjacent: " + _t + ", " + y);

            if (!Edges.isUndirectedEdge(oldEdge)) {
                throw new IllegalArgumentException("Should be undirected: " + oldEdge);
            }

            graph.removeEdge(_t, y);
            graph.addDirectedEdge(_t, y);

            if (log) {
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(_t, y));
                System.out.println("--- Directing " + oldEdge + " to " +
                        graph.getEdge(_t, y));
            }
        }

        return true;
    }

    /**
     * Do an actual deletion (Definition 13 from Chickering, 2002).
     */
    private void delete(Node x, Node y, Set<Node> subset, Graph graph, double score, boolean log, double bump, List<Node> nodes) {

        Edge trueEdge = null;

        if (trueGraph != null) {
            Node _x = trueGraph.getNode(x.getName());
            Node _y = trueGraph.getNode(y.getName());
            trueEdge = trueGraph.getEdge(_x, _y);
        }

        if (log) {
            Edge oldEdge = graph.getEdge(x, y);

            String label = trueGraph != null && trueEdge != null ? "*" : "";
            TetradLogger.getInstance().log("deletedEdges", (graph.getNumEdges() - 1) + ". DELETE " + oldEdge +
                    " " + subset +
                    " (" + nf.format(score) + ") " + label);
            System.out.println((graph.getNumEdges() - 1) + ". DELETE " + oldEdge +
                    " " + subset +
                    " (" + nf.format(score) + ", " + bump + ") " + label);
        }

        graph.removeEdge(x, y);

        for (Node h : subset) {
            graph.removeEdge(y, h);
            graph.addDirectedEdge(y, h);

            if (log) {
                Edge oldEdge = graph.getEdge(y, h);
                TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                        graph.getEdge(y, h));
            }

            if (Edges.isUndirectedEdge(graph.getEdge(x, h))) {
                if (!graph.isAdjacentTo(x, h)) throw new IllegalArgumentException("Not adjacent: " + x + ", " + h);

                graph.removeEdge(x, h);
                graph.addDirectedEdge(x, h);

                if (log) {
                    Edge oldEdge = graph.getEdge(x, h);
                    TetradLogger.getInstance().log("directedEdges", "--- Directing " + oldEdge + " to " +
                            graph.getEdge(x, h));
                }
            }
        }
    }

    /*
     * Test if the candidate insertion is a valid operation
     * (Theorem 15 from Chickering, 2002).
     **/

    private boolean validInsert(Node x, Node y, Set<Node> t, Set<Node> naYX, Graph graph) {
        Set<Node> union = new HashSet<Node>(t);
        union.addAll(naYX);

        if (!isClique(union, graph)) {
            return false;
        }

        if (existsUnblockedSemiDirectedPath(y, x, union, graph)) {
            return false;
        }

        return true;
    }

    /**
     * Test if the candidate deletion is a valid operation (Theorem 17 from Chickering, 2002).
     */
    private static boolean validDelete(Set<Node> h, Set<Node> naXY, Graph graph) {
        Set<Node> set = new HashSet<Node>(naXY);
        set.removeAll(h);
        return isClique(set, graph);
    }

    //---Background knowledge methods.

    private void addRequiredEdges(Graph graph) {
        for (Iterator<KnowledgeEdge> it =
                     this.getKnowledge().requiredEdgesIterator(); it.hasNext(); ) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (!graph.isAncestorOf(nodeB, nodeA)) {
                graph.removeEdges(nodeA, nodeB);
                graph.addDirectedEdge(nodeA, nodeB);
                TetradLogger.getInstance().log("insertedEdges", "Adding edge by knowledge: " + graph.getEdge(nodeA, nodeB));
            }
        }
        for (Iterator<KnowledgeEdge> it = getKnowledge().forbiddenEdgesIterator(); it.hasNext(); ) {
            KnowledgeEdge next = it.next();
            String a = next.getFrom();
            String b = next.getTo();
            Node nodeA = null, nodeB = null;
            Iterator<Node> itn = graph.getNodes().iterator();
            while (itn.hasNext() && (nodeA == null || nodeB == null)) {
                Node nextNode = itn.next();
                if (nextNode.getName().equals(a)) {
                    nodeA = nextNode;
                }
                if (nextNode.getName().equals(b)) {
                    nodeB = nextNode;
                }
            }
            if (nodeA != null && nodeB != null && graph.isAdjacentTo(nodeA, nodeB) &&
                    !graph.isChildOf(nodeA, nodeB)) {
                if (!graph.isAncestorOf(nodeA, nodeB)) {
                    graph.removeEdges(nodeA, nodeB);
                    graph.addDirectedEdge(nodeB, nodeA);
                    TetradLogger.getInstance().log("insertedEdges", "Adding edge by knowledge: " + graph.getEdge(nodeB, nodeA));
                }
            }
        }
    }

    /**
     * Use background knowledge to decide if an insert or delete operation does not orient edges in a forbidden
     * direction according to prior knowledge. If some orientation is forbidden in the subset, the whole subset is
     * forbidden.
     */
    private boolean validSetByKnowledge(Node y, Set<Node> subset) {
        for (Node node : subset) {
            if (getKnowledge().edgeForbidden(node.getName(), y.getName())) {
                return false;
            }
        }
        return true;
    }

    //--Auxiliary methods.

    /**
     * Find all nodes that are connected to Y by an undirected edge that are adjacent to X (that is, by undirected or
     * directed edge).
     */
    private static Set<Node> getNaYX(Node x, Node y, Graph graph) {
        List<Node> naYX = graph.getAdjacentNodes(y);
        naYX.retainAll(graph.getAdjacentNodes(x));

        for (int i = naYX.size() - 1; i >= 0; i--) {
            Node z = naYX.get(i);
            Edge edge = graph.getEdge(y, z);

            if (!Edges.isUndirectedEdge(edge)) {
                naYX.remove(z);
            }
        }

        return new HashSet<Node>(naYX);
    }

    /**
     * Returns true iif the given set forms a clique in the given graph.
     */
    private static boolean isClique(Set<Node> _nodes, Graph graph) {
        List<Node> nodes = new LinkedList<Node>(_nodes);
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i; j < nodes.size(); j++) {
                if (i == j && graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    throw new IllegalArgumentException();
                }

                if (!graph.isAdjacentTo(nodes.get(i), nodes.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean existsUnblockedSemiDirectedPath(Node node1, Node node2, Set<Node> cond, Graph graph) {
        return existsUnblockedSemiDirectedPathVisit(node1, node2,
                new LinkedList<Node>(), graph, cond);
    }

    private boolean existsUnblockedSemiDirectedPathVisit(Node node1, Node node2,
                                                         LinkedList<Node> path, Graph graph, Set<Node> cond) {
        if (node1 == node2) return true;
        if (node1 == null) return false;
        if (path.contains(node1)) return false;
        if (cond.contains(node1)) return false;
        path.addLast(node1);

        for (Edge edge : graph.getEdges(node1)) {
            Node child = Edges.traverseSemiDirected(node1, edge);

//            if (child == null && !cond.contains(node1)) continue;
//            if (child != null && cond.contains(node1)) continue;

            if (existsUnblockedSemiDirectedPathVisit(child, node2, path, graph, cond)) {
                return true;
            }
        }

        path.removeLast();
        return false;
    }

    /**
     * Completes a pattern that was modified by an insertion/deletion operator Based on the algorithm described on
     * Appendix C of (Chickering, 2002).
     */
    private void rebuildPattern(Graph graph) {
        SearchGraphUtils.basicPattern(graph);
        addRequiredEdges(graph);
        meekOrient(graph, getKnowledge());

        TetradLogger.getInstance().log("rebuiltPatterns", "Rebuilt pattern = " + graph);
    }

    /**
     * Fully direct a graph with background knowledge. I am not sure how to adapt Chickering's suggested algorithm above
     * (dagToPdag) to incorporate background knowledge, so I am also implementing this algorithm based on Meek's 1995
     * UAI paper. Notice it is the same implemented in PcSearch. </p> *IMPORTANT!* *It assumes all colliders are
     * meekOriented, as well as arrows dictated by time order.*
     */
    private void meekOrient(Graph graph, Knowledge knowledge) {
        MeekRules rules = new MeekRules();
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);

        extraMeekOriented = rules.getChangedEdges().keySet();
        extraMeekOriented.removeAll(meekOriented);

        meekOriented.addAll(extraMeekOriented);

    }

    private void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
        List<String> _varNames = dataSet.getVariableNames();

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = dataSet.getVariables();
        this.dataSet = dataSet;
        this.discrete = dataSet.isDiscrete();

        if (!isDiscrete()) {
            this.covariances = dataSet.getCovarianceMatrix();
        }

        this.sampleSize = dataSet.getNumRows();
    }

    private void setCovMatrix(ICovarianceMatrix covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
        this.covariances = covarianceMatrix.getMatrix();
        List<String> _varNames = covarianceMatrix.getVariableNames();

        this.varNames = _varNames.toArray(new String[0]);
        this.variables = covarianceMatrix.getVariables();
        this.sampleSize = covarianceMatrix.getSampleSize();
    }

    private void buildIndexing(Graph graph) {
        this.hashIndices = new HashMap<Node, Integer>();
        for (Node next : graph.getNodes()) {
            for (int i = 0; i < this.varNames.length; i++) {
                if (this.varNames[i].equals(next.getName())) {
                    this.hashIndices.put(next, i);
                    break;
                }
            }
        }
    }

    //===========================SCORING METHODS===========================//

    public double scoreGraph(Graph graph) {
        Dag dag = new Dag(graph);

        double score = 0.0;

        for (Node y : dag.getNodes()) {
            Set<Node> parents = new HashSet<Node>(dag.getParents(y));
            int nextIndex = -1;
            for (int i = 0; i < getVariables().size(); i++) {
                if (this.varNames[i].equals(y.getName())) {
                    nextIndex = i;
                    break;
                }
            }
            int parentIndices[] = new int[parents.size()];
            Iterator<Node> pi = parents.iterator();
            int count = 0;
            while (pi.hasNext()) {
                Node nextParent = pi.next();
                for (int i = 0; i < getVariables().size(); i++) {
                    if (this.varNames[i].equals(nextParent.getName())) {
                        parentIndices[count++] = i;
                        break;
                    }
                }
            }

            if (this.isDiscrete()) {
                score += localDiscreteScore(nextIndex, parentIndices);
            } else {
                score += localSemScore(nextIndex, parentIndices);
            }
        }
        return score;
    }

    private double scoreGraphChange(Node y, Set<Node> parents1,
                                    Set<Node> parents2) {
        int yIndex = hashIndices.get(y);

        Double score1 = scoreHash.get(y).get(parents1);

        if (score1 == null) {
            int parentIndices1[] = new int[parents1.size()];

            int count = 0;
            for (Node aParents1 : parents1) {
                parentIndices1[count++] = (hashIndices.get(aParents1));
            }

            if (isDiscrete()) {
                score1 = localDiscreteScore(yIndex, parentIndices1);
            } else {
                score1 = localSemScore(yIndex, parentIndices1);
            }

            scoreHash.get(y).put(parents1, score1);
        }

        Double score2 = scoreHash.get(y).get(parents2);

        if (score2 == null) {
            int parentIndices2[] = new int[parents2.size()];

            int count2 = 0;
            for (Node aParents2 : parents2) {
                parentIndices2[count2++] = (hashIndices.get(aParents2));
            }

            if (isDiscrete()) {
                score2 = localDiscreteScore(yIndex, parentIndices2);
            } else {
                score2 = localSemScore(yIndex, parentIndices2);
            }

            scoreHash.get(y).put(parents2, score2);
        }

        // That is, the score for the variable set that contains x minus the score
        // for the variable set that does not contain x.
        return score1 - score2;
    }

    /**
     * Compute the local BDeu score of (i, parents(i)). See (Chickering, 2002).
     */
    private double localDiscreteScore(int i, int parents[]) {
        return getDiscreteScore().localScore(i, parents);
    }

    /**
     * Calculates the sample likelihood and BIC score for i given its parents in a simple SEM model.
     */
    private double localSemScore(int i, int[] parents) {
        double variance = getCovMatrix().get(i, i);
        int n = sampleSize();
//        int k = parents.length + 1;
        int k = (parents.length * (parents.length + 1)) / 2;

        if (parents.length > 0) {
            // Regress z onto i, yielding regression coefficients b.
            TetradMatrix Czz = getCovMatrix().getSelection(parents, parents);
            TetradMatrix inverse;

            try {
                inverse = Czz.inverse();
//                inverse = new TetradMatrix(MatrixUtils.pseudoInverse(Czz.toArray()));
            } catch (Exception e) {
                e.printStackTrace();
                Node target = dataSet().getVariable(i);
                List<Node> regressors = new ArrayList<Node>();
                for (int m = 0; m < parents.length; m++) regressors.add(dataSet().getVariable(parents[m]));
                String msg = "Matrix singularity regressing variable " + target + " on variables " + regressors;
                throw new RuntimeException(msg, e);
            }

            TetradVector Cyz = getCovMatrix().getColumn(i);
            Cyz = Cyz.viewSelection(parents);
            TetradVector b = inverse.times(Cyz);

            variance -= Cyz.dotProduct(b);
        }

        if (variance == 0) {
            StringBuilder builder = localModelString(i, parents);
            this.logger.log("info", builder.toString());
            this.logger.log("info", "Zero residual variance; returning negative infinity.");
            return 1e-15;
        }

        double c = getPenaltyDiscount();

        return -0.5 * n * log(variance) - c * k * log(n);
//        return -0.5 * n * Math.log(variance) - n * Math.log(2 * Math.PI) - n - k * c * Math.log(n);
    }

    private StringBuilder localModelString(int i, int[] parents) {
        StringBuilder b = new StringBuilder();
        b.append(("*** "));
        b.append(variables.get(i));

        if (parents.length == 0) {
            b.append(" with no parents");
        } else {
            b.append(" with parents ");

            for (int j = 0; j < parents.length; j++) {
                b.append(variables.get(parents[j]));

                if (j < parents.length - 1) {
                    b.append(",");
                }
            }
        }
        return b;
    }

    private int sampleSize() {
        return this.sampleSize;
    }

    private List<Node> getVariables() {
        return variables;
    }

    private TetradMatrix getCovMatrix() {
        return covariances;
    }

    private DataSet dataSet() {
        return dataSet;
    }

    private boolean isDiscrete() {
        return discrete;
    }

    private void fireGraphChange(Graph graph) {
        for (PropertyChangeListener l : getListeners()) {
            l.propertyChange(new PropertyChangeEvent(this, "graph", null, graph));
        }
    }

    private List<PropertyChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        return listeners;
    }

    private void storeGraph(Graph graph, double score) {
        if (!isStoreGraphs()) return;

        if (topGraphs.isEmpty() || score > topGraphs.first().getScore()) {
            Graph graphCopy = new EdgeListGraph(graph);

            topGraphs.add(new ScoredGraph(graphCopy, score));

            if (topGraphs.size() > getNumPatternsToStore()) {
                topGraphs.remove(topGraphs.first());
            }
        }
    }
}




