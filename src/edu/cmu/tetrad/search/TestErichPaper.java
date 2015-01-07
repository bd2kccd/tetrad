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

import edu.cmu.tetrad.bayes.MeanInterpolator;
import edu.cmu.tetrad.cluster.ClusterUtils;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.DepthChoiceGenerator;
import edu.cmu.tetrad.util.ProbUtils;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.abs;


/**
 * Tests the IndTestTimeSeries class.
 *
 * @author Joseph Ramsey
 */
public class TestErichPaper extends TestCase {
    public TestErichPaper(String name) {
        super(name);
    }

    public void test1() {
        Graph mim = GraphUtils.randomSingleFactorModel(10, 10, 6, 0, 0, 0);

        Graph mimStructure = structure(mim);

        SemPm pm = new SemPm(mim);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(1000, false);

        FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.001);
        fofc.setSortKey(FindOneFactorClusters.SortKey.size);
//        fofc.setAlgorithm(FindOneFactorClusters.Algorithm.threeClustersFirst);
        fofc.setClusterMinP(Double.NaN);

        Graph searchGraph = fofc.search();
        Graph graph2 = reidentifyVariables(searchGraph, mim);
        Clusters clusters = MimUtils.convertToClusters(graph2, data.getVariables());
        List<List<Node>> partition = ClusterUtils.clustersToPartition(clusters, data.getVariables());
        List<String> latentVarList = new ArrayList<String>();

        for (int i = 0; i < clusters.getNumClusters(); i++) {
            latentVarList.add(clusters.getClusterName(i));
        }

        Mimbuild2 mimbuild = new Mimbuild2();
        Graph mimbuildStructure = mimbuild.search(partition, latentVarList, data);

        System.out.println(mimStructure);
        System.out.println(mimbuildStructure);

    }

    public void test1a() {
        RandomUtil.getInstance().setSeed(1001L);

        Graph mim = GraphUtils.randomBifactorModel(3, 0, 8, 0, 0, 0);

        Node l1 = mim.getNode("L1");
        Node l1b = mim.getNode("L1B");
        Node l2 = mim.getNode("L2");
        Node l2b = mim.getNode("L2B");
        Node l3 = mim.getNode("L3");
        Node l3b = mim.getNode("L3B");

        mim.addDirectedEdge(l2, l1);
        mim.addDirectedEdge(l2, l3);
        mim.addDirectedEdge(l2b, l1b);
        mim.addDirectedEdge(l2b, l3b);

        System.out.println(mim);

        SemPm pm = new SemPm(mim);
        SemIm im = new SemIm(pm);
        DataSet data = im.simulateData(5000, false);

        FindTwoFactorClusters fofc = new FindTwoFactorClusters(data, TestType.TETRAD_BOLLEN, 0.1);

        fofc.setSignificanceCalculated(true);
        fofc.search();
        List<List<Node>> clusters = fofc.getClusters();

        System.out.println(clusters);

        System.out.println("Model p = " + getModelP(clusters, data));
        System.out.println();
    }

    public void test1b() {
        try {
//            File dir = new File("/Users/josephramsey/Documents/proj/tetrad5/sample_data/");
//            File dir = new File("sample_data/");
//            File file = new File("sample_data/depressioncoping.dat");
            File file = new File("sample_data/dc.clean2.txt");

            double alpha = 0.995;

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            reader.setMaxIntegralDiscrete(0);
            DataSet data = reader.parseTabular(file);

            DataFilter interpolator = new MeanInterpolator();
            data = interpolator.filter(data);

            List<Node> dataVars = data.getVariables();
            List<Node> vars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("STR")) {
                    vars.add(node);
                }
            }

            DataSet subset = data.subsetColumns(vars);

            FindTwoFactorClusters ftfc = new FindTwoFactorClusters(new CovarianceMatrix(subset), TestType.TETRAD_BOLLEN, alpha);
            ftfc.setVerbose(false);
            ftfc.setSignificanceCalculated(true);
            ftfc.search();

            List<List<Node>> strClusters = ftfc.getClusters();

            vars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("DEP")) {
                    vars.add(node);
                }
            }

            subset = data.subsetColumns(vars);

            ftfc = new FindTwoFactorClusters(new CovarianceMatrix(subset), TestType.TETRAD_BOLLEN, alpha);
            ftfc.setVerbose(false);
            ftfc.setSignificanceCalculated(true);
            ftfc.search();

            List<List<Node>> depClusters = ftfc.getClusters();

            vars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("COP")) {
                    vars.add(node);
                }
            }

            subset = data.subsetColumns(vars);

            ftfc = new FindTwoFactorClusters(new CovarianceMatrix(subset), TestType.TETRAD_BOLLEN, alpha);
            ftfc.setVerbose(false);
            ftfc.setSignificanceCalculated(true);
            ftfc.search();

            List<List<Node>> copClusters = ftfc.getClusters();

            DepthChoiceGenerator gen1 = new DepthChoiceGenerator(strClusters.size(), -1);
            int[] choice1;

            while ((choice1 = gen1.next()) != null) {
                if (choice1.length == 0) continue;

                List<List<Node>> _strClusters = new ArrayList<List<Node>>();

                for (int choice : choice1) {
                    _strClusters.add(strClusters.get(choice));
                }


                DepthChoiceGenerator gen2 = new DepthChoiceGenerator(depClusters.size(), -1);
                int[] choice2;

                while ((choice2 = gen2.next()) != null) {
                    if (choice2.length == 0) continue;

                    List<List<Node>> _depClusters = new ArrayList<List<Node>>();

                    for (int choice : choice2) {
                        _depClusters.add(depClusters.get(choice));
                    }

                    DepthChoiceGenerator gen3 = new DepthChoiceGenerator(copClusters.size(), -1);
                    int[] choice3;

                    while ((choice3 = gen3.next()) != null) {
                        if (choice3.length == 0) continue;

                        List<List<Node>> _copClusters = new ArrayList<List<Node>>();

                        for (int choice : choice3) {
                            _copClusters.add(copClusters.get(choice));
                        }

                        List<List<Node>> allClusters = new ArrayList<List<Node>>();
                        allClusters.addAll(_strClusters);
                        allClusters.addAll(_depClusters);
                        allClusters.addAll(_copClusters);


                        List<Node> allClusteredVars = new ArrayList<Node>();
                        for (List<Node> cluster : allClusters) allClusteredVars.addAll(cluster);
                        DataSet allClusteredData = data.subsetColumns(allClusteredVars);

                        System.out.println("Model p = " + getModelP(allClusters, allClusteredData));
                        System.out.println();
                    }
                }

            }


//            for (int i = 0; i < strClusters.size(); i++) {
//                for (int j = 0; j < depClusters.size(); j++) {
//                    for (int k = 0; k < copClusters.size(); k++) {
//                        List<List<Node>> allClusters = new ArrayList<List<Node>>();
//                        allClusters.add(strClusters.get(i));
//                        allClusters.add(depClusters.get(j));
//                        allClusters.add(copClusters.get(k));
//
//                        List<Node> allClusteredVars = new ArrayList<Node>();
//                        for (List<Node> cluster : allClusters) allClusteredVars.addAll(cluster);
//                        DataSet allClusteredData = data.subsetColumns(allClusteredVars);
//
//                        System.out.println("Model p = " + getModelP(allClusters, allClusteredData));
//                        System.out.println();
//                    }
//                }
//            }

//            {
//                List<List<Node>> allClusters = new ArrayList<List<Node>>();
//                allClusters.addAll(strClusters);
//                allClusters.addAll(depClusters);
//                allClusters.addAll(copClusters);
//
//                List<Node> allClusteredVars = new ArrayList<Node>();
//                for (List<Node> cluster : allClusters) allClusteredVars.addAll(cluster);
//                DataSet allClusteredData = data.subsetColumns(allClusteredVars);
//
//                System.out.println("Model p = " + getModelP(allClusters, allClusteredData));
//                System.out.println();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test1c() {
        try {
//            File dir = new File("/Users/josephramsey/Documents/proj/tetrad5/sample_data/");
//            File dir = new File("sample_data/");
//            File file = new File("sample_data/depressioncoping.dat");
            File file = new File("sample_data/dc.clean2.txt");

            double alpha = 0.6;

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            reader.setMaxIntegralDiscrete(0);
            DataSet data = reader.parseTabular(file);

            DataFilter interpolator = new MeanInterpolator();
            data = interpolator.filter(data);

            List<Node> dataVars = data.getVariables();
            List<Node> strVars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("STR")) {
                    strVars.add(node);
                }
            }

            List<Node> depVars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("DEP")) {
                    depVars.add(node);
                }
            }

            List<Node> copVars = new ArrayList<Node>();

            for (Node node : dataVars) {
                if (node.getName().startsWith("COP")) {
                    copVars.add(node);
                }
            }

            Set<Set<Node>> groups = new HashSet<Set<Node>>();

            groups.add(new HashSet<Node>(strVars));
            groups.add(new HashSet<Node>(depVars));
            groups.add(new HashSet<Node>(copVars));

            FindTwoFactorClusters ftfc = new FindTwoFactorClusters(data, TestType.TETRAD_BOLLEN, alpha);
            ftfc.setVerbose(true);
            ftfc.setSignificanceCalculated(true);
            ftfc.search();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private double getModelP(List<List<Node>> clusters, DataSet dataSet) {
        List<Node> latents = new ArrayList<Node>();

        for (int i = 0; i < clusters.size(); i++) {
            GraphNode l = new GraphNode("L" + i);
            l.setNodeType(NodeType.LATENT);
            latents.add(l);
        }

        for (int i = 0; i < clusters.size(); i++) {
            GraphNode m = new GraphNode("M" + i);
            m.setNodeType(NodeType.LATENT);
            latents.add(m);
        }

        Graph graph = new EdgeListGraph(latents);

        for (List<Node> cluster : clusters) {
            for (Node node : cluster) {
                graph.addNode(node);
            }
        }

        for (int i = 0; i < latents.size(); i++) {
            for (int j = i + 1; j < latents.size(); j++) {
                if (i + clusters.size() == j) continue;
                graph.addDirectedEdge(latents.get(i), latents.get(j));
            }
        }

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = 0; j < clusters.get(i).size(); j++) {
                graph.addDirectedEdge(latents.get(i), clusters.get(i).get(j));
                graph.addDirectedEdge(latents.get(i + clusters.size()), clusters.get(i).get(j));
            }
        }

        try {
            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/kummerfeld.spirtes.yang.ramsey");
            File _file = new File(dir, "graph.txt");
            PrintWriter out = new PrintWriter(_file);
            out.println(graph);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SemPm pm = new SemPm(graph);

        for (Node x : pm.getGraph().getNodes()) {
            if (x.getNodeType() != NodeType.LATENT) {
                continue;
            }

            for (Node y : graph.getAdjacentNodes(x)) {
                if (y.getNodeType() != NodeType.MEASURED) {
                    continue;
                }

                Edge edge = graph.getEdge(x, y);

                if (!edge.pointsTowards(y)) {
                    continue;
                }

                Parameter p = pm.getParameter(x, y);

                if (p == null) throw new IllegalArgumentException();

                if (p.isFixed()) {
                    continue;
                }

                p.setFixed(true);
                p.setInitializedRandomly(false);
                p.setStartingValue(1.0);
                break;
            }
        }

        SemEstimator est = new SemEstimator(dataSet, pm, new SemOptimizerEm());

        SemIm im = est.estimate();
        double chisq = im.getChiSquare();

        System.out.println("Model chisq = " + chisq);

        int numMeasures = 0;
        for (Node node : graph.getNodes()) if (node.getNodeType() == NodeType.MEASURED) numMeasures++;

        int dof = (numMeasures * (numMeasures + 1)) / 2;
        dof -= graph.getNumNodes(); // Error covariances
        dof -= graph.getNumEdges(); // Coefficients.

        for (List<Node> cluster : clusters) {
            int c = cluster.size();
            int numConstraints = (c * (c + 1)) / 2;
            numConstraints -= harmanDof(c);
            System.out.println(cluster + " dof = " + harmanDof(c) + " numConstraints = " + numConstraints);
//            dof -= numConstraints;
        }

        System.out.println("Model DOF = " + dof);

        double q = ProbUtils.chisqCdf(chisq, dof);
        return 1.0 - q;
    }

    private int drtonDof(int c) {

        // From the table of codimensions from "Algebraic factor analysis: tetrads, pentads and beyond" Drton et al.
        // Table 1, for m = 2 factors.
//        if (cluster.size() == 5) {
//            dof = 1;
//        } else if (cluster.size() == 6) {
//            dof = 4;
//        } else if (cluster.size() == 7) {
//            dof = 8;
//        } else if (cluster.size() == 8) {
//            dof = 13;
//        } else if (cluster.size() == 9) {
//            dof = 19;
//        }

        // The formula from Drton.
        int dof = ((c - 2) * (c - 3)) / 2 - 2;
        if (dof < 0) dof = 0;

        return dof;
    }

    private int harmanDof(int c) {

        // From Harman.
        int dof = c * (c - 5) / 2 + 1;
        if (dof < 0) dof = 0;

        return dof;
    }

    public void test2() {
        Graph mim = GraphUtils.randomSingleFactorModel(10, 10, 6, 0, 0, 0);

        SemPm pm = new SemPm(mim);
        SemIm im = new SemIm(pm);

        for (int i = 0; i < 10; i++) {
            System.out.println("Round " + (i + 1) + " (resampling, randomly reordering variables)");

            DataSet data = im.simulateData(1000, false);
            data = reorderVariables(data);

            Clusters clusters = MimUtils.convertToClusters(mim, measuredVariables(mim));
            List<List<Node>> trueClusters = ClusterUtils.clustersToPartition(clusters, data.getVariables());

            FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.001);
            fofc.setSortKey(FindOneFactorClusters.SortKey.pValue);
//            fofc.setAlgorithm(FindOneFactorClusters.Algorithm.threeClustersFirst);
//            BuildPureClusters fofc = new BuildPureClusters(data, 0.001,
//                    TestType.TETRAD_WISHART, TestType.GAUSSIAN_PVALUE);

            Graph graph = fofc.search();

            graph = reidentifyVariables(graph, mim);

            Clusters clusters2 = MimUtils.convertToClusters(graph, data.getVariables());
            List<List<Node>> foundClusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

            NumberFormat nf = new DecimalFormat("0.0000");

            System.out.println("Measure 1 = " + nf.format(foundClusters.size() / (double) trueClusters.size()));

            double sum2 = 0.;
            int count2 = 0;

            for (List<Node> cluster : foundClusters) {
                List<Node> c = largestPureSubcluster(cluster, trueClusters);
                double d = c.size() / (double) cluster.size();
                sum2 += d;
                count2++;
            }

            System.out.println("Measure 2 = " + nf.format(sum2 / count2));

            double sum3 = 0.;
            int count3 = 0;

            for (List<Node> cluster : foundClusters) {
                List<Node> c = largestPureSubcluster2(cluster, trueClusters);
                double d = cluster.size() / (double) c.size();
                sum3 += d;
                count3++;
            }

            System.out.println("Measure 3 = " + nf.format(sum3 / count3));
        }
    }

    public void test3() {
        try {
            int numTrials = 100;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");
//
//            String path = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/sub014/roidata/roiextract_extpower_radius5.txt";
//            File file = new File(path);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            data.removeColumn(data.getColumn(data.getVariable("DEP14")));
            data.removeColumn(data.getColumn(data.getVariable("COP6")));
            data.removeColumn(data.getColumn(data.getVariable("STR11")));

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

            for (int i = 0; i < numTrials; i++) {
                data = reorderVariables(data);

//                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_DELTA, .0001);
//                fofc.setSortKey(FindOneFactorClusters.SortKey.pValue);
//                fofc.setClusterMinP(0.00);
//                fofc.setIncludeThreeClusters(true);
//
                BuildPureClusters fofc = new BuildPureClusters(data, 0.001,
                        TestType.TETRAD_WISHART, TestType.GAUSSIAN_PVALUE);

                Graph fofcGraph = fofc.search();

                Clusters clusters2 = MimUtils.convertToClusters(fofcGraph, data.getVariables());
                List<List<Node>> clusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

                System.out.println(clusters);

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (getType(c).equals("MIXED")) clusters.remove(c);
//                }

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (c.size() < 4) clusters.remove(c);
//                }

//                clusters = combineTypes(clusters);

                Mimbuild2 mimbuild = new Mimbuild2();
                mimbuild.setAlpha(0.1);

                Graph latentGraph = mimbuild.search(clusters, data);

                System.out.println(latentGraph);

                Graph fullMimGraph = mimbuild.getFullGraph();

                Clusters clusters3 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                List<List<Node>> foundClusters2 = ClusterUtils.clustersToPartition(clusters3, data.getVariables());

                Map<Node, List<Node>> clustersForLatents = new HashMap<Node, List<Node>>();

                for (List<Node> cluster : foundClusters2) {
                    Node latent = getLatent(fullMimGraph, cluster);
                    clustersForLatents.put(latent, cluster);
                }

                if (numClusteredVars(clusters) > 1) {
                    printClusterPs(data, foundClusters2);

                    Node str = new GraphNode("STR");
                    Node dep = new GraphNode("DEP");
                    Node cop = new GraphNode("COP");
                    Node mix = new GraphNode("MIXED");

                    Graph graph = new EdgeListGraph();
                    graph.addNode(str);
                    graph.addNode(dep);
                    graph.addNode(cop);
                    graph.addNode(mix);

//                System.out.println("Latent graph");
//                System.out.println(latentGraph);

                    Set<Edge> thisGraphEdges = new HashSet<Edge>();

                    for (Edge edge : latentGraph.getEdges()) {
                        Node latent1 = edge.getNode1();
                        List<Node> cluster1 = clustersForLatents.get(latent1);
                        String type1 = getType(cluster1);
                        Node node1 = graph.getNode(type1);

                        Node latent2 = edge.getNode2();
                        List<Node> cluster2 = clustersForLatents.get(latent2);
                        String type2 = getType(cluster2);
                        Node node2 = graph.getNode(type2);

                        if (node1 == node2) continue;

                        if (node1 == null || node2 == null) {
                            throw new NullPointerException();
                        }

                        if (type1.equals("MIXED") || type2.equals("MIXED")) {
                            System.out.println(cluster1 + "o=o" + cluster2);
                        }

//                    Edge _edge = new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
                        Edge _edge = Edges.undirectedEdge(node1, node2);

//                    System.out.println("Metaedge " + _edge);

                        if (!thisGraphEdges.contains(_edge)) {
                            increment(edges, _edge);
                            thisGraphEdges.add(_edge);
                        }
                    }

                    Clusters clusters4 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                    List<List<Node>> foundClusters4 = ClusterUtils.clustersToPartition(clusters4, data.getVariables());

                    if (false) {
                        fullMimGraph = createFullyConnectedTwoFactorModel(latentGraph, fullMimGraph, foundClusters4);
                    }

                    if (true) {
                        fullMimGraph = createFullyConnectedOneFactorModel(latentGraph, fullMimGraph);
                    }

                    double p = calcModelP(data, fullMimGraph);
                    System.out.println("Model P = " + p);
//
                    System.out.println("# clustered variables = " + numClusteredVars(clusters));

                    System.out.println("\nCounts for meta-edges out of " + (i + 1) + ":");

                    for (Edge edge : edges.keySet()) {
                        if (edges.get(edge) > minCount) {
                            System.out.println(edge + " count = " + edges.get(edge));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test3a() {
        try {
            int numTrials = 100;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");
//
//            String path = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/sub014/roidata/roiextract_extpower_radius5.txt";
//            File file = new File(path);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            data.removeColumn(data.getColumn(data.getVariable("DEP14")));
            data.removeColumn(data.getColumn(data.getVariable("COP6")));
            data.removeColumn(data.getColumn(data.getVariable("STR11")));

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

            for (int i = 0; i < numTrials; i++) {
                data = reorderVariables(data);

                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, .001);
                fofc.setSortKey(FindOneFactorClusters.SortKey.pValue);
//                fofc.setClusterMinP(Double.NaN);

                Graph fofcGraph = fofc.search();

                List<List<Node>> _clusters = fofc.getClusters();

                Clusters clusters2 = MimUtils.convertToClusters(fofcGraph, data.getVariables());
                List<List<Node>> clusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

                System.out.println(clusters);

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (getType(c).equals("MIXED")) clusters.remove(c);
//                }

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (c.size() < 4) clusters.remove(c);
//                }

//                clusters = combineTypes(clusters);

                Mimbuild2 mimbuild = new Mimbuild2();
                mimbuild.setAlpha(0.1);

                Graph latentGraph = mimbuild.search(clusters, data);

                System.out.println(latentGraph);

                Graph fullMimGraph = mimbuild.getFullGraph();

                Clusters clusters3 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                List<List<Node>> foundClusters2 = ClusterUtils.clustersToPartition(clusters3, data.getVariables());

                Map<Node, List<Node>> clustersForLatents = new HashMap<Node, List<Node>>();

                for (List<Node> cluster : foundClusters2) {
                    Node latent = getLatent(fullMimGraph, cluster);
                    clustersForLatents.put(latent, cluster);
                }

                if (numClusteredVars(clusters) > 1) {
                    printClusterPs(data, foundClusters2);

                    Node str = new GraphNode("STR");
                    Node dep = new GraphNode("DEP");
                    Node cop = new GraphNode("COP");
                    Node mix = new GraphNode("MIXED");

                    Graph graph = new EdgeListGraph();
                    graph.addNode(str);
                    graph.addNode(dep);
                    graph.addNode(cop);
                    graph.addNode(mix);

//                System.out.println("Latent graph");
//                System.out.println(latentGraph);

                    Set<Edge> thisGraphEdges = new HashSet<Edge>();

                    for (Edge edge : latentGraph.getEdges()) {
                        Node latent1 = edge.getNode1();
                        List<Node> cluster1 = clustersForLatents.get(latent1);
                        String type1 = getType(cluster1);
                        Node node1 = graph.getNode(type1);

                        Node latent2 = edge.getNode2();
                        List<Node> cluster2 = clustersForLatents.get(latent2);
                        String type2 = getType(cluster2);
                        Node node2 = graph.getNode(type2);

                        if (node1 == node2) continue;

                        if (node1 == null || node2 == null) {
                            throw new NullPointerException();
                        }

                        if (type1.equals("MIXED") || type2.equals("MIXED")) {
                            System.out.println(cluster1 + "o=o" + cluster2);
                        }

//                    Edge _edge = new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
                        Edge _edge = Edges.undirectedEdge(node1, node2);

//                    System.out.println("Metaedge " + _edge);

                        if (!thisGraphEdges.contains(_edge)) {
                            increment(edges, _edge);
                            thisGraphEdges.add(_edge);
                        }
                    }

                    Clusters clusters4 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                    List<List<Node>> foundClusters4 = ClusterUtils.clustersToPartition(clusters4, data.getVariables());

//                    if (false) {
//                        fullMimGraph = createFullyConnectedTwoFactorModel(latentGraph, fullMimGraph, foundClusters4);
//                    }
//
//                    if (true) {
//                        fullMimGraph = createFullyConnectedOneFactorModel(latentGraph, fullMimGraph);
//                    }

                    Graph _graph = oneFactorModel(_clusters);
                    double p = calcModelP(data, _graph);

//                    double p = calcModelP(data, fullMimGraph);
                    System.out.println("Model P = " + p);
//
                    System.out.println("# clustered variables = " + numClusteredVars(clusters));

                    System.out.println("\nCounts for meta-edges out of " + (i + 1) + ":");

                    for (Edge edge : edges.keySet()) {
                        if (edges.get(edge) > minCount) {
                            System.out.println(edge + " count = " + edges.get(edge));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test3c() {
        try {
            int numTrials = 1;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");
//
//            String path = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/sub014/roidata/roiextract_extpower_radius5.txt";
//            File file = new File(path);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            data.removeColumn(data.getColumn(data.getVariable("DEP14")));
            data.removeColumn(data.getColumn(data.getVariable("COP6")));
            data.removeColumn(data.getColumn(data.getVariable("STR11")));

            for (int i = 0; i < numTrials; i++) {
                data = reorderVariables(data);

                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, .01);
                fofc.setSortKey(FindOneFactorClusters.SortKey.pValue);
                fofc.setClusterMinP(0.0);
//                fofc.setIncludeThreeClusters(true);

                fofc.search();
                List<List<Node>> clusters = fofc.getClusters();

                printClusterPs(data, clusters);

                DepthChoiceGenerator gen = new DepthChoiceGenerator(clusters.size(), clusters.size());
                int[] choice;

                Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

                Node str = new GraphNode("STR");
                Node dep = new GraphNode("DEP");
                Node cop = new GraphNode("COP");
                Node mix = new GraphNode("MIXED");

                Graph graph = new EdgeListGraph();
                graph.addNode(str);
                graph.addNode(dep);
                graph.addNode(cop);
                graph.addNode(mix);

                while ((choice = gen.next()) != null) {
//                    if (choice.length == 0) continue;
                    if (choice.length < 3) continue;

                    List<List<Node>> subClustering = new ArrayList<List<Node>>();

                    for (int k = 0; k < choice.length; k++) {
                        subClustering.add(clusters.get(choice[k]));
                    }

                    Graph model = oneFactorModel(subClustering);

                    double p = calcModelP(data, model);

//                    if (p < 0.2) continue;

                    System.out.println("\n================NEW SUBMODEL===================");

                    printClusterPs(data, subClustering);

                    System.out.println("Model P = " + p);
//
                    System.out.println("# clustered variables = " + numClusteredVars(subClustering));

                    Mimbuild2 mimbuild = new Mimbuild2();
                    mimbuild.setAlpha(0.1);

                    Graph latentGraph = mimbuild.search(subClustering, data);

                    System.out.println(latentGraph);

                    Graph fullMimGraph = mimbuild.getFullGraph();

                    Clusters clusters3 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                    List<List<Node>> foundClusters2 = ClusterUtils.clustersToPartition(clusters3, data.getVariables());


                    Map<Node, List<Node>> clustersForLatents = new HashMap<Node, List<Node>>();

                    for (List<Node> cluster : subClustering) {
                        Node latent = getLatent(fullMimGraph, cluster);
                        clustersForLatents.put(latent, cluster);
                    }


//                System.out.println("Latent graph");
//                System.out.println(latentGraph);

                    System.out.println("META-GRAPH");

                    Set<Edge> thisGraphEdges = new HashSet<Edge>();
                    Graph graph1 = new EdgeListGraph();
                    graph1.addNode(str);
                    graph1.addNode(dep);
                    graph1.addNode(cop);
                    graph1.addNode(mix);

                    for (Edge edge : latentGraph.getEdges()) {
                        Node latent1 = edge.getNode1();
                        List<Node> cluster1 = clustersForLatents.get(latent1);
                        String type1 = getType(cluster1);
                        Node node1 = graph.getNode(type1);

                        Node latent2 = edge.getNode2();
                        List<Node> cluster2 = clustersForLatents.get(latent2);
                        String type2 = getType(cluster2);
                        Node node2 = graph.getNode(type2);

                        if (node1 == node2) continue;

                        if (node1 == null || node2 == null) {
                            throw new NullPointerException();
                        }

                        if (type1.equals("MIXED") || type2.equals("MIXED")) {
                            System.out.println(cluster1 + "o=o" + cluster2);
                        }

//                    Edge _edge = new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
                        Edge _edge = Edges.undirectedEdge(node1, node2);

                        if (!thisGraphEdges.contains(_edge)) {
                            increment(edges, _edge);
                            thisGraphEdges.add(_edge);
                            graph1.addEdge(_edge);
                        }
                    }

                    System.out.println(graph1);

                    System.out.println("\nRunning count for meta-edges:");

                    for (Edge edge : edges.keySet()) {
                        if (edges.get(edge) > minCount) {
                            System.out.println(edge + " count = " + edges.get(edge));
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test3d() {
        try {
            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");
//
//            String path = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/sub014/roidata/roiextract_extpower_radius5.txt";
//            File file = new File(path);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            Graph graph = new EdgeListGraph(data.getVariables());

            List<List<Node>> clustering = new ArrayList<List<Node>>();

            List<Node> str = new ArrayList<Node>();
            str.add(graph.getNode("STR3"));
            str.add(graph.getNode("STR4"));
            str.add(graph.getNode("STR16"));
            str.add(graph.getNode("STR18"));
            str.add(graph.getNode("STR20"));

            List<Node> dep = new ArrayList<Node>();
            dep.add(graph.getNode("DEP9"));
            dep.add(graph.getNode("DEP13"));
            dep.add(graph.getNode("DEP19"));


            List<Node> cop = new ArrayList<Node>();
            cop.add(graph.getNode("COP9"));
            cop.add(graph.getNode("COP12"));
            cop.add(graph.getNode("COP14"));
            cop.add(graph.getNode("COP15"));

            System.out.println(data.getVariables());

            clustering.add(str);
            clustering.add(dep);
            clustering.add(cop);

            printClusterPs(data, clustering);

            Graph model = oneFactorModel(clustering);
            double p = calcModelP(data, model);
            System.out.println("Model P = " + p);

            Mimbuild2 mimbuild = new Mimbuild2();
            mimbuild.setAlpha(0.1);

            Graph latentGraph = mimbuild.search(clustering, data);

            System.out.println(latentGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void test3e() {
        try {
            int numTrials = 100;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");
//
//            String path = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/roidata/sub014/roidata/roiextract_extpower_radius5.txt";
//            File file = new File(path);

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            data.removeColumn(data.getColumn(data.getVariable("DEP14")));
            data.removeColumn(data.getColumn(data.getVariable("COP6")));
            data.removeColumn(data.getColumn(data.getVariable("STR11")));

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

            for (double d : new double[]{0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001, 0.00005, 0.00001, 0.000001, 0.000001}) {
                System.out.println("d = " + d);

                data = reorderVariables(data);

                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_BOLLEN, d);
                fofc.setSortKey(FindOneFactorClusters.SortKey.pValue);
                fofc.setClusterMinP(0.00);
//                fofc.setIncludeThreeClusters(true);

                Graph fofcGraph = fofc.search();

                Clusters clusters2 = MimUtils.convertToClusters(fofcGraph, data.getVariables());
                List<List<Node>> clusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

                System.out.println(clusters);

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (getType(c).equals("MIXED")) clusters.remove(c);
//                }

//                for (List<Node> c : new ArrayList<List<Node>>(clusters)) {
//                    if (c.size() < 4) clusters.remove(c);
//                }

//                clusters = combineTypes(clusters);

                Mimbuild2 mimbuild = new Mimbuild2();
                mimbuild.setAlpha(0.1);

                Graph latentGraph = mimbuild.search(clusters, data);

                System.out.println(latentGraph);

                Graph fullMimGraph = mimbuild.getFullGraph();

                Clusters clusters3 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                List<List<Node>> foundClusters2 = ClusterUtils.clustersToPartition(clusters3, data.getVariables());

                Map<Node, List<Node>> clustersForLatents = new HashMap<Node, List<Node>>();

                for (List<Node> cluster : foundClusters2) {
                    Node latent = getLatent(fullMimGraph, cluster);
                    clustersForLatents.put(latent, cluster);
                }

                if (numClusteredVars(clusters) > 1) {
                    printClusterPs(data, foundClusters2);

                    Node str = new GraphNode("STR");
                    Node dep = new GraphNode("DEP");
                    Node cop = new GraphNode("COP");
                    Node mix = new GraphNode("MIXED");

                    Graph graph = new EdgeListGraph();
                    graph.addNode(str);
                    graph.addNode(dep);
                    graph.addNode(cop);
                    graph.addNode(mix);

//                System.out.println("Latent graph");
//                System.out.println(latentGraph);

                    Set<Edge> thisGraphEdges = new HashSet<Edge>();

                    for (Edge edge : latentGraph.getEdges()) {
                        Node latent1 = edge.getNode1();
                        List<Node> cluster1 = clustersForLatents.get(latent1);
                        String type1 = getType(cluster1);
                        Node node1 = graph.getNode(type1);

                        Node latent2 = edge.getNode2();
                        List<Node> cluster2 = clustersForLatents.get(latent2);
                        String type2 = getType(cluster2);
                        Node node2 = graph.getNode(type2);

                        if (node1 == node2) continue;

                        if (node1 == null || node2 == null) {
                            throw new NullPointerException();
                        }

                        if (type1.equals("MIXED") || type2.equals("MIXED")) {
                            System.out.println(cluster1 + "o=o" + cluster2);
                        }

//                    Edge _edge = new Edge(node1, node2, edge.getEndpoint1(), edge.getEndpoint2());
                        Edge _edge = Edges.undirectedEdge(node1, node2);

//                    System.out.println("Metaedge " + _edge);

                        if (!thisGraphEdges.contains(_edge)) {
                            increment(edges, _edge);
                            thisGraphEdges.add(_edge);
                        }
                    }

                    Clusters clusters4 = MimUtils.convertToClusters(fullMimGraph, data.getVariables());
                    List<List<Node>> foundClusters4 = ClusterUtils.clustersToPartition(clusters4, data.getVariables());

                    if (false) {
                        fullMimGraph = createFullyConnectedTwoFactorModel(latentGraph, fullMimGraph, foundClusters4);
                    }

                    if (true) {
                        fullMimGraph = createFullyConnectedOneFactorModel(latentGraph, fullMimGraph);
                    }

                    double p = calcModelP(data, fullMimGraph);
                    System.out.println("Model P = " + p);
//
                    System.out.println("# clustered variables = " + numClusteredVars(clusters));

                    System.out.println("\nCounts for meta-edges out of d = " + d + ":");

                    for (Edge edge : edges.keySet()) {
                        if (edges.get(edge) > minCount) {
                            System.out.println(edge + " count = " + edges.get(edge));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Graph oneFactorModel(List<List<Node>> clusters) {
        Graph g = new EdgeListGraph();
        int count = 0;
        List<Node> latents = new ArrayList<Node>();

        for (List<Node> cluster : clusters) {
            for (Node d : cluster) g.addNode(d);
            Node l = new GraphNode("L" + (++count));
            l.setNodeType(NodeType.LATENT);
            latents.add(l);
            g.addNode(l);

            for (Node n : cluster) {
                g.addDirectedEdge(l, n);
            }
        }

        for (int _i = 0; _i < latents.size(); _i++) {
            for (int _j = _i + 1; _j < latents.size(); _j++) {
                g.addDirectedEdge(latents.get(_i), latents.get(_j));
            }
        }

        return g;
    }

    private List<List<Node>> combineTypes(List<List<Node>> clusters) {
        List<Node> _dep = new ArrayList<Node>();
        List<Node> _str = new ArrayList<Node>();
        List<Node> _cop = new ArrayList<Node>();
        List<Node> _mix = new ArrayList<Node>();

        for (List<Node> c : clusters) {
            if (getType(c).equals("DEP")) _dep.addAll(c);
            if (getType(c).equals("STR")) _str.addAll(c);
            if (getType(c).equals("COP")) _cop.addAll(c);
            if (getType(c).equals("MIXED")) _mix.addAll(c);
        }

        clusters = new ArrayList<List<Node>>();
        clusters.add(_dep);
        clusters.add(_str);
        clusters.add(_cop);
        clusters.add(_mix);
        return clusters;
    }

    public void test4() {
        try {
            int numTrials = 100;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

            for (int i = 0; i < 1; i++) {
                data = reorderVariables(data);

                FindTwoFactorClusters fofc = new FindTwoFactorClusters(data, TestType.TETRAD_BOLLEN, .05);

                Graph fofcGraph = fofc.search();

                Clusters clusters2 = MimUtils.convertToClusters(fofcGraph, data.getVariables());
                List<List<Node>> clusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

                System.out.println(clusters);

//                List<Node> c1 = new ArrayList<Node>();
//                c1.add(data.getVariable("DEP14"));
//
//                List<Node> c2 = new ArrayList<Node>();
//                c2.add(data.getVariable("COP6"));
//
//                List<List<Node>> clusters = new ArrayList<List<Node>>();
//                clusters.add(c1);
//                clusters.add(c2);

                Graph fullGraph = getFullGraph(clusters);
                List<Node> latents = getLatents(fullGraph);
                Graph latentGraph = new EdgeListGraph(latents);
//
                if (true) {
                    fullGraph = createFullyConnectedTwoFactorModel(latentGraph, fullGraph, clusters);
                }

                if (false) {
                    fullGraph = createFullyConnectedOneFactorModel(latentGraph, fullGraph);
                }

                double p = calcModelP(data, fullGraph);

                System.out.println("Model p = " + p);
//                System.out.println("# clustered variables = " + numClusteredVars(clusters));
//
//                System.out.println("\nCounts for meta-edges out of " + (i + 1) + ":");
//
//                for (Edge edge : edges.keySet()) {
//                    if (edges.get(edge) > minCount) {
//                        System.out.println(edge + " count = " + edges.get(edge));
//                    }
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test4a() {
        try {
            int numTrials = 100;
            int minCount = 0; //numTrials / 4;

            File dir = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2013.11.23/erich");
            File file = new File(dir, "dc.clean.txt");

            DataReader reader = new DataReader();
            reader.setVariablesSupplied(true);
            DataSet data = reader.parseTabular(file);

            Map<Edge, Integer> edges = new HashMap<Edge, Integer>();

            for (int i = 0; i < 1; i++) {
                data = reorderVariables(data);

                FindTwoFactorClusters4 fofc = new FindTwoFactorClusters4(data, .05);
//
                Graph fofcGraph = fofc.search();
//
//                Clusters clusters2 = MimUtils.convertToClusters(fofcGraph, data.getVariables());
//                List<List<Node>> clusters = ClusterUtils.clustersToPartition(clusters2, data.getVariables());

                System.out.println(fofc.getClusters());

//                List<Node> c1 = new ArrayList<Node>();
//                c1.add(data.getVariable("DEP14"));
//
//                List<Node> c2 = new ArrayList<Node>();
//                c2.add(data.getVariable("COP6"));
//
//                List<List<Node>> clusters = new ArrayList<List<Node>>();
//                clusters.add(c1);
//                clusters.add(c2);

//                Graph fullGraph = getFullGraph(clusters);
//                List<Node> latents = getLatents(fullGraph);
//                Graph latentGraph = new EdgeListGraph(latents);
////
//                if (true) {
//                    fullGraph = createFullyConnectedTwoFactorModel(latentGraph, fullGraph, clusters);
//                }
//
//                if (false) {
//                    fullGraph = createFullyConnectedOneFactorModel(latentGraph, fullGraph);
//                }

//                double p = calcModelP(data, fullGraph);
//
//                System.out.println("Model p = " + p);
//                System.out.println("# clustered variables = " + numClusteredVars(clusters));
//
//                System.out.println("\nCounts for meta-edges out of " + (i + 1) + ":");
//
//                for (Edge edge : edges.keySet()) {
//                    if (edges.get(edge) > minCount) {
//                        System.out.println(edge + " count = " + edges.get(edge));
//                    }
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test6() {
        List<List<List<List<String>>>> clustering = getClusteringFa();
        System.out.println(clustering.size());

        for (List<List<List<String>>> fileClustering : clustering) {
            System.out.println(fileClustering.size());

            for (List<List<String>> clusters : fileClustering) {

                for (List<String> cluster : clusters) {
//                    System.out.println(cluster);
                }

//                System.out.println();
            }
        }

        System.out.println(clustering.size());
    }

    public List<List<List<List<String>>>> getClusteringFofc() {
        List<List<List<List<String>>>> allClusterings = null;
        int count = -1;

        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation";
//            String dir = "/Users/josephramsey/Documents/logging";

            String[] names = new String[]{
                    "output1.txt",
                    "output2.txt",
                    "output3.txt",
                    "output4.txt",
                    "output5.txt",
                    "output6.txt",
                    "output7.txt",
                    "output8.txt",
                    "output9.txt",
                    "output10.txt",
                    "output11.txt",
                    "output12.txt",
            };

            allClusterings = new ArrayList<List<List<List<String>>>>();

            for (String name : names) {
                File file = new File(dir, name);

                allClusterings.add(new ArrayList<List<List<String>>>());
                count++;

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                boolean found = false;

                List<List<String>> clusters = null;

                while ((line = reader.readLine()) != null) {
                    if (!found && line.startsWith("FOFC")) {

                        // Skip 2 lines for BPC, 6 for FOFC
                        line = reader.readLine();
                        line = reader.readLine();
                        line = reader.readLine();
                        line = reader.readLine();
                        line = reader.readLine();
                        line = reader.readLine();
                        found = true;
                        clusters = new ArrayList<List<String>>();
                        continue;
                    }

                    if (found && line.trim().isEmpty()) {
                        found = false;

                        if (clusters.isEmpty()) {
                            System.out.println();
                        }

                        allClusterings.get(count).add(clusters);
                        continue;
                    }

                    if (found) {
                        String[] tokens = line.split(" ");
                        List<String> vars = new ArrayList<String>();
                        for (int i = 1; i < tokens.length; i++) vars.add(tokens[i]);
                        clusters.add(vars);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allClusterings;
    }

    public List<List<List<List<String>>>> getClusteringBpc() {
        List<List<List<List<String>>>> allClusterings = null;
        int count = -1;

        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation";
//            String dir = "/Users/josephramsey/Documents/logging";

            String[] names = new String[]{
                    "output1.txt",
                    "output2.txt",
                    "output3.txt",
                    "output4.txt",
                    "output5.txt",
                    "output6.txt",
                    "output7.txt",
                    "output8.txt",
                    "output9.txt",
                    "output10.txt",
                    "output11.txt",
                    "output12.txt",
            };

            allClusterings = new ArrayList<List<List<List<String>>>>();

            for (String name : names) {
                File file = new File(dir, name);

                allClusterings.add(new ArrayList<List<List<String>>>());
                count++;

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                boolean found = false;

                List<List<String>> clusters = null;

                while ((line = reader.readLine()) != null) {
                    if (!found && line.startsWith("BPC")) {

                        // Skip 2 lines for BPC, 6 for FOFC
                        line = reader.readLine();
                        line = reader.readLine();
//                        line = reader.readLine();
//                        line = reader.readLine();
//                        line = reader.readLine();
//                        line = reader.readLine();
                        found = true;
                        clusters = new ArrayList<List<String>>();
                        continue;
                    }

                    if (found && line.trim().isEmpty()) {
                        found = false;

                        if (clusters.isEmpty()) {
                            System.out.println();
                        }

                        allClusterings.get(count).add(clusters);
                        continue;
                    }

                    if (found) {
                        String[] tokens = line.split(" ");
                        List<String> vars = new ArrayList<String>();
                        for (int i = 1; i < tokens.length; i++) vars.add(tokens[i]);
                        clusters.add(vars);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allClusterings;
    }

    public List<List<List<List<String>>>> getClusteringFa() {
        List<List<List<List<String>>>> allClusterings = null;
        String specline = "";

        try {
            String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation";
            String name = "matlaboutput4.txt";

            File file = new File(dir, name);
            allClusterings = new ArrayList<List<List<List<String>>>>();
            List<List<List<String>>> clustering = new ArrayList<List<List<String>>>();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            List<List<String>> clusters = null;
            List<String> cluster = new ArrayList<String>();
            clusters = new ArrayList<List<String>>();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (line.trim().startsWith("Column")) continue;
//                if (line.trim().startsWith("name")) continue;
                if (line.contains("Warning")) continue;
                if (line.contains("In")) continue;
                if (line.contains("Empty")) continue;
                if (line.trim().startsWith("Elapsed")) continue;

                if (line.trim().startsWith("\'X")) {
                    cluster.addAll(getStrings(line));
                    continue;
                }
                if (line.trim().startsWith("ans") || line.trim().startsWith("name")) {
                    if (!cluster.isEmpty()) {
                        clusters.add(cluster);
                    }
                    cluster = new ArrayList<String>();
                    continue;
                }
                if (line.contains("separate.datasets")) {
                    String s = line.trim().substring(0, 20);

                    clustering.add(clusters);
                    clusters = new ArrayList<List<String>>();

                    if (!s.equals(specline)) {
                        if (!clustering.get(0).isEmpty()) {
                            allClusterings.add(clustering);
                        }

                        clustering = new ArrayList<List<List<String>>>();
                        specline = s;
                    }

                    continue;
                }
            }

            if (!clustering.isEmpty()) {
                allClusterings.add(clustering);
            }
            clustering = new ArrayList<List<List<String>>>();

            clusters = new ArrayList<List<String>>();

            if (!cluster.isEmpty()) {
                clusters.add(cluster);
                clustering.add(clusters);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allClusterings;
    }

    // Run Mimbuild on clusters.
    public void test7() {

        try {

            List<List<List<List<String>>>> allFileClusters = null;
            String type = "FA";

            System.out.println(type);

            if ("FOFC".equals(type)) {
                allFileClusters = getClusteringFofc();
            } else if ("BPC".equals(type)) {
                allFileClusters = getClusteringBpc();
            } else if ("FA".equals(type)) {
                allFileClusters = getClusteringFa();
            }

            // Load data.

            for (int i = 1; i <= 12; i++) {
                List<List<List<String>>> fileClustering = allFileClusters.get(i - 1);

                List<Integer> errors = new ArrayList<Integer>();


                for (int j = 1; j <= 50; j++) {
                    List<List<String>> clustering = fileClustering.get(j - 1);

                    String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/separate.datasets/";
                    String subdir = dir + i;
                    String name = j + ".txt";

                    DataReader reader = new DataReader();
                    reader.setVariablesSupplied(true);

                    List<List<Node>> _clustering = new ArrayList<List<Node>>();

                    File file1 = new File(subdir, name);

//                    System.out.println(file1);

                    DataSet data = reader.parseTabular(file1);

                    Graph searchGraph = new EdgeListGraph();
                    int count = 1;

                    for (List<String> cluster : clustering) {
                        Node latent = new GraphNode("L" + count++);
                        searchGraph.addNode(latent);

                        List<Node> _cluster = new ArrayList<Node>();

                        for (String s : cluster) {
                            Node variable = data.getVariable(s);
                            _cluster.add(variable);
                            searchGraph.addNode(variable);
                            searchGraph.addDirectedEdge(latent, variable);
                        }

                        _clustering.add(_cluster);
                    }

                    String graphType;

                    switch (i) {
                        case 1:
                        case 2:
                        case 3:
                            graphType = "pure.txt";
                            break;
                        case 4:
                        case 5:
                        case 6:
                            graphType = "impure.txt";
                            break;
                        case 7:
                        case 8:
                        case 9:
                            graphType = "impurecyclic.txt";
                            break;
                        case 10:
                        case 11:
                        case 12:
                            graphType = "impure1.txt";
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    File file = new File(dir + "..", graphType);
                    if (!file.exists()) {
                        System.out.println();
                    }

                    Graph mim = GraphUtils.loadGraphTxt(file);

                    for (Node node : mim.getNodes()) {
                        if (node.getName().startsWith("L")) node.setNodeType(NodeType.LATENT);
                    }

                    List<String> _latentNames = reidentifyVariables2(_clustering, mim, data);
//                    Mimbuild3 mimbuild2 = new Mimbuild3();
//                    mimbuild2.setReps(500);
                    Mimbuild2 mimbuild2 = new Mimbuild2();
                    Graph mimbuildGraph = mimbuild2.search(_clustering, _latentNames, data);

                    mimbuildGraph = GraphUtils.replaceNodes(mimbuildGraph, mim.getNodes());

                    int error = structuralHammingDistanceBetweenLatents(mim, mimbuildGraph);

//                    System.out.println(error);
                    errors.add(error);
                }

                System.out.println("\nSimulation " + i);

                double[] _errors = new double[errors.size()];
                for (int h = 0; h < errors.size(); h++) _errors[h] = errors.get(h);

                System.out.println("Average error = " + StatUtils.mean(_errors));
                System.out.println("SE error = " + StatUtils.sd(_errors));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int structuralHammingDistanceBetweenLatents(Graph mim, Graph mimbuildGraph) {
        int error = 0;

        Set<Node> _allNodes = new HashSet<Node>();

        List<Node> mimLatents = getLatents(mim);
        List<Node> graphLatents = getLatents(mimbuildGraph);

        Graph u = mim.subgraph(mimLatents);

        Graph G = new Pc(new IndTestDSep(u, true)).search();
        Graph H = mimbuildGraph.subgraph(graphLatents);

        _allNodes.addAll(mimLatents);
        _allNodes.addAll(graphLatents);

        List<Node> allNodes = new ArrayList<Node>(_allNodes);

        for (int i1 = 0; i1 < allNodes.size(); i1++) {
            for (int i2 = i1 + 1; i2 < allNodes.size(); i2++) {
                Node l1 = allNodes.get(i1);
                Node l2 = allNodes.get(i2);

                Edge e1 = null;
                try {
                    e1 = G.getEdge(l1, l2);
                } catch (Exception e) {
//                                e.printStackTrace();
                }
                Edge e2 = null;
                try {
                    e2 = H.getEdge(l1, l2);
                } catch (Exception e) {
//                                e.printStackTrace();
                }

                int shd = structuralHammingDistance(e1, e2);
//                            System.out.println("SHD " + e1 + " " + e2 + " " + shd);
                error += shd;
            }
        }
        return error;
    }

    private int structuralHammingDistance(Edge e1, Edge e2) {
        if (e1 == null && e2 != null && Edges.isUndirectedEdge(e2)) {
            return 1;
        }
        else if (e1 == null && e2 != null && Edges.isDirectedEdge(e2)) {
            return 2;
        }
        else if (e1 != null && Edges.isUndirectedEdge(e1) && e2 == null) {
            return 1;
        }
        else if (e1 != null && Edges.isUndirectedEdge(e1) && e2 != null && Edges.isDirectedEdge(e2)) {
            return 1;
        }
        else if (e1 != null && Edges.isDirectedEdge(e1) && e2 == null) {
            return 2;
        }
        else if (e1 != null && Edges.isDirectedEdge(e1) && e2 != null && Edges.isUndirectedEdge(e2)) {
            return 1;
        }
        else if (e1 != null && Edges.isDirectedEdge(e1) && e2 != null && Edges.isDirectedEdge(e2)) {
            if (Edges.getDirectedEdgeHead(e1) == Edges.getDirectedEdgeTail(e2)) {
                return 1;
            }
        }

        return 0;
    }

    // Parse out separate data files. Save it this time, dammit!

    public void test8() {
        try {
            String indir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/output9.txt";

            String outdir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/separate.datasets2/9/";

            BufferedReader reader = new BufferedReader(new FileReader(indir));
            int count = 1;

            while (true) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (!"========LOGGING DataManip4".equals(line)) continue;

                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();
                    reader.readLine();

                    File file = new File(outdir);
                    file.mkdirs();
                    PrintStream out = new PrintStream(new FileOutputStream(new File(file, count++ + ".txt")));

                    while (!(line = reader.readLine()).trim().isEmpty()) {
                        out.println(line);
                    }

                    out.close();
                }

                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test9() {

        try {

            List<List<List<List<String>>>> allFileClusters = null;
            String type = "FA";

            System.out.println(type);

            if ("FOFC".equals(type)) {
                allFileClusters = getClusteringFofc();
            } else if ("BPC".equals(type)) {
                allFileClusters = getClusteringBpc();
            } else if ("FA".equals(type)) {
                allFileClusters = getClusteringFa();
            }

            // Load data.

            for (int i = 1; i <= 12; i++) {
                List<List<List<String>>> fileClustering = allFileClusters.get(i - 1);

                List<Double> precisions = new ArrayList<Double>();
                List<Double> recalls = new ArrayList<Double>();

                for (int j = 1; j <= 50; j++) {
                    List<List<String>> C = fileClustering.get(j - 1);

                    String dir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/separate.datasets/";

                    DataReader reader = new DataReader();
                    reader.setVariablesSupplied(true);

                    String graphType;

                    switch (i) {
                        case 1:
                        case 2:
                        case 3:
                            graphType = "pure.txt";
                            break;
                        case 4:
                        case 5:
                        case 6:
                            graphType = "impure.txt";
                            break;
                        case 7:
                        case 8:
                        case 9:
                            graphType = "impurecyclic.txt";
                            break;
                        case 10:
                        case 11:
                        case 12:
                            graphType = "impure1.txt";
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    File file = new File(dir + "..", graphType);
                    if (!file.exists()) {
                        System.out.println();
                    }

                    Graph G = GraphUtils.loadGraphTxt(file);
                    List<Node> latents = new ArrayList<Node>();

                    for (Node node : G.getNodes()) {
                        if (node.getName().startsWith("L")) {
                            node.setNodeType(NodeType.LATENT);
                            latents.add(node);
                        }
                    }

                    List<Node> F = getLatents(G);

                    List<Node> M = union(C, G);

                    int sizeM = M.size();
                    int sizeC = C.size();

                    int pureCount = 0;

                    for (List<String> _T : C) {
                        List<Node> T = nodes(_T, G);

                        PICK_AN_L:
                        for (Node L : F) {
                            for (Node X : T) {
                                List<Node> MminusX = new ArrayList<Node>(M);
                                MminusX.remove(X);

                                for (Node Y : MminusX) {
                                    if (!G.isDSeparatedFrom(X, Y, Collections.singletonList(L))) {
                                        continue PICK_AN_L;
                                    }
                                }
                            }

                            pureCount++;
                            break;
                        }
                    }

                    double precision = pureCount / (double) sizeC;
                    double recall;

                    if (graphType.equals("pure.txt")) {
                        recall = sizeM / 48.0;
                    } else {
                        recall = sizeM / 38.0;
                    }

                    precisions.add(precision);
                    recalls.add(recall);

                }

                System.out.println("\nSimulation " + i);

                double[] _precisions = new double[precisions.size()];
                for (int h = 0; h < precisions.size(); h++) _precisions[h] = precisions.get(h);

                System.out.println("Average precision = " + StatUtils.mean(_precisions));
                System.out.println("SE precision = " + StatUtils.sd(_precisions));

                double[] _recalls = new double[recalls.size()];
                for (int h = 0; h < recalls.size(); h++) _recalls[h] = recalls.get(h);

                System.out.println("Average recall = " + StatUtils.mean(_recalls));
                System.out.println("SE recall = " + StatUtils.sd(_recalls));


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test10() {
        try {

//            String outdir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/separate.datasets/" + i + "/";


            List<Long> elapsedTimes1 = new ArrayList<Long>();
            List<Long> elapsedTimes2 = new ArrayList<Long>();

            for (int i = 1; i <= 12; i++) {
                String indir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/output" + i + ".txt";
                BufferedReader reader = new BufferedReader(new FileReader(indir));

                boolean flag = true;

                while (true) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("FOFC")) {
                            flag = true;
                        }
                        if (line.startsWith("BPC")) {
                            flag = false;
                        }

                        if (line.startsWith("Elapsed")) {
                            String[] tokens = line.split(" ");
                            String timeString = tokens[1];

                            try {
                                long elapsed = Long.parseLong(timeString);

                                if (flag) {
                                    elapsedTimes1.add(elapsed);
//                                flag = false;
                                } else {
                                    elapsedTimes2.add(elapsed);
//                                flag = true;
                                }
                            } catch (NumberFormatException e) {
                                //
                            }
                        }

                    }

                    break;
                }
            }


            double[] _elapsedTimes1 = new double[elapsedTimes1.size()];
            for (int h = 0; h < elapsedTimes1.size(); h++) _elapsedTimes1[h] = elapsedTimes1.get(h);

            System.out.println("Average elapsed time FOFC = " + StatUtils.mean(_elapsedTimes1));
            System.out.println("SE elapsed time 1 = " + StatUtils.sd(_elapsedTimes1));

            double[] _elapsedTimes2 = new double[elapsedTimes2.size()];
            for (int h = 0; h < elapsedTimes2.size(); h++) _elapsedTimes2[h] = elapsedTimes2.get(h);

            System.out.println("Average elapsed time BPC = " + StatUtils.mean(_elapsedTimes2));
            System.out.println("SE elapsed time 2 = " + StatUtils.sd(_elapsedTimes2));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void test11() {
        try {

//            String outdir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/separate.datasets/" + i + "/";


            String indir = "/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/erich/simulation/matlaboutput4.txt";
            BufferedReader reader = new BufferedReader(new FileReader(indir));

            List<Double> elapsedTimes1 = new ArrayList<Double>();
            boolean flag = true;

            while (true) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Elapsed")) {
                        System.out.println(line);
                        String[] tokens = line.split(" ");
                        String timeString = tokens[3];

                        System.out.println(timeString);

                        try {
                            double elapsed = Double.parseDouble(timeString) * 1000;
                            elapsedTimes1.add(elapsed);
                        } catch (NumberFormatException e) {
                            //
                        }
                    }

                }

                break;
            }

            double[] _elapsedTimes1 = new double[elapsedTimes1.size()];
            for (int h = 0; h < elapsedTimes1.size(); h++) _elapsedTimes1[h] = elapsedTimes1.get(h);

            System.out.println("Average elapsed time FA = " + StatUtils.mean(_elapsedTimes1));
            System.out.println("SE elapsed time 1 = " + StatUtils.sd(_elapsedTimes1));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<Node> nodes(List<String> t, Graph g) {
        List<Node> out = new ArrayList<Node>();

        for (String s : t) {
            out.add(g.getNode(s));
        }

        return out;
    }

    private List<Node> union(List<List<String>> C, Graph G) {
        Set<String> all = new HashSet<String>();

        for (List<String> c : C) {
            all.addAll(c);
        }

        List<Node> out = new ArrayList<Node>();

        for (String s : all) {
            out.add(G.getNode(s));
        }

        return out;
    }

    private List<String> getStrings(String line) {
        String[] tokens = line.split(" ");
        List<String> cluster = new ArrayList<String>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.startsWith("'X")) {
                token = token.substring(1, token.length() - 1);
                cluster.add(token);
            }
        }
        return cluster;
    }

    private List<Node> getLatents(Graph fullGraph) {
        List<Node> latents = new ArrayList<Node>();
        for (Node node : fullGraph.getNodes()) if (node.getNodeType() == NodeType.LATENT) latents.add(node);
        return latents;
    }

    private int numClusteredVars(List<List<Node>> clusters) {
        int sum = 0;

        for (List<Node> c : clusters) {
            sum += c.size();
        }
        return sum;
    }

    private Graph createFullyConnectedTwoFactorModel(Graph latentGraph, Graph fullMimGraph, List<List<Node>> foundClusters4) {
        fullMimGraph = new EdgeListGraph(fullMimGraph);

        for (List<Node> c : foundClusters4) {
            Node l1 = getLatent(fullMimGraph, c);
            Node l = new GraphNode(l1.getName() + "-2");
            l.setNodeType(NodeType.LATENT);
            fullMimGraph.addNode(l);

            for (Node n : c) {
                fullMimGraph.addDirectedEdge(l, n);
            }
        }

        for (Edge edge : latentGraph.getEdges()) {
            fullMimGraph.removeEdge(edge);
        }

        List<Node> latents = new ArrayList<Node>();

        for (Node node : fullMimGraph.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) latents.add(node);
        }

        for (int _i = 0; _i < latents.size(); _i++) {
            for (int _j = _i + 1; _j < latents.size(); _j++) {
                fullMimGraph.addDirectedEdge(latents.get(_i), latents.get(_j));
            }
        }

        return fullMimGraph;
    }

    private Graph createFullyConnectedOneFactorModel(Graph latentGraph, Graph fullMimGraph) {
        fullMimGraph = new EdgeListGraph(fullMimGraph);

        for (Edge edge : latentGraph.getEdges()) {
            fullMimGraph.removeEdge(edge);
        }

        List<Node> latents = new ArrayList<Node>();

        for (Node node : fullMimGraph.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) latents.add(node);
        }

        Collections.shuffle(latents);

        for (int _i = 0; _i < latents.size(); _i++) {
            for (int _j = _i + 1; _j < latents.size(); _j++) {
                fullMimGraph.addDirectedEdge(latents.get(_i), latents.get(_j));
            }
        }

        return fullMimGraph;
    }

    private double calcModelP(DataSet data, Graph fullMimGraph) {
        Graph dag = SearchGraphUtils.dagFromPattern(fullMimGraph);
        SemEstimator est = new SemEstimator(data, new SemPm(dag), new SemOptimizerEm());
        SemIm est2 = est.estimate();
        return est2.getPValue();
    }

    private void increment(Map<Edge, Integer> edges, Edge edge) {
        if (!edges.containsKey(edge)) {
            edges.put(edge, 0);
        }

        edges.put(edge, edges.get(edge) + 1);
    }

    private Node getLatent(Graph graph, List<Node> cluster) {
        return graph.getParents(cluster.get(0)).get(0);
    }

    private String getType(List<Node> cluster) {
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;

        if (cluster == null) {
            System.out.println();
        }

        for (Node node : cluster) {
            if (node == null) {
                System.out.println();
            }
        }

        for (Node node : cluster) {
            if (node.getName().startsWith("STR")/* || node.getName().equals("DEP14")*/) {
                count1++;
            } else if (node.getName().startsWith("DEP") /*&& !node.getName().equals("DEP14")*/) {
                count2++;
            } else if (node.getName().startsWith("COP") || node.getName().equals("ATTEND")) {
                count3++;
            }
        }

        if (count1 > 0 && count2 == 0 && count3 == 0) return "STR";
        else if (count2 > 0 && count1 == 0 && count3 == 0) return "DEP";
        else if (count3 > 0 && count1 == 0 && count2 == 0) return "COP";
        else return "MIXED";
    }

    /**
     * Returns the minimum p value over the clusters.
     */
    private double printClusterPs(DataSet data, List<List<Node>> foundClusters) {
        NumberFormat nf = new DecimalFormat("0.0000");
        double minP = 1;

        System.out.println("\nCluster Chi Square P Values");

        for (List<Node> c : foundClusters) {
            double p = getClusterP(data, c);
            System.out.println("p =  " + nf.format(p) + " " + c);

            if (!Double.isNaN(p) && p < minP) {
                minP = p;
            }
        }

        return minP;
    }

    private double getClusterP(DataSet data, List<Node> c) {
        Graph g = getClusterGraph(c);
        SemPm _pm = new SemPm(g);
        SemEstimator est = new SemEstimator(data, _pm);
        SemIm estIm = est.estimate();
        return estIm.getPValue();
    }

    private Graph getClusterGraph(List<Node> c) {
        Graph g = new EdgeListGraph(c);
        Node l = new GraphNode("L");
        l.setNodeType(NodeType.LATENT);
        g.addNode(l);

        for (Node n : c) {
            g.addDirectedEdge(l, n);
        }

        return g;
    }

    private Graph getFullGraph(List<List<Node>> c) {
        List<Node> union = new ArrayList<Node>();
        for (List<Node> d : c) union.addAll(d);
        Graph g = new EdgeListGraph(union);
        int count = 0;

        for (List<Node> d : c) {
            Node l = new GraphNode("L" + (++count));
            l.setNodeType(NodeType.LATENT);
            g.addNode(l);

            for (Node n : d) {
                g.addDirectedEdge(l, n);
            }

        }

        return g;
    }

    private List<Node> largestPureSubcluster(List<Node> cluster, List<List<Node>> trueClusters) {
        List<Node> e = new ArrayList<Node>();

        for (List<Node> c : trueClusters) {
            List<Node> d = new ArrayList<Node>(cluster);
            d.retainAll(c);
            if (d.size() > e.size()) {
                e = d;
            }
        }

        return e;
    }

    private List<Node> largestPureSubcluster2(List<Node> cluster, List<List<Node>> trueClusters) {
        List<Node> e = new ArrayList<Node>();

        for (List<Node> c : trueClusters) {
            List<Node> d = new ArrayList<Node>(cluster);
            d.retainAll(c);
            if (d.size() > e.size()) {
                e = c;
            }
        }

        return e;
    }

    private List<Node> measuredVariables(Graph mim) {
        List<Node> vars = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.MEASURED) {
                vars.add(node);
            }
        }

        return vars;
    }

    private Graph structure(Graph mim) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node);
            }
        }

        return mim.subgraph(latents);
    }

    // This reidentifies a variable if all of its members belong to one of the clusters
    // in the original graph.
    private Graph reidentifyVariables(Graph searchGraph, Graph trueGraph) {
        if (trueGraph == null) {
            return searchGraph;
        }

        Graph reidentifiedGraph = new EdgeListGraph();

        for (Node latent : searchGraph.getNodes()) {
            if (latent.getNodeType() != NodeType.LATENT) {
                continue;
            }

            boolean added = false;

            List<Node> searchChildren = searchGraph.getChildren(latent);

            for (Node _latent : trueGraph.getNodes()) {
                List<Node> trueChildren = trueGraph.getChildren(_latent);

                for (Node node2 : new ArrayList<Node>(trueChildren)) {
                    if (node2.getNodeType() == NodeType.LATENT) {
                        trueChildren.remove(node2);
                    }
                }

                boolean containsAll = true;

                for (Node child : searchChildren) {
                    boolean contains = false;

                    for (Node _child : trueChildren) {
                        if (child.getName().equals(_child.getName())) {
                            contains = true;
                            break;
                        }
                    }

                    if (!contains) {
                        containsAll = false;
                        break;
                    }
                }

                if (containsAll) {
                    reidentifiedGraph.addNode(_latent);

                    for (Node child : searchChildren) {
                        if (!reidentifiedGraph.containsNode(child)) {
                            reidentifiedGraph.addNode(child);
                        }

                        reidentifiedGraph.addDirectedEdge(_latent, child);
                    }

                    added = true;
                    break;
                }
            }

            if (!added) {
                reidentifiedGraph.addNode(latent);

                for (Node child : searchChildren) {
                    if (!reidentifiedGraph.containsNode(child)) {
                        reidentifiedGraph.addNode(child);
                    }

                    reidentifiedGraph.addDirectedEdge(latent, child);
                }
            }
        }

        return reidentifiedGraph;
    }

    // This reidentifies a variable in the output with a variable in the input if the sum of the
    // factor loadings for the output clusters on the input's loadings is greater than for
    // any other input latent.
    private List<String> reidentifyVariables2(List<List<Node>> clusters, Graph trueGraph, DataSet data) {
        trueGraph = GraphUtils.replaceNodes(trueGraph, data.getVariables());
        Map<Node, SemIm> ims = new HashMap<Node, SemIm>();
        List<String> latentNames = new ArrayList<String>();

        for (Node node : trueGraph.getNodes()) {
            if (node.getNodeType() != NodeType.LATENT) continue;

            List<Node> children = trueGraph.getChildren(node);

            List<Node> all = new ArrayList<Node>();
            all.add(node);
            all.addAll(children);

            Graph subgraph = trueGraph.subgraph(all);

            SemPm pm = new SemPm(subgraph);
            SemEstimator est = new SemEstimator(data, pm);
            SemIm im = est.estimate();

            ims.put(node, im);
        }

        Map<List<Node>, String> clustersToNames = new HashMap<List<Node>, String>();


//        Graph reidentifiedGraph = new EdgeListGraph();

        for (List<Node> cluster : clusters) {
            double maxSum = Double.NEGATIVE_INFINITY;
            Node maxLatent = null;

            for (Node _latent : trueGraph.getNodes()) {
                if (_latent.getNodeType() != NodeType.LATENT) {
                    continue;
                }

                double sum = sumOfAbsLoadings(cluster, _latent, trueGraph, ims);

                if (sum > maxSum) {
                    maxSum = sum;
                    maxLatent = _latent;
                }
            }

            String name = maxLatent.getName();
            latentNames.add(name);
            clustersToNames.put(cluster, name);
        }


        Set<String> values = new HashSet<String>(clustersToNames.values());

        for (String key : values) {
            double maxSum = Double.NEGATIVE_INFINITY;
            List<Node> maxCluster = null;

            for (List<Node> _cluster : clustersToNames.keySet()) {
                if (clustersToNames.get(_cluster).equals(key)) {
                    double sum = sumOfAbsLoadings(_cluster, trueGraph.getNode(key), trueGraph, ims);
                    if (sum > maxSum) {
                        maxCluster = _cluster;
                    }
                }
            }

            for (List<Node> _cluster : clustersToNames.keySet()) {
                if (clustersToNames.get(_cluster).equals(key)) {
                    if (!_cluster.equals(maxCluster)) {
                        String name = key;

                        while (latentNames.contains(name)) {
                            name = name + "*";
                        }

                        clustersToNames.put(_cluster, name);
                        latentNames.set(clusters.indexOf(_cluster), name);
                    }
                }
            }
        }

        return latentNames;
    }

    private double sumOfAbsLoadings(List<Node> searchChildren, Node latent, Graph mim, Map<Node, SemIm> ims) {
        double sum = 0.0;

        for (Node child : searchChildren) {
            if (mim.isParentOf(latent, child)) {
                SemIm im = ims.get(latent);
                double coef = im.getEdgeCoef(latent, child);
                sum += abs(coef);
            }
        }

        return sum;
    }


    private DataSet reorderVariables(DataSet data) {
        List<Node> variables = new ArrayList<Node>(data.getVariables());
        Collections.shuffle(variables);

        List<Node> vars = new ArrayList<Node>();

        for (Node node : variables) {
            Node _node = data.getVariable(node.getName());

            if (_node != null) {
                vars.add(_node);
            }
        }

        return data.subsetColumns(vars);
    }

    public static Test suite() {
        return new TestSuite(TestErichPaper.class);
    }
}
