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

import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.IKnowledge;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.*;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;

/**
 * Implements the PC ("Peter/Clark") algorithm, as specified in Chapter 6 of Spirtes, Glymour, and Scheines, "Causation,
 * Prediction, and Search," 2nd edition, with a modified rule set in step D due to Chris Meek. For the modified rule
 * set, see Chris Meek (1995), "Causal inference and causal explanation with background knowledge."
 *
 * @author Joseph Ramsey.
 */
public class Mpc implements GraphSearch {

    private final TetradMatrix cov;
    private final TetradMatrix corr;
    private double alpha = 0.001;
    private SepsetMap sepsets = new SepsetMap();

    /**
     * Forbidden and required edges for the search.
     */
    private IKnowledge knowledge = new Knowledge();

    /**
     * The maximum number of nodes conditioned on in the search. The default it 1000.
     */
    private int depth = 1000;

    /**
     * The graph that's constructed during the search.
     */
    private Graph graph;

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
     * The logger for this class. The config needs to be set.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * In an enumeration of triple types, these are the collider triples.
     */
    private Set<Triple> unshieldedColliders;

    /**
     * In an enumeration of triple types, these are the noncollider triples.
     */
    private Set<Triple> unshieldedNoncolliders;

    /**
     * The number of indepdendence tests in the last search.
     */
    private int numIndependenceTests;

    /**
     * The true graph, for purposes of comparison. Temporary.
     */
    private Graph trueGraph;

    /**
     * The number of false dependence judgements from FAS, judging from the true graph, if set. Temporary.
     */
    private int numFalseDependenceJudgements;

    /**
     * The number of dependence judgements from FAS. Temporary.
     */
    private int numDependenceJudgements;

    /**
     * The initial graph for the Fast Adjacency Search, or null if there is none.
     */
    private Graph initialGraph = null;

    private boolean verbose = false;

    private boolean fdr = false;
    private int sampleSize;
    private List<Node> nodes;

    //=============================CONSTRUCTORS==========================//

    /**
     * Constructs a new PC search using the given independence test as oracle.
     */
    public Mpc(DataSet dataSet, double alpha, int depth) {
        this.cov = new CovarianceMatrix(dataSet).getMatrix();
        this.corr = MatrixUtils.convertCovToCorr(cov);
        this.alpha = alpha;
        this.depth = depth;
        this.sampleSize = dataSet.getNumRows();
        this.nodes = dataSet.getVariables();
    }

    //==============================PUBLIC METHODS========================//

    /**
     * @return true iff edges will not be added if they would create cycles.
     */
    public boolean isAggressivelyPreventCycles() {
        return this.aggressivelyPreventCycles;
    }

    /**
     * @param aggressivelyPreventCycles Set to true just in case edges will not be addeds if they would create cycles.
     */
    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles) {
        this.aggressivelyPreventCycles = aggressivelyPreventCycles;
    }

    /**
     * Returns the knowledge specification used in the search. Non-null.
     */
    public IKnowledge getKnowledge() {
        return knowledge;
    }

    /**
     * Sets the knowledge specification to be used in the search. May not be null.
     */
    public void setKnowledge(IKnowledge knowledge) {
        if (knowledge == null) {
            throw new NullPointerException();
        }

        this.knowledge = knowledge;
    }

    /**
     * Returns the sepset map from the most recent search. Non-null after the first call to <code>search()</code>.
     */
    public SepsetMap getSepsets() {
        return this.sepsets;
    }

    /**
     * @return the current depth of search--that is, the maximum number of conditioning nodes for any conditional
     * independence checked.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth of the search--that is, the maximum number of conditioning nodes for any conditional independence
     * checked.
     *
     * @param depth The depth of the search. The default is 1000. A value of -1 may be used to indicate that the depth
     *              should be high (1000). A value of Integer.MAX_VALUE may not be used, due to a bug on multi-core
     *              machines.
     */
    public void setDepth(int depth) {
        if (depth < -1) {
            throw new IllegalArgumentException("Depth must be -1 or >= 0: " + depth);
        }

        if (depth > 1000) {
            throw new IllegalArgumentException("Depth must be <= 1000.");
        }

        this.depth = depth;
    }

    /**
     * Runs PC starting with a complete graph over all nodes of the given conditional independence test, using the given
     * independence test and knowledge and returns the resultant graph. The returned graph will be a pattern if the
     * independence information is consistent with the hypothesis that there are no latent common causes. It may,
     * however, contain cycles or bidirected edges if this assumption is not born out, either due to the actual presence
     * of latent common causes, or due to statistical errors in conditional independence judgments.
     */
    public Graph search() {
        long start = System.currentTimeMillis();

        int _depth = depth;

        if (_depth == -1) {
            _depth = 1000;
        }

        List<Node> nodes = getNodes();
        Map<Node, Set<Node>> adjacencies = completeGraph(nodes);

        for (int d = 0; d <= _depth; d++) {
            System.out.println("depth " + d);
            maxParCorrDepth3(d, nodes, adjacencies);

            if (!(freeDegree(nodes, adjacencies) > d)) {
                break;
            }
        }

//        for (int d = 1; d <= _depth; d++) {
//            System.out.println("depth " + d);
//            pcDepth(nodes, adjacencies, d);
//
//            if (!(freeDegree(nodes, adjacencies) > d)) {
//                break;
//            }
//        }

        this.graph = new EdgeListGraph(getNodes());

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                if (adjacencies.get(x).contains(y)) {
                    graph.addUndirectedEdge(x, y);
                }
            }
        }


        SearchGraphUtils.pcOrientbk(knowledge, graph, nodes);
        SearchGraphUtils.orientCollidersUsingSepsets(this.sepsets, knowledge, graph, verbose);

        MeekRules rules = new MeekRules();
        rules.setAggressivelyPreventCycles(this.aggressivelyPreventCycles);
        rules.setKnowledge(knowledge);
        rules.orientImplied(graph);

        this.logger.log("graph", "\nReturning this graph: " + graph);

        long stop = System.currentTimeMillis();
        this.elapsedTime = stop - start;

        this.logger.log("info", "Elapsed time = " + (elapsedTime) / 1000. + " s");
        this.logger.log("info", "Finishing PC Algorithm.");
        this.logger.flush();

        return graph;
    }

    /**
     * Returns the elapsed time of the search, in milliseconds.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Returns the set of unshielded colliders in the graph returned by <code>search()</code>. Non-null after
     * <code>search</code> is called.
     */
    public Set<Triple> getUnshieldedColliders() {
        return unshieldedColliders;
    }

    /**
     * Returns the set of unshielded noncolliders in the graph returned by <code>search()</code>. Non-null after
     * <code>search</code> is called.
     */
    public Set<Triple> getUnshieldedNoncolliders() {
        return unshieldedNoncolliders;
    }

    //===============================PRIVATE METHODS=======================//

    private boolean maxParCorrDepth3(int n, List<Node> nodes, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;

//        Function f = new Function() {
//            @Override
//            public double valueAt(double r) {
//                double f = sqrt((double) sampleSize) * (log(1 + r) - log(1 - r));
//                return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(f)));
//            }
//        };
//
//        double cutoff = getCutoff(f, 0, 1, alpha);

        int[] vars = new int[n + 2];

//        if (n == 0) {
//
//
//            List<Node> cond = new ArrayList<Node>();
//
//            for (int x = 0; x < nodes.size(); x++) {
//                for (int y = 0; y < nodes.size(); y++) {
////                    vars[0] = x;
////                    vars[1] = y;
//
//                    double r = corr.get(x, y);
////                    double fisherZ = sqrt(sampleSize - 1 - 3.0) * 0.5 * (log(1.0 + r) - log(1.0 - r));
////                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));
//
//                    if (abs(r) < cutoff) {
////                    if (p > alpha) {
//                        Node _x = nodes.get(x);
//                        Node _y = nodes.get(y);
//
//                        adjacencies.get(_x).remove(_y);
//                        adjacencies.get(_y).remove(_x);
//
//                        getSepsets().set(_x, _y, cond);
//
////                        System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, test.getPValue()));
//
//                        removed = true;
//                    }
//                }
//            }
//
//            return removed;
//        }

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                vars[0] = x;
                vars[1] = y;

                int[] maxIndex = new int[n];

                Node _x = nodes.get(x);
                Node _y = nodes.get(y);

                if (!adjacencies.get(_x).contains(_y)) continue;

                for (int i = 2; i < n + 2; i++) {
                    maxIndex[i - 2] = -1;

                    double max = Double.NEGATIVE_INFINITY;

                    for (int j = 0; j < nodes.size(); j++) {
                        if (seenBefore(vars, i, j)) continue;

//                        Node _z = nodes.get(j);
//
//                        if (!(adjacencies.get(_x).contains(_z) || adjacencies.get(_y).contains(_z)));

                        double c = 1.0;

                        for (int h = 0; h < i - 1; h++) {
                            c *= corr.get(vars[h], j);
                        }

                        if (abs(c) > max) {
                            max = abs(c);
                            maxIndex[i - 2] = j;
                        }
                    }

                    vars[i] = maxIndex[i - 2];
                }

                removed = removeDepth(nodes, adjacencies, indices(vars, x, y, maxIndex)) || removed;
            }
        }

        return removed;

    }

    private boolean seenBefore(int[] varsMax, int i, int j) {
        for (int k = 0; k < i; k++) if (varsMax[k] == j) return true;
        return false;
    }

    private boolean removeDepth(List<Node> nodes, Map<Node, Set<Node>> adjacencies, int... indices) {
        boolean removed = false;

        Node _x = nodes.get(indices[0]);
        Node _y = nodes.get(indices[1]);
        if (!adjacencies.get(_x).contains(_y)) return false;

        List<Node> cond = new ArrayList<Node>();

        for (int i = 2; i < indices.length; i++) {
            cond.add(nodes.get(indices[i]));
        }

//        System.out.println(Arrays.toString(indices));

        double pvalue = pvalue(indices, sampleSize);

        boolean independent = pvalue > alpha;

        if (independent) {
            adjacencies.get(_x).remove(_y);
            adjacencies.get(_y).remove(_x);

            getSepsets().set(_x, _y, cond);

            System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, pvalue));

            removed = true;
        }

        return removed;
    }

    private int[] indices(int[] varsMax, int x, int y, int[] maxIndex) {
        int[] indices = new int[varsMax.length];
        indices[0] = x;
        indices[1] = y;
        for (int i3 = 0; i3 < maxIndex.length; i3++) indices[i3 + 2] = maxIndex[i3];
        return indices;
    }

    private void enumerateTriples() {
        this.unshieldedColliders = new HashSet<Triple>();
        this.unshieldedNoncolliders = new HashSet<Triple>();

        for (Node y : graph.getNodes()) {
            List<Node> adj = graph.getAdjacentNodes(y);

            if (adj.size() < 2) {
                continue;
            }

            ChoiceGenerator gen = new ChoiceGenerator(adj.size(), 2);
            int[] choice;

            while ((choice = gen.next()) != null) {
                Node x = adj.get(choice[0]);
                Node z = adj.get(choice[1]);

                List<Node> nodes = this.sepsets.get(x, z);

                // Note that checking adj(x, z) does not suffice when knowledge
                // has been specified.
                if (nodes == null) {
                    continue;
                }

                if (nodes.contains(y)) {
                    getUnshieldedNoncolliders().add(new Triple(x, y, z));
                } else {
                    getUnshieldedColliders().add(new Triple(x, y, z));
                }
            }
        }
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

    private double pvalue(int[] indices, int sampleSize) {
        double r = parcorr(indices);
        double fisherZ = sqrt(sampleSize - 1 - 3.0) * 0.5 * (log(1.0 + r) - log(1.0 - r));
        return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));
    }

    private double parcorr(int[] indices) {
        TetradMatrix prec = cov.getSelection(indices, indices).inverse();
        return prec.get(0, 1) / sqrt(prec.get(0, 0) * prec.get(1, 1));
    }

    private boolean pcDepth(List<Node> nodes, Map<Node, Set<Node>> adjacencies, int depth) {
//        System.out.println("Depth " + depth);
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
                        int[] indices = new int[condSet.size() + 2];
                        indices[0] = nodes.indexOf(x);
                        indices[1] = nodes.indexOf(y);

                        for (int i = 0; i < condSet.size(); i++) {
                            indices[i + 2] = nodes.indexOf(condSet.get(i));
                        }

                        boolean independent;
                        double pvalue = Double.NaN;

                        try {
                            pvalue = pvalue(indices, sampleSize);
                            independent = pvalue > alpha;
                        } catch (Exception e) {
                            independent = false;
                        }

                        boolean noEdgeRequired =
                                knowledge.noEdgeRequired(x.getName(), y.getName());

                        if (independent && noEdgeRequired) {
                            adjacencies.get(x).remove(y);
                            adjacencies.get(y).remove(x);
                            getSepsets().set(x, y, condSet);

                            if (verbose) {
                                System.out.println(SearchLogUtils.independenceFactMsg(x, y, condSet, pvalue));
                            }

                            continue EDGE;
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

    public static double getCutoff(Function f, double low, double high, double p) {

        // binary search.
        while (high - low > 1e-10) {
            double midpoint = (high + low) / 2.0;

            double value = f.valueAt(midpoint);

            if (value < p) {
                high = midpoint;
            } else {
                low = midpoint;
            }
        }

        return (low + high) / 2.0;
    }

    public int getNumIndependenceTests() {
        return numIndependenceTests;
    }

    public void setTrueGraph(Graph trueGraph) {
        this.trueGraph = trueGraph;
    }

    public int getNumFalseDependenceJudgements() {
        return numFalseDependenceJudgements;
    }

    public int getNumDependenceJudgements() {
        return numDependenceJudgements;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Triple> getColliders(Node node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Triple> getNoncolliders(Node node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Triple> getAmbiguousTriples(Node node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Triple> getUnderlineTriples(Node node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Triple> getDottedUnderlineTriples(Node node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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

    /**
     * True iff the algorithm should be run with False Discovery Rate tests.
     */
    public boolean isFdr() {
        return fdr;
    }

    public void setFdr(boolean fdr) {
        this.fdr = fdr;
    }
}


