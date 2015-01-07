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
import edu.cmu.tetrad.data.DataModel;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemOptimizerEm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.ChoiceGenerator;

import java.util.*;


/**
 * Implements FindOneFactorCluster by Erich Kummerfeld (adaptation of a two factor
 * sextet algorithm to a one factor tetrad algorithm).
 *
 * @author Joseph Ramsey
 */
public class FindOneFactorClusters2 {
    public enum Algorithm {pureTetradsFirst, pureTripleFirst}

    public enum SortKey {size, pValue}

    // The list of all variables.
    private List<Node> variables;

    // The tetrad test--using Ricardo's. Used only for Wishart.
    private TetradTest test;

    // The significance level for the tetrad test.
    private double alpha;

    // The minimum p vaue for output clusters; clusters with lower p values will be ignored.
    private Double clusterMinP = Double.NaN;

    // Wishart or Bollen.
    private TestType testType;

    // The Bollen test. Testing two tetrads simultaneously.
    private DeltaTetradTest deltaTest;

    // independence test.
    private IndependenceTest indTest;

    private DataModel dataModel;

    // The depth of the PC search, -2 if the PC search should not be run.
    private List<List<Node>> clusters = new ArrayList<List<Node>>();

    private int depth = 0;

    private SortKey sortKey = SortKey.pValue;

    private Algorithm algorithm = Algorithm.pureTripleFirst;

    //========================================PUBLIC METHODS====================================//

    public FindOneFactorClusters2(ICovarianceMatrix cov, TestType testType, double alpha) {
        this.variables = cov.getVariables();
        this.test = new ContinuousTetradTest(cov, testType, this.alpha);
        this.indTest = new IndTestFisherZ(cov, this.alpha);
        this.alpha = alpha;
        this.testType = testType;
        deltaTest = new DeltaTetradTest(cov);
        this.dataModel = cov;
    }

    public FindOneFactorClusters2(DataSet dataSet, TestType testType, double alpha) {

        if (dataSet.isContinuous()) {
            this.variables = dataSet.getVariables();
            this.test = new ContinuousTetradTest(dataSet, testType, this.alpha);
            this.indTest = new IndTestFisherZ(dataSet, this.alpha);
            this.alpha = alpha;
            this.testType = testType;
            this.dataModel = dataSet;

            if (testType == TestType.TETRAD_BOLLEN) {
                deltaTest = new DeltaTetradTest(dataSet);
                deltaTest.setCacheFourthMoments(false);
            }
        } else if (dataSet.isDiscrete()) {
            this.variables = dataSet.getVariables();
            this.test = new DiscreteTetradTest(dataSet, this.alpha);
            this.indTest = new IndTestChiSquare(dataSet, this.alpha);
            this.alpha = alpha;
            this.testType = testType;
            this.dataModel = dataSet;

            if (testType == TestType.TETRAD_BOLLEN) {
                deltaTest = new DeltaTetradTest(dataSet);
                deltaTest.setCacheFourthMoments(false);
            }
        }
    }

    public Graph search() {
        if (algorithm == Algorithm.pureTripleFirst) {
            Set<List<Integer>> lists = estimateClustersTriadsFirst();
            Set<List<Integer>> allClusters = new HashSet<List<Integer>>();

            for (List<Integer> list : lists) {
                List<Integer> intCluster = new ArrayList<Integer>(list);
                List<Node> cluster = variablesForIndices(intCluster);
                this.clusters.add(cluster);
                allClusters.add(intCluster);
            }

            return convertToGraph(allClusters);
        }
        else if (algorithm == Algorithm.pureTetradsFirst) {
            Set<List<Integer>> lists = estimateClustersTetradsFirst();
            Set<List<Integer>> allClusters = new HashSet<List<Integer>>();

            for (List<Integer> list : lists) {
                List<Integer> intCluster = new ArrayList<Integer>(list);
                List<Node> cluster = variablesForIndices(intCluster);
                this.clusters.add(cluster);
                allClusters.add(intCluster);
            }

            return convertToGraph(allClusters);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    //========================================PRIVATE METHODS====================================//

    private Set<List<Integer>> estimateClustersTriadsFirst() {
        Set<List<Integer>> threeClusters = findThreeClusters();
        Set<List<Integer>> combined = combineThreeClusters(threeClusters);

        Set<List<Integer>> _combined = new HashSet<List<Integer>>();

        for (List<Integer> c : combined) {
            List a = new ArrayList<Integer>(c);
            Collections.sort(a);
            _combined.add(a);
        }

        return _combined;

    }

    private Set<List<Integer>> findThreeClusters() {
//        Graph graph = new EdgeListGraph(variables);
//        Fas fas = new Fas(graph, indTest);
//        fas.setDepth(0);     // 1?
//        Map<Node, Set<Node>> adjacencies = fas.searchMapOnly();

        List<Integer> allVariables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        if (allVariables.size() < 4) {
            return new HashSet<List<Integer>>();
        }

        ChoiceGenerator gen = new ChoiceGenerator(allVariables.size(), 3);
        int[] choice;
        Set<List<Integer>> threeClusters = new HashSet<List<Integer>>();

        CHOICE:
        while ((choice = gen.next()) != null) {
            int n1 = allVariables.get(choice[0]);
            int n2 = allVariables.get(choice[1]);
            int n3 = allVariables.get(choice[2]);

            List<Integer> triple = triple(n1, n2, n3);

//            if (!clique(triple, adjacencies)) {
//                continue;
//            }

//            int rejected = 0;

            for (int o : allVariables) {
                if (triple.contains(o)) {
                    continue;
                }

                List<Integer> cluster = quartet(n1, n2, n3, o);

                if (!quartetVanishes(cluster)) {
                    continue CHOICE;
//                    rejected++;
                }

//                if (rejected > variables.size() * 0.1) {
//                    continue CHOICE;
//                }
            }

            threeClusters.add(new ArrayList<Integer>(triple));
        }

        return threeClusters;
    }

    private Set<List<Integer>> combineThreeClusters(Set<List<Integer>> threeClusters) {
        Set<List<Integer>> grown = new HashSet<List<Integer>>();
        List<Integer> _variables = new ArrayList<Integer>();
        for (int i = 0; i < variables.size(); i++) _variables.add(i);

        for (List<Integer> cluster : threeClusters) {
            List<Integer> _cluster = new ArrayList<Integer>(cluster);

            // Strict.
            O:
            for (int o : _variables) {
                _cluster.add(o);

//                List<Integer> _cluster2 = new ArrayList<Integer>(_cluster);

                ChoiceGenerator gen = new ChoiceGenerator(_cluster.size(), 3);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    int n1 = _cluster.get(choice[0]);
                    int n2 = _cluster.get(choice[1]);
                    int n3 = _cluster.get(choice[2]);

                    List<Integer> triple = triple(n1, n2, n3);
                    if (!triple.contains(o)) continue;

                    if (!threeClusters.contains(triple)) {
                        _cluster.remove(new Integer(o));
                        continue O;
                    }
                }
            }

            grown.add(_cluster);
        }

        List<List<Integer>> _grown = new ArrayList<List<Integer>>(grown);
        final Map<List<Integer>, Double> pValues = new HashMap<List<Integer>, Double>();

        if (!Double.isNaN(clusterMinP) || sortKey == SortKey.pValue) {
            for (List<Integer> g : _grown) {
                double p = getClusterP(new ArrayList<Integer>(g));
                pValues.put(g, p);
            }
        }

        // Print the grown clusters.
        Collections.sort(_grown, new Comparator<List<Integer>>() {
            public int compare(List<Integer> o1, List<Integer> o2) {
                if (sortKey == SortKey.pValue) {
                    Double p1 = pValues.get(o2);
                    Double p2 = pValues.get(o1);
                    return Double.compare(Double.isNaN(p1) ? -1 : p1, Double.isNaN(p2) ? -1 : p2);
                } else if (sortKey == SortKey.size) {
                    return o2.size() - o1.size();
                } else {
                    throw new IllegalStateException();
                }
            }
        });

//        System.out.println("Grown");
//        for (List<Integer> l : _grown) {
//            List<Node> nodes = variablesForIndices(l);
//
//            for (int i = 0; i < nodes.size(); i++) {
//                System.out.print(nodes.get(i));
//
//                if (i < nodes.size() - 1) {
//                    System.out.print(" ");
//                }
//            }
//
//            if (sortKey == SortKey.size) {
//                System.out.println();
//            }
//            if (sortKey == SortKey.pValue) {
//                System.out.println("\t" + pValues.get(l));
//            }
//        }

        Set<List<Integer>> out = new HashSet<List<Integer>>();

        while (!_grown.isEmpty()) {
            List<Integer> maxCluster = _grown.remove(0);
            if (!Double.isNaN(clusterMinP) && maxCluster.size() == 3) {
                _grown.remove(maxCluster);
                continue;
            }
            if (!Double.isNaN(clusterMinP) && pValues.get(maxCluster) < clusterMinP) {
                grown.remove(maxCluster);
                continue;
            }
            out.add(maxCluster);

            // Remove from grown any cluster that intersects it.
            for (List<Integer> _cluster : new HashSet<List<Integer>>(_grown)) {
                Set<Integer> cluster2 = new HashSet<Integer>(_cluster);
                cluster2.retainAll(maxCluster);

                if (!cluster2.isEmpty()) {
                    _grown.remove(_cluster);
                }
            }
        }

//        NumberFormat nf = new DecimalFormat("0.0000");
//
//        // Print the output clusters.
//        System.out.println("Output clusters:");
//
//        for (Set<Integer> l : out) {
//            List<Node> nodes = variablesForIndices(l);
//
//            for (int i = 0; i < nodes.size(); i++) {
//                System.out.print(nodes.get(i));
//
//                if (i < nodes.size() - 1) {
//                    System.out.print(" ");
//                }
//            }
//
//            if (sortKey == SortKey.size) {
//                System.out.println();
//            }
//            else if (sortKey == SortKey.pValue) {
//                System.out.println("\t" + nf.format(pValues.get(l)));
//            }
//        }

        return out;
    }


    private List<Integer> triple(int n1, int n2, int n3) {
        List<Integer> triple = new ArrayList<Integer>();
        triple.add(n1);
        triple.add(n2);
        triple.add(n3);

        Collections.sort(triple);

        if (triple.size() < 3)
            throw new IllegalArgumentException("Triple elements must be unique: <" + n1 + ", " + n2 + ", " + n3 + ">");

        return triple;
    }

    // This is the main algorithm.
    private Set<List<Integer>> estimateClustersTetradsFirst() {
        Map<Node, Set<Node>> adjacencies;

        if (depth == -2) {
            adjacencies = new HashMap<Node, Set<Node>>();

            for (Node node : variables) {
                HashSet<Node> _nodes = new HashSet<Node>(variables);
                _nodes.remove(node);
                adjacencies.put(node, _nodes);
            }
        } else {
            System.out.println("Running PC adjacency search...");
            Graph graph = new EdgeListGraph(variables);
            Fas fas = new Fas(graph, indTest);
            fas.setDepth(depth);     // 1?
            adjacencies = fas.searchMapOnly();
            System.out.println("...done.");
        }

        List<Integer> _variables = new ArrayList<Integer>();
        for (int i = 0; i < variables.size(); i++) _variables.add(i);

        Set<List<Integer>> pureClusters = findPureClusters(_variables, adjacencies);
        for (List<Integer> cluster : pureClusters) _variables.removeAll(cluster);
        Set<List<Integer>> mixedClusters = findMixedClusters(_variables, unionPure(pureClusters), adjacencies);
        Set<List<Integer>> allClusters = new HashSet<List<Integer>>(pureClusters);
        allClusters.addAll(mixedClusters);
        return allClusters;

    }

    // Finds clusters of size 4 or higher.
    private Set<List<Integer>> findPureClusters(List<Integer> _variables, Map<Node, Set<Node>> adjacencies) {
        System.out.println("Original variables = " + variables);

        Set<List<Integer>> clusters = new HashSet<List<Integer>>();
        List<Integer> allVariables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        VARIABLES:
        while (!_variables.isEmpty()) {
            if (_variables.size() < 4) break;

            VARIABLE:
            for (int x : _variables) {
                Node nodeX = variables.get(x);
                List<Node> adjX = new ArrayList<Node>(adjacencies.get(nodeX));
                adjX.retainAll(variablesForIndices(new ArrayList<Integer>(_variables)));

                for (Node node : new ArrayList<Node>(adjX)) {
                    if (adjacencies.get(node).size() < 3) {
                        adjX.remove(node);
                    }
                }

                if (adjX.size() < 3) {
                    continue;
                }

                ChoiceGenerator gen = new ChoiceGenerator(adjX.size(), 3);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    Node nodeY = adjX.get(choice[0]);
                    Node nodeZ = adjX.get(choice[1]);
                    Node nodeW = adjX.get(choice[2]);

                    int y = variables.indexOf(nodeY);
                    int w = variables.indexOf(nodeW);
                    int z = variables.indexOf(nodeZ);

                    List<Integer> cluster = quartet(x, y, z, w);

                    if (!clique(cluster, adjacencies)) {
                        continue;
                    }

                    // Note that purity needs to be assessed with respect to all of the variables in order to
                    // remove all latent-measure impurities between pairs of latents.
                    if (pure(cluster, allVariables)) {

                        O:
                        for (int o : _variables) {
                            if (cluster.contains(o)) continue;
                            cluster.add(o);
                            List<Integer> _cluster = new ArrayList<Integer>(cluster);

                            if (!clique(cluster, adjacencies)) {
                                cluster.remove(o);
                                continue O;
                            }

//                            if (!allVariablesDependent(cluster)) {
//                                cluster.remove(o);
//                                continue O;
//                            }

                            ChoiceGenerator gen2 = new ChoiceGenerator(_cluster.size(), 4);
                            int[] choice2;
                            int count = 0;

                            while ((choice2 = gen2.next()) != null) {
                                int x2 = _cluster.get(choice2[0]);
                                int y2 = _cluster.get(choice2[1]);
                                int z2 = _cluster.get(choice2[2]);
                                int w2 = _cluster.get(choice2[3]);

                                List<Integer> quartet = quartet(x2, y2, z2, w2);

                                // Optimizes for large clusters.
                                if (quartet.contains(o)) {
                                    if (++count > 50) continue O;
                                }

                                if (quartet.contains(o) && !pure(quartet, allVariables)) {
                                    cluster.remove(o);
                                    continue O;
                                }
                            }
                        }

                        System.out.println("Cluster found: " + variablesForIndices(cluster));
                        clusters.add(cluster);
                        _variables.removeAll(cluster);

                        continue VARIABLES;
                    }
                }
            }

            break;
        }

        return clusters;
    }

    // Trying to optimize the search for 4-cliques a bit.
    private Set<List<Integer>> findPureClusters2(List<Integer> _variables, Map<Node, Set<Node>> adjacencies) {
        System.out.println("Original variables = " + variables);

        Set<List<Integer>> clusters = new HashSet<List<Integer>>();
        List<Integer> allVariables = new ArrayList<Integer>();
        Set<Node> foundVariables = new HashSet<Node>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        for (int x : _variables) {
            Node nodeX = variables.get(x);
            if (foundVariables.contains(nodeX)) continue;

            List<Node> adjX = new ArrayList<Node>(adjacencies.get(nodeX));
            adjX.removeAll(foundVariables);

            if (adjX.size() < 3) continue;

            for (Node nodeY : adjX) {
                if (foundVariables.contains(nodeY)) continue;

                List<Node> commonXY = new ArrayList<Node>(adjacencies.get(nodeY));
                commonXY.retainAll(adjX);
                commonXY.removeAll(foundVariables);

                for (Node nodeZ : commonXY) {
                    if (foundVariables.contains(nodeZ)) continue;

                    List<Node> commonXZ = new ArrayList<Node>(commonXY);
                    commonXZ.retainAll(adjacencies.get(nodeZ));
                    commonXZ.removeAll(foundVariables);

                    for (Node nodeW : commonXZ) {
                        if (foundVariables.contains(nodeW)) continue;

                        if (!adjacencies.get(nodeY).contains(nodeW)) {
                            continue;
                        }

                        int y = variables.indexOf(nodeY);
                        int w = variables.indexOf(nodeW);
                        int z = variables.indexOf(nodeZ);

                        List<Integer> cluster = quartet(x, y, z, w);

                        // Note that purity needs to be assessed with respect to all of the variables in order to
                        // remove all latent-measure impurities between pairs of latents.
                        if (pure(cluster, allVariables)) {

                            O:
                            for (int o : _variables) {
                                if (cluster.contains(o)) continue;
                                cluster.add(o);

                                if (!clique(cluster, adjacencies)) {
                                    cluster.remove(o);
                                    continue O;
                                }

//                                if (!allVariablesDependent(cluster)) {
//                                    cluster.remove(o);
//                                    continue O;
//                                }

                                List<Integer> _cluster = new ArrayList<Integer>(cluster);

                                ChoiceGenerator gen2 = new ChoiceGenerator(_cluster.size(), 4);
                                int[] choice2;
                                int count = 0;

                                while ((choice2 = gen2.next()) != null) {
                                    int x2 = _cluster.get(choice2[0]);
                                    int y2 = _cluster.get(choice2[1]);
                                    int z2 = _cluster.get(choice2[2]);
                                    int w2 = _cluster.get(choice2[3]);

                                    List<Integer> quartet = quartet(x2, y2, z2, w2);

                                    // Optimizes for large clusters.
                                    if (quartet.contains(o)) {
                                        if (++count > 2) continue O;
                                    }

                                    if (quartet.contains(o) && !pure(quartet, allVariables)) {
                                        cluster.remove(o);
                                        continue O;
                                    }
                                }
                            }

                            System.out.println("Cluster found: " + variablesForIndices(cluster));
                            clusters.add(cluster);
                            foundVariables.addAll(variablesForIndices(cluster));
                        }
                    }
                }
            }
        }

        return clusters;
    }

    //  Finds clusters of size 3.
    private Set<List<Integer>> findMixedClusters(List<Integer> remaining, List<Integer> unionPure, Map<Node, Set<Node>> adjacencies) {
        Set<List<Integer>> threeClusters = new HashSet<List<Integer>>();

        if (unionPure.isEmpty()) {
            return new HashSet<List<Integer>>();
        }

        REMAINING:
        while (true) {
            if (remaining.size() < 3) break;

            ChoiceGenerator gen = new ChoiceGenerator(remaining.size(), 3);
            int[] choice;

            while ((choice = gen.next()) != null) {
                int y = remaining.get(choice[0]);
                int z = remaining.get(choice[1]);
                int w = remaining.get(choice[2]);

                List<Integer> cluster = new ArrayList<Integer>();
                cluster.add(y);
                cluster.add(z);
                cluster.add(w);

//                if (!allVariablesDependent(cluster)) {
//                    continue;
//                }

                if (!clique(cluster, adjacencies)) {
                    continue;
                }

                // Check all x as a cross check; really only one should be necessary.
                boolean allX = true;

                for (int x : unionPure) {
                    List<Integer> _cluster = new ArrayList<Integer>(cluster);
                    _cluster.add(x);

                    if (!quartetVanishes(_cluster) || !significant(new ArrayList<Integer>(_cluster))) {
                        allX = false;
                        break;
                    }
                }

                if (allX) {
                    threeClusters.add(cluster);
                    unionPure.addAll(cluster);
                    remaining.removeAll(cluster);

                    System.out.println("3-cluster found: " + variablesForIndices(cluster));

                    continue REMAINING;
                }
            }

            break;
        }

        return threeClusters;
    }

    private boolean clique(List<Integer> cluster, Map<Node, Set<Node>> adjacencies) {
        for (int i = 0; i < cluster.size(); i++) {
            for (int j = i + 1; j < cluster.size(); j++) {
                Node nodei = variables.get(cluster.get(i));
                Node nodej = variables.get(cluster.get(j));

                if (!adjacencies.get(nodei).contains(nodej)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<Node> variablesForIndices(List<Integer> cluster) {
        List<Node> _cluster = new ArrayList<Node>();

        for (int c : cluster) {
            _cluster.add(variables.get(c));
        }

        Collections.sort(_cluster);

        return _cluster;
    }


    private boolean pure(List<Integer> quartet, List<Integer> variables) {
        if (quartetVanishes(quartet)) {
            for (int o : variables) {
                if (quartet.contains(o)) continue;

                for (int p : quartet) {
                    List<Integer> _quartet = new ArrayList<Integer>(quartet);
                    _quartet.remove(p);
                    _quartet.add(o);

                    if (!quartetVanishes(_quartet)) {
                        return false;
                    }
                }
            }

            return significant(new ArrayList<Integer>(quartet));
        }

        return false;
    }

    private List<Integer> quartet(int x, int y, int z, int w) {
        List<Integer> set = new ArrayList<Integer>();
        set.add(x);
        set.add(y);
        set.add(z);
        set.add(w);

        Collections.sort(set);

        if (set.size() < 4)
            throw new IllegalArgumentException("Quartet elements must be unique: <" + x + ", " + y + ", " + z + ", " + w + ">");

        return set;
    }

    private boolean quartetVanishes(List<Integer> quartet) {
        if (quartet.size() != 4) throw new IllegalArgumentException("Expecting a quartet, size = " + quartet.size());

        Iterator<Integer> iter = quartet.iterator();
        int x = iter.next();
        int y = iter.next();
        int z = iter.next();
        int w = iter.next();

        return testVanishing(x, y, z, w);
    }

    private boolean testVanishing(int x, int y, int z, int w) {
        if (testType == TestType.TETRAD_BOLLEN) {
            Tetrad t1 = new Tetrad(variables.get(x), variables.get(y), variables.get(z), variables.get(w));
            Tetrad t2 = new Tetrad(variables.get(x), variables.get(y), variables.get(w), variables.get(z));
            double p = deltaTest.getPValue(t1, t2);
            return p > alpha;
        } else {
            return test.tetradHolds(x, y, z, w) && test.tetradHolds(x, y, w, z);
        }
    }

    private Graph convertSearchGraphNodes(Set<Set<Node>> clusters) {
        Graph graph = new EdgeListGraph(variables);

        List<Node> latents = new ArrayList<Node>();
        for (int i = 0; i < clusters.size(); i++) {
            Node latent = new GraphNode(MimBuild.LATENT_PREFIX + (i + 1));
            latent.setNodeType(NodeType.LATENT);
            latents.add(latent);
            graph.addNode(latent);
        }

        List<Set<Node>> _clusters = new ArrayList<Set<Node>>(clusters);

        for (int i = 0; i < latents.size(); i++) {
            for (Node node : _clusters.get(i)) {
                if (!graph.containsNode(node)) graph.addNode(node);
                graph.addDirectedEdge(latents.get(i), node);
            }
        }

        return graph;
    }

    private Graph convertToGraph(Set<List<Integer>> allClusters) {
        Set<Set<Node>> _clustering = new HashSet<Set<Node>>();

        for (List<Integer> cluster : allClusters) {
            Set<Node> nodes = new HashSet<Node>();

            for (int i : cluster) {
                nodes.add(variables.get(i));
            }

            _clustering.add(nodes);
        }

        return convertSearchGraphNodes(_clustering);
    }

    private List<Integer> unionPure(Set<List<Integer>> pureClusters) {
        List<Integer> unionPure = new ArrayList<Integer>();

        for (List<Integer> cluster : pureClusters) {
            unionPure.addAll(cluster);
        }

        return unionPure;
    }

    private boolean significant(List<Integer> cluster) {
        double p = getClusterP(cluster);

        return p > alpha;
    }

    private double getClusterP2(List<Node> c) {
        Graph g = new EdgeListGraph(c);
        Node l = new GraphNode("L");
        l.setNodeType(NodeType.LATENT);
        g.addNode(l);

        for (Node n : c) {
            g.addDirectedEdge(l, n);
        }

        SemPm pm = new SemPm(g);
        SemEstimator est;
        if (dataModel instanceof DataSet) {
            est = new SemEstimator((DataSet) dataModel, pm, new SemOptimizerEm());
        } else {
            est = new SemEstimator((CovarianceMatrix) dataModel, pm, new SemOptimizerEm());
        }
        SemIm estIm = est.estimate();
        double pValue = estIm.getPValue();
        return pValue == 1 ? Double.NaN : pValue;
    }


    private double getClusterP(List<Integer> cluster) {
        return getClusterP2(variablesForIndices(new ArrayList<Integer>(cluster)));

//        double p;
//
//        if (cluster.size() == 4) {
//            double chisq = getClusterChiSquare(cluster);
//            int dof = 2;
//            double q = ProbUtils.chisqCdf(chisq, dof);
//            p = 1.0 - q;
//
//            if (p > alpha) {
//                System.out.println("Trying cluster " + variablesForIndices(new HashSet<Integer>(cluster)));
//                System.out.println("chisq = " + chisq + " p = " + p);
//            }
//        } else if (cluster.size() == 3) {
//            double chisq = getClusterChiSquare(cluster);
//            int dof = 0;
//            double q = ProbUtils.chisqCdf(chisq, dof);
//            p = 1.0 - q;
//
//            if (p > alpha) {
//                System.out.println("Trying cluster " + variablesForIndices(new HashSet<Integer>(cluster)));
//                System.out.println("chisq (3) = " + chisq + " p = " + p);
//            }
//        }
//        else {
//            SemEstimator est = getSemEstimator(cluster);
//            SemIm im = est.estimate();
//            return im.getPValue();
//        }
//
//        return p;
    }

    private double getClusterChiSquare(List<Integer> c) {
        SemEstimator est = getSemEstimator(c);
        SemIm estIm = est.estimate();
        return estIm.getChiSquare();
    }

    private SemEstimator getSemEstimator(List<Integer> c) {
        List<Node> z = new ArrayList<Node>();
        for (int i : c) z.add(variables.get(i));

        Graph g = new EdgeListGraph(z);
        Node l = new GraphNode("L");
        l.setNodeType(NodeType.LATENT);
        g.addNode(l);

        for (int n : c) {
            g.addDirectedEdge(l, variables.get(n));
        }

        SemPm pm = new SemPm(g);
        SemEstimator est;
        if (dataModel instanceof DataSet) {
            est = new SemEstimator((DataSet) dataModel, pm, new SemOptimizerEm());
        } else {
            est = new SemEstimator((CovarianceMatrix) dataModel, pm, new SemOptimizerEm());
        }
        return est;
    }


    public List<List<Node>> getClusters() {
        return clusters;
    }

    public SortKey getSortKey() {
        return sortKey;
    }

    public void setSortKey(SortKey sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * Clusters with p value balow this will not be returned, saving you time.
     */
    public Double getClusterMinP() {
        return clusterMinP;
    }

    public void setClusterMinP(Double clusterMinP) {
        if (clusterMinP < 0 || clusterMinP > 1) throw new IllegalArgumentException();
        this.clusterMinP = clusterMinP;
    }

}


