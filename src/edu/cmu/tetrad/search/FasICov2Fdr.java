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
import edu.cmu.tetrad.util.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.sqrt;

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
public class FasICov2Fdr implements IFas {

    private final List<TetradMatrix> covMatices;
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
     * The number of false dependence judgements, judged from the true graph using d-separation. Temporary.
     */
    private int numFalseDependenceJudgments;

    /**
     * The number of dependence judgements. Temporary.
     */
    private int numDependenceJudgement;

    /**
     * The sepsets found during the search.
     */
    private SepsetMap sepset = new SepsetMap();

    /**
     * True if this is being run by FCI--need to skip the knowledge forbid step.
     */
    private boolean fci = false;

    private NumberFormat nf = new DecimalFormat("0.00E0");

    /**
     * True iff verbose output should be printed.
     */
    private boolean verbose = false;
    private List pValueList = new ArrayList();
    private final double alpha;
    private boolean fdr;

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new FastAdjacencySearch.
     */
    public FasICov2Fdr(IndependenceTest test) {
        this.graph = new EdgeListGraph(test.getVariables());
        this.test = test;
        this.covMatices = test.getCovMatrices();
        this.alpha = test.getAlpha();
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
    public Graph search() {
        this.logger.log("info", "Starting Fast Adjacency Search.");
        graph.removeEdges(graph.getEdges());

        sepset = new SepsetMap();

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }

        List<Node> nodes = graph.getNodes();
        Map<Node, Set<Node>> adjacencies = emptyGraph(nodes);

        searchICov(nodes, test, adjacencies, true);
        searchiCovAll(nodes, test, adjacencies);

//        for (int d = 0; d <= _depth; d++) {
//            searchAtDepth(nodes, test, adjacencies, d);
//
//            if (!(freeDegree(nodes, adjacencies) > depth)) {
//                break;
//            }
//        }

        pValueList.clear();

        for (int d = 0; d <= _depth; d++) {
            test.setAlpha(alpha);
            Map<Node, Set<Node>> _adjacencies = copy(adjacencies);
            searchAtDepth(nodes, test, adjacencies, d);
            double cutoff = StatUtils.fdrCutoff(test.getAlpha(), pValueList, false);
            adjacencies = _adjacencies;
            test.setAlpha(cutoff);
            boolean more = searchAtDepth(nodes, test, adjacencies, d);

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

    private Map<Node, Set<Node>> copy(Map<Node, Set<Node>> adjacencies) {
        Map<Node, Set<Node>> copy = new HashMap<Node, Set<Node>>();

        for (Node node : adjacencies.keySet()) {
            copy.put(node, new HashSet<Node>(adjacencies.get(node)));
        }

        return copy;
    }

    private Map<Node, Set<Node>> emptyGraph(List<Node> nodes) {
        Map<Node, Set<Node>> adjacencies = new HashMap<Node, Set<Node>>();

        for (Node node : nodes) {
            adjacencies.put(node, new TreeSet<Node>());
        }
        return adjacencies;
    }

    private Map<Node, Set<Node>> completeGraph(List<Node> nodes) {
        Map<Node, Set<Node>> adjacencies = new HashMap<Node, Set<Node>>();

        for (int i = 0; i < nodes.size(); i++) {
            adjacencies.put(nodes.get(i), new HashSet<Node>());
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node x = nodes.get(i);

            for (int j = i + 1; j < nodes.size(); j++) {
                Node y = nodes.get(j);
                adjacencies.get(x).add(y);
                adjacencies.get(y).add(x);
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

    private boolean searchICov(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies,
                               boolean addDependencies) {
        boolean removed = false;
//        double percent = 1.0;

        List<TetradMatrix> allCovInvs = new ArrayList<TetradMatrix>();

        for (int i = 0; i < covMatices.size(); i++) {
            allCovInvs.add(covMatices.get(i).inverse());
        }

        int sampleSize = test.getSampleSize();

        if (sampleSize == 0) throw new IllegalArgumentException();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                List<Double> pValues = new ArrayList<Double>();

                for (int m = 0; m < allCovInvs.size(); m++) {
                    TetradMatrix inv = allCovInvs.get(m);
                    double r = -inv.get(i, j) / sqrt(inv.get(i, i) * inv.get(j, j));
                    double fisherZ = sqrt(sampleSize - (nodes.size() - 2) - 3.0) *
                            0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));

                    if (pvalue >= 0.0) {
                        pValues.add(pvalue);
                    }
                }

                double _cutoff = test.getAlpha();

                if (fdr) {
                    _cutoff = StatUtils.fdrCutoff(test.getAlpha(), pValues, false);
                }

                Collections.sort(pValues);
//                int index = (int) Math.round((1.0 - percent) * pValues.size());
                boolean independent = pValues.get(0) > _cutoff;

                if (addDependencies) {
                    if (independent) {
                        List<Node> theRest = new ArrayList<Node>();

                        for (Node node : nodes) {
                            if (node != x && node != y) theRest.add(node);
                        }

                        getSepsets().set(x, y, theRest);

                        if (verbose) {
//                            System.out.println(SearchLogUtils.independenceFactMsg(x, y, theRest, test.getPValue()));
                            System.out.println(x + " _||_ " + y + " | the rest" + " p = " +
                                    nf.format(test.getPValue()));
                        }

                        removed = true;
                    } else if (!forbiddenEdge(x, y)) {
                        adjacencies.get(x).add(y);
                        adjacencies.get(y).add(x);
                    }
                } else {
                    if (independent) {
                        if (!adjacencies.get(x).contains(y)) continue;

                        List<Node> theRest = new ArrayList<Node>();

                        for (Node node : nodes) {
                            if (node != x && node != y) theRest.add(node);
                        }

                        adjacencies.get(x).remove(y);
                        adjacencies.get(y).remove(x);

                        getSepsets().set(x, y, theRest);

                        if (verbose) {
                            System.out.println(x + " _||_ " + y + " | the rest" + " p = " +
                                    nf.format(test.getPValue()));
                        }

                        removed = true;
                    }
                }
            }
        }

        return removed;
    }

    private void searchiCovAll(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed;
        Set<Set<Node>> condSets = new HashSet<Set<Node>>();

        do {
            removed = false;

            for (Node x : nodes) {
                List<Node> adj = new ArrayList<Node>(adjacencies.get(x));
                adj.add(x);
                HashSet<Node> s = new HashSet<Node>(adj);
                if (condSets.contains(s)) {
                    continue;
                }
                condSets.add(s);
                removed = removed || searchICov(adj, test, adjacencies, false);
//
//
//                for (Node y : adjx) {
//                    if (!adjacencies.get(x).contains(y)) continue;
//                    List<Node> adjy = new ArrayList<Node>(adjacencies.get(y));
//                    List<Node> adj = new ArrayList<Node>(adjx);
//                    for (Node node : adjy) if (!adj.contains(node)) adj.add(node);
//                    removed = removed || searchICov(adj, test, adjacencies, false);
//                }
            }
        } while (removed);
    }

    private void searchiCovAdj(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed;

        do {
            removed = false;

            for (Node x : nodes) {
                List<Node> adj = new ArrayList<Node>(adjacencies.get(x));
                adj.add(x);
                removed = removed || searchICov(adj, test, adjacencies, false);
            }
        } while (removed);
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

    private boolean searchAtDepth(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies, int depth) {
        int numRemoved = 0;
        int count = 0;

        for (Node x : nodes) {
            if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());

            List<Node> adjx = new ArrayList<Node>(adjacencies.get(x));

            EDGE:
            for (Node y : adjx) {
                List<Node> _adjx = new ArrayList<Node>(adjacencies.get(x));
                _adjx.remove(y);
                List<Node> ppx = possibleParents(x, _adjx, knowledge);

                if (ppx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(ppx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, ppx);

                        boolean independent;

                        try {
                            independent = test.isIndependent(x, y, condSet);
                            pValueList.add(test.getPValue());
                        } catch (Exception e) {
                            independent = false;
                        }

                        boolean noEdgeRequired =
                                knowledge.noEdgeRequired(x.getName(), y.getName());

                        if (independent && noEdgeRequired) {
                            adjacencies.get(x).remove(y);
                            adjacencies.get(y).remove(x);
                            numRemoved++;
                            getSepsets().set(x, y, condSet);

                            if (verbose) {
                                System.out.println(SearchLogUtils.independenceFact(x, y, condSet) + " p = " +
                                        nf.format(test.getPValue()));
                            }
                            continue EDGE;
                        }
//                        else {
//                            if (verbose) {
//                                System.out.println("Dependence: " + SearchLogUtils.independenceFact(x, y, condSet) + " p = " +
//                                        nf.format(test.getPValue()));
//                            }
//                        }

                    }
                }
            }
        }

//        System.out.println("Num removed = " + numRemoved);
//        return numRemoved > 0;

        return freeDegree(nodes, adjacencies) > depth;
    }

    private boolean searchAtDepthICov(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies, int depth) {
        int numRemoved = 0;
        int count = 0;

        for (Node x : nodes) {
            if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());

            List<Node> adjx = new ArrayList<Node>(adjacencies.get(x));

            EDGE:
            for (Node y : adjx) {
                if (adjx.size() >= depth) {
                    ChoiceGenerator cg = new ChoiceGenerator(adjx.size(), depth);
                    int[] choice;

                    while ((choice = cg.next()) != null) {
                        List<Node> condSet = GraphUtils.asList(choice, adjx);

                        List<Node> _cond = new ArrayList<Node>(condSet);

                        for (Node node : nodes) {
                            if (!adjx.contains(node)) {
                                _cond.add(node);
                            }
                        }

                        if (!_cond.contains(x)) {
                            _cond.add(x);
                        }

                        if (!_cond.contains(y)) {
                            _cond.add(y);
                        }

                        searchICov(_cond, test, adjacencies, false);
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
//        throw new UnsupportedOperationException();
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
//        throw new UnsupportedOperationException();
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
        return null;
    }

    @Override
    public List<Triple> getAmbiguousTriples(Node node) {
        return null;
    }

    public boolean isFdr() {
        return fdr;
    }

    public void setFdr(boolean fdr) {
        this.fdr = fdr;
    }
}
