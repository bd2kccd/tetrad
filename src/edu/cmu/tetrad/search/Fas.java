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
import edu.cmu.tetrad.util.ChoiceGenerator;
import edu.cmu.tetrad.util.TetradLogger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Implements the "fast adjacency search" used in several causal algorithms in this package. In the fast adjacency
 * search, at a given stage of the search, an edge X*-*Y is removed from the graph if X _||_ Y | S, where S is a subset
 * of size d either of adj(X) or of adj(Y), where d is the depth of the search. The fast adjacency search performs this
 * procedure for each pair of adjacent edges in the graph and for each depth d = 0, 1, 2, ..., d1, where d1 is either
 * the maximum depth or else the first such depth at which no edges can be removed. The interpretation of this adjacency
 * search is different for different algorithms, depending on the assumptions of the algorithm. A mapping from {x, y} to
 * S({x, y}) is returned for edges x *-* y that have been removed.
 *
 * @author Joseph Ramsey.
 */
public class Fas implements IFas {

    /**
     * The search graph. It is assumed going in that all of the true adjacencies of x are in this graph for every node
     * x. It is hoped (i.e. true in the large sample limit) that true adjacencies are never removed.
     */
    private Graph graph;

    /**
     * The independence test. This should be appropriate to the types
     */
    private IndependenceTest test;

    /**
     * Specification of which edges are forbidden or required.
     */
    private IKnowledge knowledge = new Knowledge();

    /**
     * The maximum number of variables conditioned on in any conditional independence test. If the depth is -1, it will
     * be taken to be the maximum value, which is 1000. Otherwise, it should be set to a non-negative integer.
     */
    private int depth = 1000;

    /**
     * The number of independence tests.
     */
    private int numIndependenceTests;


    /**
     * The logger, by default the empty logger.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * The true graph, for purposes of comparison. Temporary.
     */
    private Graph trueGraph;

    /**
     * The number of false dependence judgements, judged from the true graph using d-separation. Temporary.
     */
    private int numFalseDependenceJudgments;

    /**
     * The number of dependence judgements. Temporary.
     */
    private int numDependenceJudgement;

    private int numIndependenceJudgements;

    /**
     * The sepsets found during the search.
     */
    private SepsetMap sepset = new SepsetMap();

    /**
     * True if this is being run by FCI--need to skip the knowledge forbid step.
     */
    private boolean fci = false;

    /**
     * The depth 0 graph, specified initially.
     */
    private Graph initialGraph;

//    private List<Double> pValues = new ArrayList<Double>();

    private NumberFormat nf = new DecimalFormat("0.00E0");

    /**
     * True iff verbose output should be printed.
     */
    private boolean verbose = false;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new FastAdjacencySearch.
     */
    public Fas(Graph graph, IndependenceTest test) {
        this.graph = graph;
        this.test = test;
    }

    public Fas(IndependenceTest test) {
        this.graph = new EdgeListGraph(test.getVariables());
        this.test = test;
    }

    //==========================PUBLIC METHODS===========================//

    /**
     * Discovers all adjacencies in data.  The procedure is to remove edges in the graph which connect pairs of
     * variables which are independent conditional on some other set of variables in the graph (the "sepset"). These are
     * removed in tiers.  First, edges which are independent conditional on zero other variables are removed, then edges
     * which are independent conditional on one other variable are removed, then two, then three, and so on, until no
     * more edges can be removed from the graph.  The edges which remain in the graph after this procedure are the
     * adjacencies in the data.
     *
     * @return a SepSet, which indicates which variables are independent conditional on which other variables
     */
    public Graph    search() {
        this.logger.log("info", "Starting Fast Adjacency Search.");
        graph.removeEdges(graph.getEdges());

        sepset = new SepsetMap();

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }

        Map<Node, Set<Node>> adjacencies = new HashMap<Node, Set<Node>>();
        List<Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            adjacencies.put(node, new TreeSet<Node>());
        }

//        complete graph
//        for (Node node : nodes) {
//            adjacencies.put(node, new TreeSet<Node>());
//
//            for (Node node2 : nodes) {
//                if (node == node2) continue;
//                adjacencies.get(node).add(node2);
//            }
//        }

        for (int d = 0; d <= _depth; d++) {
            boolean more;

            if (d == 0) {
                more = searchAtDepth0(nodes, test, adjacencies);
            } else {
                more = searchAtDepth(nodes, test, adjacencies, d);
            }

            if (!more) {
                break;
            }
        }

//        System.out.println("Finished with search, constructing Graph...");

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                if (adjacencies.get(x).contains(y)) {
                    graph.addUndirectedEdge(x, y);
                }
            }
        }

//        System.out.println("Finished constructing Graph.");

        this.logger.log("info", "Finishing Fast Adjacency Search.");

        return graph;
    }

    public Map<Node, Set<Node>> searchMapOnly() {
        this.logger.log("info", "Starting Fast Adjacency Search.");
        graph.removeEdges(graph.getEdges());

        sepset = new SepsetMap();

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }


        Map<Node, Set<Node>> adjacencies = new HashMap<Node, Set<Node>>();
        List<Node> nodes = graph.getNodes();

        for (Node node : nodes) {
            adjacencies.put(node, new TreeSet<Node>());
        }

        for (int d = 0; d <= _depth; d++) {
            boolean more;

            if (d == 0) {
                more = searchAtDepth0(nodes, test, adjacencies);
            } else {
                more = searchAtDepth(nodes, test, adjacencies, d);
            }

            if (!more) {
                break;
            }
        }

        return adjacencies;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException(
                    "Depth must be -1 (unlimited) or >= 0.");
        }

        this.depth = depth;
    }

    public IKnowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(IKnowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException("Cannot set knowledge to null");
        }
        this.knowledge = knowledge;
    }

    //==============================PRIVATE METHODS======================/

    private boolean searchAtDepth0(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        List<Node> empty = Collections.emptyList();
        for (int i = 0; i < nodes.size(); i++) {
            if (verbose) {
                if ((i + 1) % 100 == 0) System.out.println("Node # " + (i + 1));
            }

            Node x = nodes.get(i);

//            if (missingCol(test.getData(), x)) {
//                continue;
//            }

            for (int j = i + 1; j < nodes.size(); j++) {

                Node y = nodes.get(j);

//                if (missingCol(test.getData(), y)) {
//                    continue;
//                }

                if (initialGraph != null) {
                    Node x2 = initialGraph.getNode(x.getName());
                    Node y2 = initialGraph.getNode(y.getName());

                    if (!initialGraph.isAdjacentTo(x2, y2)) {
                        continue;
                    }
                }


                boolean independent;

                try {
                    numIndependenceTests++;
                    independent = test.isIndependent(x, y, empty);
                } catch (Exception e) {
                    e.printStackTrace();
                    independent = false;
                }

                if (independent) {
                    numIndependenceJudgements++;
                } else {
                    numDependenceJudgement++;
                }

                boolean noEdgeRequired =
                        knowledge.noEdgeRequired(x.getName(), y.getName());


                if (independent && noEdgeRequired) {
                    getSepsets().set(x, y, empty);

                    TetradLogger.getInstance().log("independencies", SearchLogUtils.independenceFact(x, y, empty) + " p = " +
                            nf.format(test.getPValue()));

                    if (verbose) {
                        System.out.println(SearchLogUtils.independenceFact(x, y, empty) + " p = " +
                                nf.format(test.getPValue()));
                    }

                } else if (!forbiddenEdge(x, y)) {
                    adjacencies.get(x).add(y);
                    adjacencies.get(y).add(x);

                    TetradLogger.getInstance().log("dependencies", SearchLogUtils.independenceFact(x, y, empty) + " p = " +
                            nf.format(test.getPValue()));

//                    if (verbose) {
//                        System.out.println(SearchLogUtils.dependenceFactMsg(x, y, empty, test.getPValue()) + " p = " +
//                                nf.format(test.getPValue()));
//                    }
                }
            }
        }

        return

                freeDegree(nodes, adjacencies)

                        > 0;
    }

    // Returns true just in case there are no defined values in the column.
    private boolean missingCol(DataModel data, Node x) {
        return false; // TODO revert.

//        if (data instanceof DataSet) {
//            DataSet dataSet = (DataSet) data;
//            int j = dataSet.getColumn(dataSet.getVariable(x.getName()));
//
//            for (int i = 0; i < dataSet.getNumRows(); i++) {
//                if (!Double.isNaN(dataSet.getDouble(i, j))) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//
//        return false;
    }

    private int freeDegree(List<Node> nodes, Map<Node, Set<Node>> adjacencies) {
        int max = 0;

        for (Node x : nodes) {
            Set<Node> opposites = adjacencies.get(x);

            for (Node y : opposites) {
                Set<Node> adjx = new HashSet<Node>(opposites);
                adjx.remove(y);

                if (adjx.size() > max) {
                    max = adjx.size();
                }
            }
        }

        return max;
    }

    private boolean forbiddenEdge(Node x, Node y) {
        String name1 = x.getName();
        String name2 = y.getName();

        if (knowledge.edgeForbidden(name1, name2) &&
                knowledge.edgeForbidden(name2, name1)) {
            this.logger.log("edgeRemoved", "Removed " + Edges.undirectedEdge(x, y) + " because it was " +
                    "forbidden by background knowledge.");

            return true;
        }

        return false;
    }

    private boolean searchAtDepth(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies, int depth) {
        int numRemoved = 0;
        int count = 0;

        List<IndependenceFact> facts = new ArrayList<IndependenceFact>();

        for (Node x : nodes) {
            if (verbose) {
                if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());
            }

            List<Node> adjx = new ArrayList<Node>(adjacencies.get(x));

            EDGE:
            for (Node y : adjx) {
                List<Node> _adjx = new ArrayList<Node>(adjacencies.get(x));
                _adjx.remove(y);
                List<Node> ppx = possibleParents(x, _adjx, knowledge);
//                final Node _x = x;
//
//                Collections.sort(ppx, new Comparator<Node>() {
//                    @Override
//                    public int compare(Node node1, Node node2) {
//                        test.isIndependent(_x, node1);
//                        double p1 = test.getPValue();
//                        test.isIndependent(_x, node2);
//                        double p2 = test.getPValue();
//                        return Double.compare(p2, p1);
//                    }
//                });

                if (ppx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(ppx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, ppx);

                        IndependenceFact fact = new IndependenceFact(x, y, condSet);
                        if (facts.contains(fact)) continue;
                        facts.add(fact);

                        boolean independent;

                        try {
                            numIndependenceTests++;
                            independent = test.isIndependent(x, y, condSet);
                        } catch (Exception e) {
                            independent = false;
                        }

                        if (independent) {
                            numIndependenceJudgements++;
                        } else {
                            numDependenceJudgement++;
                        }

                        boolean noEdgeRequired =
                                knowledge.noEdgeRequired(x.getName(), y.getName());

                        if (independent && noEdgeRequired) {
                            adjacencies.get(x).remove(y);
                            adjacencies.get(y).remove(x);
                            numRemoved++;
                            getSepsets().set(x, y, condSet);

                            TetradLogger.getInstance().log("independencies", SearchLogUtils.independenceFact(x, y, condSet) + " p = " +
                                    nf.format(test.getPValue()));

                            if (verbose) {
                                System.out.println(SearchLogUtils.independenceFactMsg(x, y, condSet, test.getPValue()));
                            }

                            continue EDGE;
                        }
                        else {
//                            if (verbose) {
//                                System.out.println(SearchLogUtils.dependenceFactMsg(x, y, condSet, test.getPValue()));
//                            }

                        }
                    }
                }
            }
        }

//        System.out.println("Num removed = " + numRemoved);
//        return numRemoved > 0;

        return freeDegree(nodes, adjacencies) > depth;
    }

    private List<Node> possibleParents(Node x, List<Node> adjx,
                                       IKnowledge knowledge) {
        List<Node> possibleParents = new LinkedList<Node>();
        String _x = x.getName();

        for (Node z : adjx) {
            String _z = z.getName();

            if (possibleParentOf(_z, _x, knowledge)) {
                possibleParents.add(z);
            }
        }

        return possibleParents;
    }

    private boolean possibleParentOf(String z, String x, IKnowledge knowledge) {
        return !knowledge.edgeForbidden(z, x) && !knowledge.edgeRequired(x, z);
    }

    public int getNumIndependenceTests() {
        return numIndependenceTests;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public int getNumFalseDependenceJudgments() {
        return numFalseDependenceJudgments;
    }

    public int getNumDependenceJudgments() {
        return numDependenceJudgement;
    }

    public SepsetMap getSepsets() {
        return sepset;
    }

    public void setInitialGraph(Graph initialGraph) {
        this.initialGraph = initialGraph;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isAggressivelyPreventCycles() {
        return false;
    }

    @Override
    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {

    }

    @Override
    public IndependenceTest getIndependenceTest() {
        return null;
    }

    @Override
    public Graph search(List<Node> nodes) {
        return null;
    }

    @Override
    public long getElapsedTime() {
        return 0;
    }

    @Override
    public List<Node> getNodes() {
        return test.getVariables();
    }

    @Override
    public List<Triple> getAmbiguousTriples(Node node) {
        return null;
    }

    public int getNumIndependenceJudgements() {
        return numIndependenceJudgements;
    }
}
