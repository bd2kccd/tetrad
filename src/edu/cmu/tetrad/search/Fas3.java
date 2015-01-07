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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.*;

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
public class Fas3 implements IFas {

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
     * The logger, by default the empty logger.
     */
    private TetradLogger logger = TetradLogger.getInstance();

    /**
     * The sepsets found during the search.
     */
    private SepsetMap sepset = new SepsetMap();

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

    /**
     * The correlation matrix for this test.
     */
    private TetradMatrix corr;

    /**
     * Covariance matrix for this data.
     */
    private final TetradMatrix cov;

    private List<Node> empty = new ArrayList<Node>();

    //==========================CONSTRUCTORS=============================//

    /**
     * Constructs a new FastAdjacencySearch.
     */
    public Fas3(IndependenceTest test) {
        this.graph = new EdgeListGraph(test.getVariables());
        this.test = test;

        List<DataSet> dataSets = test.getDataSets();
        List<DataSet> std = DataUtils.standardizeData(dataSets);
        DataSet concatStd = DataUtils.concatenateData(std);
        cov = new CovarianceMatrix(concatStd).getMatrix();
        corr = MatrixUtils.convertCovToCorr(cov);
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

        List<Node> nodes = getNodes();
        Map<Node, Set<Node>> adjacencies = completeGraph(nodes);

//        depth0(nodes, test, adjacencies);
//        depth1(nodes, test, adjacencies);
//        depth2(nodes, test, adjacencies);
//
//        depth0(nodes, test, adjacencies);
//        depth1(nodes, test, adjacencies);
//        depth2(nodes, test, adjacencies);

        for (int d = 0; d <= _depth; d++) {
            System.out.println("depth " + d);
//            strictMin(d, nodes, adjacencies, test.getSampleSize(), test.getAlpha());
            maxParCorrDepth3(d, nodes, test, adjacencies);
//            maxParCorrDepth2(d, nodes, test, adjacencies);
//            sgsDepth(d, nodes, test, adjacencies);
//            depthRandom(d, nodes, test, adjacencies);
//            pcDepth(nodes, test, adjacencies, d);

            if (!(freeDegree(nodes, adjacencies) > d)) {
                break;
            }
        }

        for (int d = 1; d <= _depth; d++) {
            System.out.println("depth " + d);
//            strictMin(d, nodes, adjacencies, test.getSampleSize(), test.getAlpha());
//            depth(d, nodes, test, adjacencies);
//            depthRandom(d, nodes, test, adjacencies);
            pcDepth(nodes, test, adjacencies, d);

            if (!(freeDegree(nodes, adjacencies) > d)) {
                break;
            }
        }

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                if (adjacencies.get(x).contains(y)) {
                    graph.addUndirectedEdge(x, y);
                }
            }
        }

        this.logger.log("info", "Finishing Fast Adjacency Search.");
        return graph;
    }

    //==============================PRIVATE METHODS======================/


//    private boolean depth(int n, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
//        boolean removed = false;
//
//        int[] varsMax = new int[n + 2];
//        int[] varsMin = new int[n + 2];
//        int[] varsMinAbs = new int[n + 2];
//
//        for (int x = 0; x < nodes.size(); x++) {
//            for (int y = x + 1; y < nodes.size(); y++) {
//                varsMax[0] = x;
//                varsMax[1] = y;
//                varsMin[0] = x;
//                varsMin[1] = y;
//                varsMinAbs[0] = x;
//                varsMinAbs[1] = y;
//
//                int[] maxIndex = new int[n];
//                int[] minIndex = new int[n];
//                int[] minAbsIndex = new int[n];
//
//                Node _x = nodes.get(x);
//                Node _y = nodes.get(y);
//                if (!adjacencies.get(_x).contains(_y)) continue;
//
//                for (int i = 2; i < n + 2; i++) {
//                        double max = Double.NEGATIVE_INFINITY;
//                        double min = Double.POSITIVE_INFINITY;
//                        double minAbs = Double.POSITIVE_INFINITY;
//
//                        J:
//                        for (int j = 0; j < nodes.size(); j++) {
//                            for (int k = 0; k < i; k++) if (varsMax[k] == j) continue J;
//                            double c = corr.get(j, varsMax[2]);
//
//                            if (c > max) {
//                                max = c;
//                                maxIndex[i - 2] = j;
//                            }
//
//                            if (c < min) {
//                                min = c;
//                                minIndex[i - 2] = j;
//                            }
//
//                            if (abs(c) < minAbs) {
//                                minAbs = abs(c);
//                                minAbsIndex[i - 2] = j;
//                            }
//                        }
//
//                        varsMax[i] = maxIndex[i - 2];
//                        varsMin[i] = minIndex[i - 2];
//                        varsMinAbs[i] = minAbsIndex[i - 2];
////                    }
//                    }
//
//                    removed = removeDepth(nodes, test, adjacencies, x, y, maxIndex) || removed;
////                    removed = removeDepth(nodes, test, adjacencies, x, y, minIndex) || removed;
////                    removed = removeDepth(nodes, test, adjacencies, x, y, minAbsIndex) || removed;
//
////                double p1 = pValue(nodes, test, x, y, maxIndex);
////                double p2 = pValue(nodes, test, x, y, minIndex);
////                double p3 = pValue(nodes, test, x, y, minAbsIndex);
////
////                if (p1 >= p2 && p1 >= p3) {
////                    removed = removeDepth(nodes, test, adjacencies, x, y, maxIndex) || removed;
////                }
////                else if (p2 >= p1 && p2 >= p3) {
////                    removed = removeDepth(nodes, test, adjacencies, x, y, minIndex) || removed;
////                }
////                else if (p3 >= p1 && p3 >= p2) {
////                    removed = removeDepth(nodes, test, adjacencies, x, y, minAbsIndex) || removed;
////                }
//                }
//            }
//
//            return removed;
//        }

    private boolean strictMin(int d, List<Node> nodes, Map<Node, Set<Node>> adjacencies, int sampleSize, double alpha) {
        boolean removed = false;

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                Node _x = nodes.get(x);
                Node _y = nodes.get(y);
                removed = check(d, alpha, adjacencies, x, y, _x, _y, nodes, sampleSize) || removed;

//                if (!adjacencies.get(_x).contains(_y)) continue;
//
//                Set<Node> adj = adjacencies.get(_x);
//                List<Node> _adj = new ArrayList<Node>(adj);
//                _adj.remove(_y);
//                if (_adj.size() >= d) {
//                    removed = check(d, test, adjacencies, x, y, _x, _y, _adj) || removed;
//                }
//
//                adj = adjacencies.get(_y);
//                _adj = new ArrayList<Node>(adj);
//                _adj.remove(_x);
//                if (_adj.size() >= d) {
//                    removed = check(d, test, adjacencies, x, y, _x, _y, _adj) || removed;
//                }
            }
        }

        return removed;
    }

    private boolean check(int d, double alpha, Map<Node, Set<Node>> adjacencies, int x, int y,
                          Node _x, Node _y, List<Node> _adj, int sampleSize) {
        double max = Double.NEGATIVE_INFINITY;
        List<Node> maxCond = new ArrayList<Node>();

//        System.out.println("x = " + x + " y = " + y);

        ChoiceGenerator gen = new ChoiceGenerator(_adj.size(), d);
        int[] choice;

        CHOICE:
        while ((choice = gen.next()) != null) {
            for (int i = 0; i < choice.length; i++) {
                if (choice[i] == x || choice[i] == y) continue CHOICE;
            }

            List<Node> cond = new ArrayList<Node>();
            for (int i = 0; i < choice.length; i++) {
                cond.add(_adj.get(choice[i]));
            }

            int[] indices = new int[choice.length + 2];
            indices[0] = x;
            indices[1] = y;
            for (int i = 0; i < choice.length; i++) indices[i + 2] = choice[i];

//            System.out.println(Arrays.toString(indices));

            double pvalue = pvalue(indices, sampleSize);

            if (pvalue > max) {
//                System.out.println(pvalue);
                max = pvalue;
                maxCond = cond;
            }
        }

        if (max > alpha) {
            adjacencies.get(_x).remove(_y);
            adjacencies.get(_y).remove(_x);

            getSepsets().set(_x, _y, maxCond);

            System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, maxCond, max));

            return true;
        }

        return false;
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

    private boolean maxParCorrDepth1(int n, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;

        int[] vars = new int[n + 2];
        Arrays.fill(vars, -1);

        for (int x = 0; x < nodes.size(); x++) {

            Y:
            for (int y = x + 1; y < nodes.size(); y++) {
                vars[0] = x;
                vars[1] = y;

                int[] maxIndex = new int[n];

                Node _x = nodes.get(x);
                Node _y = nodes.get(y);

                if (!adjacencies.get(_x).contains(_y)) continue;

                for (int i = 2; i < n + 2; i++) {
                    maxIndex[i - 2] = -1;

                    if (false) {
                        double max = Double.NEGATIVE_INFINITY;

                        J:
                        for (int j = 0; j < nodes.size(); j++) {
                            if (seenBefore(vars, i, j)) continue;

                            double c = corr.get(j, x) * corr.get(j, y);

                            if (abs(c) > max) {
                                max = abs(c);
                                maxIndex[i - 2] = j;
                            }
                        }

                        vars[i] = maxIndex[i - 2];
                    } else {
                        double max = Double.NEGATIVE_INFINITY;

                        for (int j = 0; j < nodes.size(); j++) {
                            if (seenBefore(vars, i, j)) continue;

                            double c = 1.0;
//                            vars[i] = j;

//                            for (int e = 0; e < i; e++) {
//                                for (int f = e + 1; f < i; f++) {
//                                    c *= corr.get(e, f);
//                                }
//                            }

                            for (int h = 0; h < i - 1; h++) {
                                c *= corr.get(vars[h], j);
                            }

//                            double c = corr.get(j, x) * corr.get(j, y) * corr.get(j, vars[2]);
//                            double c = corr.get(j, vars[2]);

                            if (abs(c) > max) {
                                max = abs(c);
                                maxIndex[i - 2] = j;
                            }
                        }

                        vars[i] = maxIndex[i - 2];
                    }
                }

                removed = removeDepth(nodes, test, adjacencies, indices(vars, x, y, maxIndex)) || removed;
            }
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

    private boolean maxParCorrDepth2(int d, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
//        System.out.println(nodes);

        boolean removed = false;

        int[] varsMax = new int[d + 2];
        Arrays.fill(varsMax, -1);

        int[] varsMin = new int[d + 2];
        Arrays.fill(varsMin, -1);

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                varsMax[0] = x;
                varsMax[1] = y;

                varsMin[0] = x;
                varsMin[1] = y;

                Node _x = nodes.get(x);
                Node _y = nodes.get(y);
                if (!adjacencies.get(_x).contains(_y)) continue;

//                System.out.println("corr(x, y) = " + corr.get(x, y));

                if (d == 0) {
                    double r = corr.get(x, y);
                    double fisherZ = sqrt(test.getSampleSize() - 1 - 3.0) * 0.5 * (log(1.0 + r) - log(1.0 - r));
                    double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

                    boolean independent = pvalue > test.getAlpha();

                    if (independent) {
                        adjacencies.get(_x).remove(_y);
                        adjacencies.get(_y).remove(_x);
                        getSepsets().set(_x, _y, empty);
                        System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, empty, test.getPValue()));
                    }
//                    removed = removeDepth(nodes, test, adjacencies, varsMax) || removed;
                } else if (d == 1) {
                    double rxy = corr.get(x, y);
                    double min = Double.POSITIVE_INFINITY;

                    for (int j = 0; j < nodes.size(); j++) {
                        if (!seenBefore(varsMax, 2, j)) {
                            double c = rxy - corr.get(j, x) * corr.get(j, y);

                            if (abs(c) < min) {
                                min = abs(c);
                                varsMax[2] = j;
                            }
                        }
                    }

                    removed = removeDepth(nodes, test, adjacencies, varsMax) || removed;
//                } else if (d == 2) {
//                    double rxy = corr.get(x, y);
//                    double min = Double.POSITIVE_INFINITY;
//
//                    ChoiceGenerator gen = new ChoiceGenerator(nodes.size(), 2);
//                    int[] choice;
//
//                    while ((choice = gen.next()) != null) {
//                        if (choice[0] == x || choice[0] == y || choice[1] == x || choice[1] == y) {
//                            continue;
//                        }
//
//                        int[] indices = new int[4];
//                        indices[0] = x;
//                        indices[1] = y;
//                        indices[2] = choice[0];
//                        indices[3] = choice[1];
//
//                        double r = parcorr(indices);
//
//                        if (abs(r) < min) {
//                            min = abs(r);
//                            varsMax[2] = choice[0];
//                            varsMax[3] = choice[1];
//                        }
//                    }
//
//                    removed = removeDepth(nodes, test, adjacencies, varsMax) || removed;
                } else if (d > 1) {
                    for (int i = 2; i < d + 2; i++) {
                        double max = Double.NEGATIVE_INFINITY;
                        double min = Double.POSITIVE_INFINITY;

                        for (int j = 0; j < nodes.size(); j++) {
                            double c;

                            if (i == 2) {
                                c = corr.get(j, x) * corr.get(j, y);
                            } else {
                                c = corr.get(j, varsMax[2]);
                            }

                            if (!seenBefore(varsMax, i, j)) {
                                if (c > max) {
                                    max = c;
                                    varsMax[i] = j;
                                }
                            }
                        }

                        double c2;

                        for (int j = 0; j < nodes.size(); j++) {
                            if (i == 2) {
                                c2 = corr.get(j, x) * corr.get(j, y);
                            } else {
                                c2 = corr.get(j, varsMin[2]);
                            }

                            if (!seenBefore(varsMin, i, j)) {
                                if (c2 < min) {
                                    min = c2;
                                    varsMin[i] = j;
                                }
                            }
                        }
                    }

                    removed = removeDepth(nodes, test, adjacencies, varsMax) || removed;
                    removed = removeDepth(nodes, test, adjacencies, varsMin) || removed;
                }


            }
        }

        return removed;
    }

    private boolean maxParCorrDepth3(int n, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;

        int[] vars = new int[n + 2];

        if (n == 0) {
            List<Node> cond = new ArrayList<Node>();

            for (int x = 0; x < nodes.size(); x++) {
                for (int y = 0; y < nodes.size(); y++) {
                    vars[0] = x;
                    vars[1] = y;

                    double r = corr.get(x, y);
                    double fisherZ = sqrt(test.getSampleSize() - 1 - 3.0) * 0.5 * (log(1.0 + r) - log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

                    if (p > test.getAlpha()) {
                        Node _x = nodes.get(x);
                        Node _y = nodes.get(y);

                        adjacencies.get(_x).remove(_y);
                        adjacencies.get(_y).remove(_x);

                        getSepsets().set(_x, _y, cond);

//                        System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, test.getPValue()));

                        removed = true;
                    }
                }
            }

            return removed;
        }

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

                removed = removeDepth(nodes, test, adjacencies, indices(vars, x, y, maxIndex)) || removed;
            }
        }

        return removed;

    }


    private boolean sgsDepth(int d, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
//        System.out.println(nodes);

        boolean removed = false;

        int[] varsMax = new int[d + 2];
        Arrays.fill(varsMax, -1);

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                varsMax[0] = x;
                varsMax[1] = y;


                Node _x = nodes.get(x);
                Node _y = nodes.get(y);

                if (!adjacencies.get(_x).contains(_y)) {
                    continue;
                }

                double min = Double.POSITIVE_INFINITY;
                double _r = Double.NaN;

                ChoiceGenerator gen = new ChoiceGenerator(nodes.size(), d);
                int[] choice;
                int[] indices = new int[d + 2];
                indices[0] = x;
                indices[1] = y;

                WHILE:
                while ((choice = gen.next()) != null) {
                    for (int k = 0; k < d; k++) {
                        if (choice[k] == x || choice[k] == y) continue WHILE;
                    }

                    for (int k = 0; k < d; k++) {
                        indices[k + 2] = choice[k];
                    }

                    double r = parcorr(indices);

                    if (abs(r) < min) {
                        min = abs(r);

                        for (int k = 0; k < d; k++) {
                            varsMax[k + 2] = choice[k];
                            _r = r;
                        }
                    }
                }

//                double r = min;

                double fisherZ = sqrt(test.getSampleSize() - 1 - 3.0) * 0.5 * (log(1.0 + _r) - log(1.0 - _r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

                if (p > test.getAlpha()) {
                    adjacencies.get(_x).remove(_y);
                    adjacencies.get(_y).remove(_x);

                    List<Node> cond = new ArrayList<Node>();

                    for (int i = 2; i < indices.length; i++) {
                        cond.add(nodes.get(varsMax[i]));
                    }

                    getSepsets().set(_x, _y, cond);


                    System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, p));

                    removed = true;

//                removed = removeDepth(nodes, test, adjacencies, varsMax) || removed;
                }
            }
        }

        return removed;
    }

    private boolean depthRandom(int n, List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
//        System.out.println(nodes);

        boolean removed = false;

        int[] vars = new int[n + 2];
        Arrays.fill(vars, -1);

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                vars[0] = x;
                vars[1] = y;

                Node _x = nodes.get(x);
                Node _y = nodes.get(y);
                if (!adjacencies.get(_x).contains(_y)) continue;

                for (int i = 2; i < n + 2; i++) {
                    int j;

                    do {
                        j = RandomUtil.getInstance().nextInt(nodes.size());
                    } while (seenBefore(vars, i, j));

                    vars[i] = j;
                }

                removed = removeDepth(nodes, test, adjacencies, vars) || removed;
            }
        }

        return removed;
    }

    private boolean seenBefore(int[] varsMax, int i, int j) {
        for (int k = 0; k < i; k++) if (varsMax[k] == j) return true;
        return false;
    }

    private String nodesString(List<Node> nodes, int[] indices) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == -1) continue;

            buf.append(nodes.get(indices[i]));

            if (i < indices.length - 1) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    private double pValue(List<Node> nodes, IndependenceTest test, int x, int y, int... z) {
        Node _x = nodes.get(x);
        Node _y = nodes.get(y);

        List<Node> cond = new ArrayList<Node>();

        for (int i = 0; i < z.length; i++) {
            cond.add(nodes.get(z[i]));
        }

        test.isIndependent(_x, _y, cond);
        return test.getPValue();
    }


    private boolean removeDepth(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies, int... indices) {
        boolean removed = false;

        Node _x = nodes.get(indices[0]);
        Node _y = nodes.get(indices[1]);
        if (!adjacencies.get(_x).contains(_y)) return false;

        List<Node> cond = new ArrayList<Node>();

        for (int i = 2; i < indices.length; i++) {
            cond.add(nodes.get(indices[i]));
        }

//        System.out.println(Arrays.toString(indices));

        double pvalue = pvalue(indices, test.getSampleSize());

        boolean independent = pvalue > test.getAlpha();

        if (independent) {
            adjacencies.get(_x).remove(_y);
            adjacencies.get(_y).remove(_x);

            getSepsets().set(_x, _y, cond);

            System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, test.getPValue()));

            removed = true;
        }

        return removed;
    }


    private boolean depth0(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;
        int sampleSize = test.getSampleSize();

        if (sampleSize == 0) throw new IllegalArgumentException();

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                Node _x = nodes.get(x);
                Node _y = nodes.get(y);

                if (!adjacencies.get(_x).contains(_y)) continue;

                double r = corr.get(x, y);
                double fisherZ = sqrt(sampleSize) * 0.5 * (log(1.0 + r) - log(1.0 - r));
                double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

                double _cutoff = test.getAlpha();
                boolean independent = pvalue > _cutoff;

                if (independent) {
                    adjacencies.get(_x).remove(_y);
                    adjacencies.get(_y).remove(_x);

                    getSepsets().set(_x, _y, new ArrayList<Node>());

                    if (verbose) {
                        System.out.flush();
                        System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, new ArrayList<Node>(), pvalue));
//                            System.out.println(x + " _||_ " + y + " | the rest" + " p = " +
//                                    nf.format(test.getPValue()));
                    }

                    removed = true;
                }
            }
        }

        return removed;
    }

    private boolean depth1(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                Node _x = nodes.get(x);
                Node _y = nodes.get(y);
                if (!adjacencies.get(_x).contains(_y)) continue;

                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                int minr = -1;
                int maxr = -1;

                for (int r = 0; r < nodes.size(); r++) {
                    if (r == x || r == y) continue;
                    double xr = corr.get(x, r);
                    double yr = corr.get(y, r);
                    double prod = xr * yr;

                    if (prod < min) {
                        min = prod;
                        minr = r;
                    }

                    if (prod > max) {
                        max = prod;
                        maxr = r;
                    }
                }

                removed = removeDepth1(nodes, test, adjacencies, x, y, minr) || removed;
                removed = removeDepth1(nodes, test, adjacencies, x, y, maxr) || removed;
            }
        }

        return removed;
    }

    private boolean removeDepth1(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies, int x, int y, int z) {
        boolean removed = false;

        Node _x = nodes.get(x);
        Node _y = nodes.get(y);
        if (!adjacencies.get(_x).contains(_y)) return false;

        Node _z = nodes.get(z);

        double c1 = cov.get(x, y);
        double c2 = cov.get(x, z);
        double c3 = cov.get(z, y);

        double r = (c1 - c2 * c3) / (sqrt(1 - c2 * c2) * sqrt(1 - c3 * c3));
        double fisherZ = sqrt(test.getSampleSize()) * 0.5 * (log(1.0 + r) - log(1.0 - r));
        double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

        if (pvalue > test.getAlpha()) {
            adjacencies.get(_x).remove(_y);
            adjacencies.get(_y).remove(_x);

            getSepsets().set(_x, _y, Collections.singletonList(_z));

            if (verbose) {
                System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, Collections.singletonList(_z), pvalue));
            }

            removed = true;
        }
        return removed;
    }


    private boolean depth2(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies) {
        boolean removed = false;

        for (int x = 0; x < nodes.size(); x++) {
            for (int y = x + 1; y < nodes.size(); y++) {
                Node _x = nodes.get(x);
                Node _y = nodes.get(y);
                if (!adjacencies.get(_x).contains(_y)) continue;

                int maxr = -1;
                int maxs = -1;
                int minr = -1;
                int mins = -1;

                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;

                for (int r = 0; r < nodes.size(); r++) {
                    if (r == x || r == y) continue;
                    for (int s = 0; s < nodes.size(); s++) {
                        if (s == x || s == y || s == r) continue;
//                        double prod = corr.get(x, r) * corr.get(y, s);
                        double prod = corr.get(x, r) * corr.get(y, s) * corr.get(r, s);

                        if (prod < min) {
                            min = prod;
                            minr = r;
                            mins = s;
                        }

                        if (prod > max) {
                            max = prod;
                            maxr = r;
                            maxs = s;
                        }
                    }
                }

                if (minr == mins) continue;
                if (maxr == maxs) continue;

                removed = removeDepth2(nodes, test, adjacencies, x, y, minr, mins) || removed;
                removed = removeDepth2(nodes, test, adjacencies, x, y, maxr, maxs) || removed;
            }
        }

        return removed;
    }

    private boolean removeDepth2(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies, int x, int y, int z, int w) {
        boolean removed = false;

        Node _x = nodes.get(x);
        Node _y = nodes.get(y);
        if (!adjacencies.get(_x).contains(_y)) return false;

        Node _z = nodes.get(z);
        Node _w = nodes.get(w);

        int[] indices = new int[]{x, y, z, w};
        TetradMatrix prec = test.getCov().getMatrix().getSelection(indices, indices).inverse();
        double r = -prec.get(0, 1) / sqrt(prec.get(0, 0) * prec.get(1, 1));
        double fisherZ = sqrt(test.getSampleSize()) * 0.5 * (log(1.0 + r) - log(1.0 - r));
        double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(fisherZ)));

        if (pvalue > test.getAlpha()) {
            adjacencies.get(_x).remove(_y);
            adjacencies.get(_y).remove(_x);

            List<Node> cond = new ArrayList<Node>();
            cond.add(_z);
            cond.add(_w);

            getSepsets().set(_x, _y, cond);

            System.out.println(SearchLogUtils.independenceFactMsg(_x, _y, cond, pvalue));

            removed = true;
        }
        return removed;
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

    private boolean pcDepth(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies, int depth) {
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

                        boolean independent;

                        try {
                            independent = test.isIndependent(x, y, condSet);
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
                                System.out.println(SearchLogUtils.independenceFactMsg(x, y, condSet, test.getPValue()));
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

    private boolean searchAtDepthICov(List<Node> nodes, final IndependenceTest test, Map<Node, Set<Node>> adjacencies, int depth) {
        int count = 0;

        for (Node x : nodes) {
            if (++count % 100 == 0) System.out.println("count " + count + " of " + nodes.size());

            List<Node> adjx = new ArrayList<Node>(adjacencies.get(x));

            if (adjx.size() < depth) continue;

            ChoiceGenerator cg = new ChoiceGenerator(adjx.size(), depth);
            int[] choice;

            while ((choice = cg.next()) != null) {
                List<Node> condSet = GraphUtils.asList(choice, adjx);

                if (!adjacencies.get(x).containsAll(condSet)) {
                    continue;
                }

                List<Node> _cond = new ArrayList<Node>(condSet);
                _cond.add(x);
                searchICov(_cond, test, adjacencies, false);
            }
        }

//        System.out.println("Num removed = " + numRemoved);
//        return numRemoved > 0;

        return freeDegree(nodes, adjacencies) > depth;
    }

    private boolean searchICov(List<Node> nodes, IndependenceTest test, Map<Node, Set<Node>> adjacencies,
                               boolean addDependencies) {
        boolean removed = false;

        TetradMatrix subCovInv;

        int[] ind = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) ind[i] = test.getVariables().indexOf(nodes.get(i));
        subCovInv = test.getCov().getMatrix().getSelection(ind, ind).inverse();

        int sampleSize = test.getSampleSize();

        if (sampleSize == 0) throw new IllegalArgumentException();

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Node x = nodes.get(i);
                Node y = nodes.get(j);

                List<Double> pValues = new ArrayList<Double>();

                TetradMatrix inv = subCovInv;
                double r = -inv.get(i, j) / sqrt(inv.get(i, i) * inv.get(j, j));
                double fisherZ = sqrt(sampleSize - (nodes.size() - 2) - 3.0) *
                        0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double pvalue = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fisherZ)));

                double _cutoff = test.getAlpha();

                boolean independent = pvalue > _cutoff;

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
                            System.out.println(SearchLogUtils.independenceFactMsg(x, y, theRest, test.getPValue()));
//                            System.out.println(x + " _||_ " + y + " | the rest" + " p = " +
//                                    nf.format(test.getPValue()));
                        }

                        removed = true;
                    }
                }
            }
        }

        return removed;
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

    //============================ACCESSORS===============================//

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

    public int getNumIndependenceTests() {
        return 0;
    }

    public void setTrueGraph(Graph trueGraph) {
    }

    public int getNumFalseDependenceJudgments() {
        return 0;
    }

    public int getNumDependenceJudgments() {
        return 0;
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
}
