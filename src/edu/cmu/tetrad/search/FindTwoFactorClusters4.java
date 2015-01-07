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
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.ChoiceGenerator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


/**
 * Implements FindOneFactorCluster by Erich Kummerfeld (adaptation of a two factor
 * sextet algorithm to a one factor tetrad algorithm).
 *
 * @author Joseph Ramsey
 */
public class FindTwoFactorClusters4 {

    private DataSet data;
    private ICovarianceMatrix cov;

    public enum SortKey {size, pValue}

    // The list of all variables.
    private List<Node> variables;
    private List<String> varNames;

    // The significance level for the tetrad test.
    private double alpha;

    // The minimum p vaue for output clusters; clusters with lower p values will be ignored.
    private Double clusterMinP = Double.NaN;

    // The Delta test. Testing tetrads simultaneously.
    private IDeltaSextadTest deltaTest;

    // independence test.
    private IndependenceTest indTest;

    // The depth of the PC search, -2 if the PC search should not be run.
    private List<List<Node>> clusters = new ArrayList<List<Node>>();

    private int depth = 0;

    private SortKey sortKey = SortKey.pValue;

    //========================================PUBLIC METHODS====================================//

    public FindTwoFactorClusters4(ICovarianceMatrix cov, double alpha) {
        this.cov = new CovarianceMatrix(cov);

        for (int i = 0; i < cov.getDimension(); i++) {
            for (int j = i + 1; j < cov.getDimension(); j++) {
                double _cov = cov.getValue(i, j);

                if (Math.abs(_cov) < 0.0) {
                    cov.setValue(i, j, 0);
                }
            }
        }

        this.variables = cov.getVariables();
        this.varNames = cov.getVariableNames();
        this.indTest = new IndTestFisherZ(cov, alpha);
        this.alpha = alpha;
        this.deltaTest = new DeltaSextadTest(cov);
    }

    public FindTwoFactorClusters4(DataSet dataSet, double alpha) {
        this.data = dataSet;
        this.variables = dataSet.getVariables();
        this.varNames = dataSet.getVariableNames();
        this.indTest = new IndTestFisherZ(dataSet, alpha);
        this.alpha = alpha;
        this.deltaTest = new DeltaSextadTest(dataSet);
    }

    public Graph search() {
        deltaTest.setCacheFourthMoments(false);

        boolean sextadsFirst = false;

        if (sextadsFirst) {
            List<List<Integer>> allClusters = estimateClustersSextadsFirst();
            recordClusters(allClusters);
            return convertToGraph(allClusters);
        } else {
            List<List<Integer>> allClusters = estimateClustersPentadsFirst();
            recordClusters(allClusters);
            return convertToGraph(allClusters);
        }
    }

    private void recordClusters(List<List<Integer>> allClusters) {
        clusters = new ArrayList<List<Node>>();

        for (List<Integer> cluster2 : allClusters) {
            List<Integer> cluster1 = new ArrayList<Integer>(cluster2);
            Collections.sort(cluster1);

            List<Node> cluster = new ArrayList<Node>();

            for (int i : cluster1) {
                cluster.add(variables.get(i));
            }

            clusters.add(cluster);
        }
    }

    //========================================PRIVATE METHODS====================================//

    // This is the main algorithm.
    private List<List<Integer>> estimateClustersSextadsFirst() {
        Map<Node, Set<Node>> adjacencies;

        if (depth == -2) {
            adjacencies = new HashMap<Node, Set<Node>>();

            for (Node node : variables) {
                Set<Node> _nodes = new HashSet<Node>(variables);
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

        List<List<Integer>> pureClusters = findPureClusters(_variables, adjacencies);
        for (List<Integer> cluster : pureClusters) _variables.removeAll(cluster);
        List<List<Integer>> mixedClusters = findMixedClusters(_variables, unionPure(pureClusters), adjacencies);
        List<List<Integer>> allClusters = new ArrayList<List<Integer>>(pureClusters);
        allClusters.addAll(mixedClusters);
        return allClusters;

    }

    // Finds clusters of size 6 or higher.
    private List<List<Integer>> findPureClusters(List<Integer> _variables, Map<Node, Set<Node>> adjacencies) {
        System.out.println("Original variables = " + variables);

        List<List<Integer>> clusters = new ArrayList<List<Integer>>();
        List<Integer> allVariables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        VARIABLES:
        while (!_variables.isEmpty()) {
            if (_variables.size() < 6) break;

            VARIABLE:
            for (int x : _variables) {
                Node nodeX = variables.get(x);
                List<Node> adjX = new ArrayList<Node>(adjacencies.get(nodeX));
                adjX.retainAll(variablesForIndices(_variables));

                for (Node node : new ArrayList<Node>(adjX)) {
                    if (adjacencies.get(node).size() < 5) {
                        adjX.remove(node);
                    }
                }

                if (adjX.size() < 5) {
                    continue;
                }

                ChoiceGenerator gen = new ChoiceGenerator(adjX.size(), 5);
                int[] choice;

                while ((choice = gen.next()) != null) {
                    Node nodeY = adjX.get(choice[0]);
                    Node nodeZ = adjX.get(choice[1]);
                    Node nodeW = adjX.get(choice[2]);
                    Node nodeR = adjX.get(choice[3]);
                    Node nodeS = adjX.get(choice[4]);

                    int y = variables.indexOf(nodeY);
                    int z = variables.indexOf(nodeZ);
                    int w = variables.indexOf(nodeW);
                    int r = variables.indexOf(nodeR);
                    int s = variables.indexOf(nodeS);

                    List<Integer> cluster = sextad(x, y, z, w, r, s);

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

                            if (!clique(cluster, adjacencies)) {
                                cluster.remove(new Integer(o));
                                continue O;
                            }

                            ChoiceGenerator gen2 = new ChoiceGenerator(cluster.size(), 6);
                            int[] choice2;
                            int count = 0;

                            while ((choice2 = gen2.next()) != null) {
                                int x2 = cluster.get(choice2[0]);
                                int y2 = cluster.get(choice2[1]);
                                int z2 = cluster.get(choice2[2]);
                                int w2 = cluster.get(choice2[3]);
                                int r2 = cluster.get(choice2[4]);
                                int s2 = cluster.get(choice2[5]);

                                List<Integer> sextad = sextad(x2, y2, z2, w2, r2, s2);

//                                // Optimizes for large clusters.
//                                if (quartet.contains(o)) {
//                                    if (++count > 50) continue O;
//                                }

                                if (sextad.contains(o) && !pure(sextad, allVariables)) {
                                    cluster.remove(new Integer(o));
                                    continue O;
                                }

                                if (!significant(cluster)) {
                                    cluster.remove(new Integer(o));
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

    //  Finds clusters of size 3.
    private List<List<Integer>> findMixedClusters(List<Integer> remaining, Set<Integer> unionPure, Map<Node, Set<Node>> adjacencies) {
        List<List<Integer>> fiveClusters = new ArrayList<List<Integer>>();

        if (unionPure.isEmpty()) {
            return new ArrayList<List<Integer>>();
        }

        REMAINING:
        while (true) {
            if (remaining.size() < 5) break;

            ChoiceGenerator gen = new ChoiceGenerator(remaining.size(), 5);
            int[] choice;

            while ((choice = gen.next()) != null) {
                int y = remaining.get(choice[0]);
                int z = remaining.get(choice[1]);
                int w = remaining.get(choice[2]);
                int r = remaining.get(choice[3]);
                int s = remaining.get(choice[4]);

                List<Integer> cluster = new ArrayList<Integer>();
                cluster.add(y);
                cluster.add(z);
                cluster.add(w);
                cluster.add(r);
                cluster.add(s);

                if (!clique(cluster, adjacencies)) {
                    continue;
                }

                // Check all x as a cross check; really only one should be necessary.
                boolean allX = true;

                for (int x : unionPure) {
                    List<Integer> _cluster = new ArrayList<Integer>(cluster);
                    _cluster.add(x);

                    if (!sextetVanishes(_cluster)) {// || !significant(_cluster)) {
                        allX = false;
                        break;
                    }
                }

                if (allX) {
                    fiveClusters.add(cluster);
                    unionPure.addAll(cluster);
                    remaining.removeAll(cluster);

                    System.out.println("5-cluster found: " + variablesForIndices(cluster));

                    continue REMAINING;
                }
            }

            break;
        }

        return fiveClusters;
    }

    private boolean clique(List<Integer> cluster, Map<Node, Set<Node>> adjacencies) {
//        if (true) return true;

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

    private boolean allVariablesDependent(List<Integer> cluster) {
        for (int i = 0; i < cluster.size(); i++) {
            for (int j = i + 1; j < cluster.size(); j++) {
                Node nodei = variables.get(cluster.get(i));
                Node nodej = variables.get(cluster.get(j));

                if (!indTest.isDependent(nodei, nodej)) {
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

        return _cluster;
    }


    private boolean pure(List<Integer> sextad, List<Integer> variables) {
        if (sextetVanishes(sextad)) {
            for (int o : variables) {
                if (sextad.contains(o)) continue;

                for (int p : sextad) {
                    List<Integer> _quartet = new ArrayList<Integer>(sextad);
                    _quartet.remove(new Integer(p));
                    _quartet.add(o);

                    if (!sextetVanishes(_quartet)) {
                        return false;
                    }
                }
            }

            return true;

//            return significant(new ArrayList<Integer>(quartet));
        }

        return false;
    }

    private List<Integer> sextad(int x, int y, int z, int w, int r, int s) {
        List<Integer> List = new ArrayList<Integer>();
        List.add(x);
        List.add(y);
        List.add(z);
        List.add(w);
        List.add(r);
        List.add(s);

        return List;
    }

    private boolean sextetVanishes(List<Integer> sextet) {
        if (sextet.size() != 6) throw new IllegalArgumentException("Expecting a sextet, size = " + sextet.size());

        int n1 = sextet.get(0);
        int n2 = sextet.get(1);
        int n3 = sextet.get(2);
        int n4 = sextet.get(3);
        int n5 = sextet.get(4);
        int n6 = sextet.get(5);

        return testVanishing(n1, n2, n3, n4, n5, n6);
    }

    private boolean testVanishing(int n1, int n2, int n3, int n4, int n5, int n6) {
        Node m1 = variables.get(n1);
        Node m2 = variables.get(n2);
        Node m3 = variables.get(n3);
        Node m4 = variables.get(n4);
        Node m5 = variables.get(n5);
        Node m6 = variables.get(n6);

        List<Sextad> t = new ArrayList<Sextad>();

        Sextad t1 = new Sextad(m1, m2, m3, m4, m5, m6);
        Sextad t2 = new Sextad(m1, m2, m4, m3, m5, m6);
        Sextad t3 = new Sextad(m1, m2, m5, m3, m4, m6);
        Sextad t4 = new Sextad(m1, m2, m6, m3, m4, m5);
        Sextad t5 = new Sextad(m1, m3, m4, m2, m5, m6);
        Sextad t6 = new Sextad(m1, m3, m5, m2, m4, m6);
        Sextad t7 = new Sextad(m1, m3, m6, m2, m4, m5);
        Sextad t8 = new Sextad(m1, m4, m5, m2, m3, m6);
        Sextad t9 = new Sextad(m1, m4, m6, m2, m3, m5);
        Sextad t10 = new Sextad(m1, m5, m6, m2, m3, m4);

        t.add(t1);
        t.add(t2);
        t.add(t3);
        t.add(t4);
        t.add(t5);
        t.add(t6);
        t.add(t7);
        t.add(t8);
        t.add(t9);
        t.add(t10);

        if (false) {
            int numSextads = 9;

            ChoiceGenerator gen = new ChoiceGenerator(t.size(), numSextads);
            int[] choice;
            int accept = 0;
            int s = -1;

            while ((choice = gen.next()) != null) {
                ++s;
                Sextad[] _sextads = new Sextad[numSextads];

                for (int i = 0; i < numSextads; i++) {
                    _sextads[i] = t.get(choice[i]);
                }

                double p = deltaTest.getPValue(_sextads);

                if (p > alpha) {
                    accept++;
                }
            }

            return accept >= 1;
        } else if (true) { //(testType == TestType.TETRAD_DELTA) {
//            double p0 = test.getPValue(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);

            int count = 0;

            for (int i = 0; i < 10; i++) {
                List<Sextad> _t2 = new ArrayList<Sextad>(t);
                _t2.remove(i);

                Sextad[] _t = new Sextad[_t2.size()];

                for (int m = 0; m < _t.length; m++) {
                    _t[m] = _t2.get(m);
                }

                double p = deltaTest.getPValue(_t);

                if (p > alpha) {
                    count++;
                    if (count > 8) return true;
                }
            }

            return false;
        } else {

            // Tests sextads individually and checks that they all pass.

            int count = 0;
            double sum = 0.0;

            for (int i = 0; i < t.size(); i++) {
                double p = deltaTest.getPValue(t.get(i));

                if (p > alpha) {
                    sum += p;
                    count++;
                }
            }

            return count >= 6;
        }
    }

    private Graph convertSearchGraphNodes(List<List<Node>> clusters) {
        Graph graph = new EdgeListGraph(variables);

        List<Node> latents = new ArrayList<Node>();
        for (int i = 0; i < clusters.size(); i++) {
            Node latent = new GraphNode(MimBuild.LATENT_PREFIX + (i + 1));
            latent.setNodeType(NodeType.LATENT);
            latents.add(latent);
            graph.addNode(latent);
        }

        List<List<Node>> _clusters = new ArrayList<List<Node>>(clusters);

        for (int i = 0; i < latents.size(); i++) {
            for (Node node : _clusters.get(i)) {
                if (!graph.containsNode(node)) graph.addNode(node);
                graph.addDirectedEdge(latents.get(i), node);
            }
        }

        return graph;
    }

    private Graph convertToGraph(List<List<Integer>> allClusters) {
        List<List<Node>> _clustering = new ArrayList<List<Node>>();

        for (List<Integer> cluster : allClusters) {
            List<Node> nodes = new ArrayList<Node>();

            for (int i : cluster) {
                nodes.add(variables.get(i));
            }

            _clustering.add(nodes);
        }

        return convertSearchGraphNodes(_clustering);
    }

    private Set<Integer> unionPure(List<List<Integer>> pureClusters) {
        Set<Integer> unionPure = new HashSet<Integer>();

        for (List<Integer> cluster : pureClusters) {
            unionPure.addAll(cluster);
        }

        return unionPure;
    }

    private boolean significant(List<Integer> cluster) {
        double p = getClusterP(new ArrayList<Integer>(cluster));

        return p > clusterMinP;
    }

    private double getClusterP(List<Integer> cluster) {
        SemIm im = estimateClusterModel(cluster);
        return im.getPValue();
    }

    private SemIm estimateClusterModel(List<Integer> sextet) {
        Graph g = new EdgeListGraph();
        Node l1 = new GraphNode("L1");
        l1.setNodeType(NodeType.LATENT);
        Node l2 = new GraphNode("L2");
        l2.setNodeType(NodeType.LATENT);
        g.addNode(l1);
        g.addNode(l2);

        for (int i = 0; i < sextet.size(); i++) {
            Node n = this.variables.get(sextet.get(i));
            g.addNode(n);
            g.addDirectedEdge(l1, n);
            g.addDirectedEdge(l2, n);
        }

        SemPm pm = new SemPm(g);

        SemEstimator est;

        if (data != null) {
            est = new SemEstimator(data, pm, new SemOptimizerEm());
        } else if (cov != null) {
            est = new SemEstimator(cov, pm, new SemOptimizerEm());
        } else {
            throw new IllegalArgumentException();
        }

        return est.estimate();
    }

    private SemIm estimateModel(List<List<Integer>> clusters) {
        Graph g = new EdgeListGraph();

        List<Node> upperLatents = new ArrayList<Node>();
        List<Node> lowerLatents = new ArrayList<Node>();

        for (int i = 0; i < clusters.size(); i++) {
            List<Integer> cluster = clusters.get(i);
            Node l1 = new GraphNode("L1." + (i + 1));
            l1.setNodeType(NodeType.LATENT);

            Node l2 = new GraphNode("L2." + (i + 1));
            l2.setNodeType(NodeType.LATENT);

            upperLatents.add(l1);
            lowerLatents.add(l2);

            g.addNode(l1);
            g.addNode(l2);

            for (int k = 0; k < cluster.size(); k++) {
                Node n = this.variables.get(cluster.get(k));
                g.addNode(n);
                g.addDirectedEdge(l1, n);
                g.addDirectedEdge(l2, n);
            }
        }

        for (int i = 0; i < upperLatents.size(); i++) {
            for (int j = i + 1; j < upperLatents.size(); j++) {
                g.addDirectedEdge(upperLatents.get(i), upperLatents.get(j));
                g.addDirectedEdge(lowerLatents.get(i), lowerLatents.get(j));
            }
        }

        for (int i = 0; i < upperLatents.size(); i++) {
            for (int j = 0; j < lowerLatents.size(); j++) {
                if (i == j) continue;
                g.addDirectedEdge(upperLatents.get(i), lowerLatents.get(j));
            }
        }

        SemPm pm = new SemPm(g);

        for (Node node : upperLatents) {
            Parameter p = pm.getParameter(node, node);
            p.setFixed(true);
            p.setStartingValue(1.0);
        }

        for (Node node : lowerLatents) {
            Parameter p = pm.getParameter(node, node);
            p.setFixed(true);
            p.setStartingValue(1.0);
        }

        SemEstimator est;

        if (data != null) {
            est = new SemEstimator(data, pm, new SemOptimizerEm());
        } else if (cov != null) {
            est = new SemEstimator(cov, pm, new SemOptimizerEm());
        } else {
            throw new IllegalArgumentException();
        }

        return est.estimate();
    }

    private List<List<Integer>> estimateClustersPentadsFirst() {
        Set<Set<Integer>> fiveClusters = findFiveClusters();
        List<List<Integer>> combined = combineFiveClusters(fiveClusters);

        List<List<Integer>> _combined = new ArrayList<List<Integer>>();

        for (List<Integer> c : combined) {
            List a = new ArrayList<Integer>(c);
            Collections.sort(a);
            _combined.add(a);
        }

        return _combined;

    }

    private Set<Set<Integer>> findFiveClusters() {
        Graph graph = new EdgeListGraph(variables);
        Fas fas = new Fas(graph, indTest);
        fas.setDepth(0);
        Map<Node, Set<Node>> adjacencies = fas.searchMapOnly();

        List<Integer> allVariables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) allVariables.add(i);

        if (allVariables.size() < 6) {
            return new HashSet<Set<Integer>>();
        }

        ChoiceGenerator gen = new ChoiceGenerator(allVariables.size(), 5);
        int[] choice;
        Set<Set<Integer>> fiveClusters = new HashSet<Set<Integer>>();

        CHOICE:
        while ((choice = gen.next()) != null) {
            int n1 = allVariables.get(choice[0]);
            int n2 = allVariables.get(choice[1]);
            int n3 = allVariables.get(choice[2]);
            int n4 = allVariables.get(choice[3]);
            int n5 = allVariables.get(choice[4]);

            List<Integer> pentad = pentad(n1, n2, n3, n4, n5);

            if (!clique(pentad, adjacencies)) {
                continue;
            }

            int rejected = 0;

            for (int o : allVariables) {
                if (pentad.contains(o)) {
                    continue;
                }

                List<Integer> cluster = sextet(o, n1, n2, n3, n4, n5);

                if (!sextetVanishes(cluster)) {
                    rejected++;

                    if (rejected > 0) {
                        continue CHOICE;
                    }
                }
            }

            System.out.println("++" + variablesForIndices(pentad));

            fiveClusters.add(new HashSet<Integer>(pentad));
        }

        return fiveClusters;
    }



    private List<List<Integer>> combineFiveClusters(Set<Set<Integer>> fiveClusters) {
        List<List<Integer>> grown = new ArrayList<List<Integer>>();
        List<Integer> variables = new ArrayList<Integer>();
        for (int i = 0; i < this.variables.size(); i++) variables.add(i);

        for (Set<Integer> cluster : fiveClusters) {
            List<Integer> _cluster = new ArrayList<Integer>(cluster);

            // Strict.
            for (int o : variables) {
                if (_cluster.contains(o)) continue;

                _cluster.add(o);

                ChoiceGenerator gen = new ChoiceGenerator(_cluster.size(), 5);
                int[] choice;
                boolean rejected = false;

                while ((choice = gen.next()) != null) {
                    int n1 = _cluster.get(choice[0]);
                    int n2 = _cluster.get(choice[1]);
                    int n3 = _cluster.get(choice[2]);
                    int n4 = _cluster.get(choice[3]);
                    int n5 = _cluster.get(choice[4]);

                    List<Integer> pentad = pentad(n1, n2, n3, n4, n5);

                    if (!pentad.contains(o)) continue;

                    if (!fiveClusters.contains(new HashSet<Integer>(pentad))) {
                        rejected = true;
                        break;
                    }
                }

                if (rejected) {
                    _cluster.remove(new Integer(o));
                }
            }

            grown.add(_cluster);
        }

        List<List<Integer>> _grown = new ArrayList<List<Integer>>(grown);
        final Map<List<Integer>, Double> pValues = new HashMap<List<Integer>, Double>();

        if (!Double.isNaN(clusterMinP) || sortKey == SortKey.pValue) {
            for (List<Integer> g : _grown) {
                double p = getClusterP(g);
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

        System.out.println("Grown");
        for (List<Integer> l : _grown) {
            List<Node> nodes = variablesForIndices(l);

            for (int i = 0; i < nodes.size(); i++) {
                System.out.print(nodes.get(i));

                if (i < nodes.size() - 1) {
                    System.out.print(" ");
                }
            }

            if (sortKey == SortKey.size) {
                System.out.println();
            }
            if (sortKey == SortKey.pValue) {
                System.out.println("\t" + pValues.get(l));
            }
        }

        List<List<Integer>> out = new ArrayList<List<Integer>>();

        while (!_grown.isEmpty()) {
            List<Integer> maxCluster = _grown.remove(0);
//            if (!Double.isNaN(clusterMinP) && maxCluster.size() == 5) {
//                _grown.remove(maxCluster);
//                continue;
//            }
            if (!Double.isNaN(clusterMinP) && pValues.get(maxCluster) < clusterMinP) {
                grown.remove(maxCluster);
                continue;
            }
            out.add(maxCluster);

            // Remove from grown any cluster that intersects it.
            for (List<Integer> _cluster : new ArrayList<List<Integer>>(_grown)) {
                List<Integer> cluster2 = new ArrayList<Integer>(_cluster);
                cluster2.retainAll(maxCluster);

                if (!cluster2.isEmpty()) {
                    _grown.remove(_cluster);
                }
            }
        }

        NumberFormat nf = new DecimalFormat("0.0000");

//        // Print the output clusters.
//        System.out.println("Output clusters:");
//
//        for (List<Integer> l : out) {
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


    private List<Integer> pentad(int n1, int n2, int n3, int n4, int n5) {
        List<Integer> pentad = new ArrayList<Integer>();
        pentad.add(n1);
        pentad.add(n2);
        pentad.add(n3);
        pentad.add(n4);
        pentad.add(n5);

        return pentad;
    }

    private List<Integer> sextet(int n1, int n2, int n3, int n4, int n5, int n6) {
        List<Integer> sextet = new ArrayList<Integer>();
        sextet.add(n1);
        sextet.add(n2);
        sextet.add(n3);
        sextet.add(n4);
        sextet.add(n5);
        sextet.add(n6);

        if (new HashSet<Integer>(sextet).size() < 6)
            throw new IllegalArgumentException("Sextet elements must be unique: <" + n1 + ", " + n2 + ", " + n3 + ", " + n4 + ", " + n5 + ", " + n6 + ">");

        return sextet;
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


