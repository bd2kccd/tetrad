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

import edu.cmu.tetrad.cluster.ClusterUtils;
import edu.cmu.tetrad.data.*;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.sem.SemEstimator;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemImInitializationParams;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.MatrixUtils;
import edu.cmu.tetrad.util.TetradMatrix;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.*;

import static java.lang.Math.abs;


/**
 * Tests the IndTestTimeSeries class.
 *
 * @author Joseph Ramsey
 */
public class TestMimbuild3 extends TestCase {
    public TestMimbuild3(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void test1() {
        for (int r = 0; r < 5; r++) {
            Graph mim = GraphUtils.randomSingleFactorModel(5, 5, 6, 0, 0, 0);

            Graph mimStructure = structure(mim);

            SemPm pm = new SemPm(mim);
            SemIm im = new SemIm(pm);
            DataSet data = im.simulateData(1000, false);

            String algorithm = "FOFC";
            Graph searchGraph;
            List<List<Node>> partition;

            if (algorithm.equals("FOFC")) {
                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.05);
                searchGraph = fofc.search();
                partition = fofc.getClusters();
            } else if (algorithm.equals("BPC")) {
                TestType testType = TestType.TETRAD_WISHART;
                TestType purifyType = TestType.TETRAD_BASED2;

                BuildPureClusters bpc = new BuildPureClusters(
                        data, 0.001,
                        testType,
                        purifyType);
                searchGraph = bpc.search();

                partition = MimUtils.convertToClusters2(searchGraph);
            } else {
                throw new IllegalStateException();
            }

            List<String> latentVarList = reidentifyVariables(mim, data, partition, 2);

            System.out.println(partition);
            System.out.println(latentVarList);

            System.out.println("True\n" + mimStructure);

            Graph mimbuildStructure;

            for (int mimbuildMethod : new int[]{1, 2, 3}) {
                if (mimbuildMethod == 1) {
                    System.out.println("Mimbuild 1\n");
                    Clusters measurements = ClusterUtils.mimClusters(searchGraph);
                    IndTestMimBuild test = new IndTestMimBuild(data, 0.001, measurements);
                    MimBuild mimbuild = new MimBuild(test, new Knowledge());
                    Graph full = mimbuild.search();
                    full = changeLatentNames(full, measurements, latentVarList);
                    mimbuildStructure = structure(full);
//                    TetradMatrix latentcov = mimbuild.getLatentsCov();
//                    List<String> latentnames = mimbuild.getLatentNames();
//                    System.out.println("\nCovariance over the latents");
//                    System.out.println(MatrixUtils.toStringSquare(latentcov.toArray(), latentnames));
                    System.out.println("SHD = " + structuralHammingDistance(mimStructure, mimbuildStructure));
                    System.out.println("Estimated\n" + mimbuildStructure);
                    System.out.println();
                } else if (mimbuildMethod == 2) {
                    System.out.println("Mimbuild 2\n");
                    Mimbuild2 mimbuild = new Mimbuild2();
                    mimbuild.setAlpha(0.001);
                    mimbuildStructure = mimbuild.search(partition, latentVarList, data);
                    TetradMatrix latentcov = mimbuild.getLatentsCov();
                    List<String> latentnames = mimbuild.getLatentNames();
                    System.out.println("\nCovariance over the latents");
                    System.out.println(MatrixUtils.toStringSquare(latentcov.toArray(), latentnames));
                    System.out.println("Estimated\n" + mimbuildStructure);
                    System.out.println("SHD = " + structuralHammingDistance(mimStructure, mimbuildStructure));
                    System.out.println();
                } else if (mimbuildMethod == 3) {
                    System.out.println("Mimbuild 3\n");
                    Mimbuild3 mimbuild = new Mimbuild3();
                    mimbuild.setAlpha(0.001);
                    mimbuild.setMinimumSize(3);
                    mimbuildStructure = mimbuild.search(partition, latentVarList, new CovarianceMatrix(data));
                    TetradMatrix latentcov = mimbuild.getLatentsCov();
                    List<String> latentnames = mimbuild.getLatentNames();
                    System.out.println("\nCovariance over the latents");
                    System.out.println(MatrixUtils.toStringSquare(latentcov.toArray(), latentnames));
                    System.out.println("Estimated\n" + mimbuildStructure);
                    System.out.println("SHD = " + structuralHammingDistance(mimStructure, mimbuildStructure));
                    System.out.println();
                } else {
                    throw new IllegalStateException();
                }
            }

        }

    }

    public void test2() {
        System.out.println("SHD\tP");
//        System.out.println("MB1\tMB2\tMB3\tMB4\tMB5\tMB6");

        LOOP:
        for (int r = 0; r < 20; r++) {
            Graph mim = GraphUtils.randomSingleFactorModel(5, 5, 12, 0, 0, 0);

            Graph mimStructure = structure(mim);

            SemPm pm = new SemPm(mim);
            SemImInitializationParams params = new SemImInitializationParams();
            params.setCoefRange(0.5, 1.5);
            SemIm im = new SemIm(pm, params);

            DataSet data = im.simulateData(1000, false);
            CovarianceMatrix cov = new CovarianceMatrix(data);

            String algorithm = "FOFC";
            Graph searchGraph;
            List<List<Node>> partition;

            if (algorithm.equals("FOFC")) {
                FindOneFactorClusters fofc = new FindOneFactorClusters(data, TestType.TETRAD_WISHART, 0.0001);
                searchGraph = fofc.search();
                partition = fofc.getClusters();
            } else if (algorithm.equals("BPC")) {
                TestType testType = TestType.TETRAD_WISHART;
                TestType purifyType = TestType.TETRAD_BASED2;

                BuildPureClusters bpc = new BuildPureClusters(
                        data, 0.001,
                        testType,
                        purifyType);
                searchGraph = bpc.search();

                partition = MimUtils.convertToClusters2(searchGraph);
            } else {
                throw new IllegalStateException();
            }


            List<String> latentVarList = reidentifyVariables(mim, data, partition, 2);

            Graph mimbuildStructure;

            for (int mimbuildMethod : new int[]{3}) {
                if (mimbuildMethod == 3) {
                    Mimbuild3 mimbuild = new Mimbuild3();
                    mimbuild.setAlpha(0.2);
                    mimbuild.setMinimumSize(3);
                    try {
                        mimbuildStructure = mimbuild.search(partition, latentVarList, cov);
                    } catch (Exception e) {
                        continue;
                    }
                    int shd2 = structuralHammingDistance(mimStructure, mimbuildStructure);
                    System.out.print(shd2 + "\t" + mimbuild.getpValue());
                }
            }

            System.out.println();

        }

    }

    public void test3() {
        Node x = new GraphNode("X");
        Node y = new GraphNode("Y");
        Node z = new GraphNode("Z");
        Node w = new GraphNode("W");

        List<Node> nodes = new ArrayList<Node>();
        nodes.add(x);
        nodes.add(y);
        nodes.add(z);
        nodes.add(w);

        Graph g = new EdgeListGraph(nodes);
        g.addDirectedEdge(x, y);
        g.addDirectedEdge(x, z);
        g.addDirectedEdge(y, w);
        g.addDirectedEdge(z, w);

        Graph maxGraph = null;
        double maxPValue = -1.0;
        TetradMatrix maxLatentCov = null;

        Graph mim = GraphUtils.randomMim(g, 8, 0, 0, 0, true);
//        Graph mim = GraphUtils.randomSingleFactorModel(5, 5, 8, 0, 0, 0);
        Graph mimStructure = structure(mim);
        SemPm pm = new SemPm(mim);

        System.out.println("\n\nTrue graph:");
        System.out.println(mimStructure);

        SemImInitializationParams params = new SemImInitializationParams();
        params.setCoefRange(0.5, 1.5);

        SemIm im = new SemIm(pm, params);

        int N = 1000;

        DataSet data = im.simulateData(N, false);

        CovarianceMatrix cov = new CovarianceMatrix(data);

        for (int i = 0; i < 10; i++) {

            ICovarianceMatrix _cov = DataUtils.reorderColumns(cov);
            List<List<Node>> partition;

            FindOneFactorClusters fofc = new FindOneFactorClusters(_cov, TestType.TETRAD_WISHART, .0001);
            fofc.search();
            partition = fofc.getClusters();
            System.out.println(partition);

            List<String> latentVarList = reidentifyVariables(mim, data, partition, 2);

            Mimbuild3 mimbuild = new Mimbuild3();

            mimbuild.setAlpha(0.01);
//            mimbuild.setMinimumSize(5);

            Graph mimbuildStructure = mimbuild.search(partition, latentVarList, _cov);

            double pValue = mimbuild.getpValue();
            System.out.println(mimbuildStructure);
            System.out.println("P = " + pValue);
            System.out.println("Latent Cov = " + MatrixUtils.toString(mimbuild.getLatentsCov().toArray()));

            if (pValue > maxPValue) {
                maxPValue = pValue;
                maxGraph = new EdgeListGraph(mimbuildStructure);
                maxLatentCov = mimbuild.getLatentsCov();
            }
        }

        System.out.println("\n\nTrue graph:");
        System.out.println(mimStructure);
        System.out.println("\nBest graph:");
        System.out.println(maxGraph);
        System.out.println("P = " + maxPValue);
        System.out.println("Latent Cov = " + MatrixUtils.toString(maxLatentCov.toArray()));
        System.out.println();
    }

    private Graph changeLatentNames(Graph full, Clusters measurements, List<String> latentVarList) {
        Graph g2 = null;

        try {
            g2 = (Graph) new MarshalledObject(full).get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < measurements.getNumClusters(); i++) {
            List<String> d = measurements.getCluster(i);
            String latentName = latentVarList.get(i);

            for (Node node : full.getNodes()) {
                if (!(node.getNodeType() == NodeType.LATENT)) {
                    continue;
                }

                List<Node> _children = full.getChildren(node);

                _children.removeAll(getLatents(full));

                List<String> childNames = getNames(_children);

                if (new HashSet<String>(childNames).equals(new HashSet<String>(d))) {
                    g2.getNode(node.getName()).setName(latentName);
                }
            }
        }

        return g2;
    }

    private List<String> getNames(List<Node> nodes) {
        List<String> names = new ArrayList<String>();
        for (Node node : nodes) {
            names.add(node.getName());
        }
        return names;
    }


    private List<String> reidentifyVariables(Graph mim, DataSet data, List<List<Node>> partition, int method) {
        List<String> latentVarList;

        if (method == 1) {
            latentVarList = reidentifyVariables1(partition, mim);
        } else if (method == 2) {
            latentVarList = reidentifyVariables2(partition, mim, data);
        } else {
            throw new IllegalStateException();
        }

        return latentVarList;
    }

    // This reidentifies a variable if all of its members belong to one of the clusters
    // in the original graph.
    private List<String> reidentifyVariables1(List<List<Node>> partition, Graph trueGraph) {
        List<String> names = new ArrayList<String>();
        Node latent = null;

        for (List<Node> _partition : partition) {
            boolean added = false;

            for (Node _latent : trueGraph.getNodes()) {
                List<Node> trueChildren = trueGraph.getChildren(_latent);

                for (Node node2 : new ArrayList<Node>(trueChildren)) {
                    if (node2.getNodeType() == NodeType.LATENT) {
                        trueChildren.remove(node2);
                    }
                }

                boolean containsAll = true;
                latent = _latent;

                for (Node child : _partition) {
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
                    String name = latent.getName();

                    while (names.contains(name)) {
                        name += "*";
                    }

                    names.add(name);

                    for (Node child : _partition) {
                        if (!_partition.contains(child)) {
                            _partition.add(child);
                        }
                    }

                    added = true;
                    break;
                }
            }

            if (!added) {
                String name = "M*";

                while (names.contains(name)) {
                    name += "*";
                }

                names.add(name);

                for (Node child : _partition) {
                    if (!_partition.contains(child)) {
                        _partition.add(child);
                    }
                }
            }
        }

        return names;
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

    private Graph structure(Graph mim) {
        List<Node> latents = new ArrayList<Node>();

        for (Node node : mim.getNodes()) {
            if (node.getNodeType() == NodeType.LATENT) {
                latents.add(node);
            }
        }

        return mim.subgraph(latents);
    }


    private int structuralHammingDistance(Graph trueGraph, Graph estGraph) {
        int error = 0;

        estGraph = GraphUtils.replaceNodes(estGraph, trueGraph.getNodes());

        Set<Node> _allNodes = new HashSet<Node>();

        List<Node> trueLatents = getLatents(trueGraph);
        List<Node> estLatents = getLatents(estGraph);

        Graph u = trueGraph.subgraph(trueLatents);
        Graph t = estGraph.subgraph(estLatents);

        Graph G = new Pc(new IndTestDSep(u, true)).search();
        Graph H = new Pc(new IndTestDSep(t, true)).search();

//        System.out.println("Pattern of true graph over latents = " + G);

        _allNodes.addAll(trueLatents);
        _allNodes.addAll(estLatents);

        List<Node> allNodes = new ArrayList<Node>(_allNodes);

        for (int i1 = 0; i1 < allNodes.size(); i1++) {
            for (int i2 = i1 + 1; i2 < allNodes.size(); i2++) {
                Node l1 = allNodes.get(i1);
                Node l2 = allNodes.get(i2);

                Edge e1 = G.getEdge(l1, l2);
                Edge e2 = H.getEdge(l1, l2);

                int shd = structuralHammingDistanceOneEdge(e1, e2);
                error += shd;
            }
        }
        return error;
    }

    private int structuralHammingDistanceOneEdge(Edge e1, Edge e2) {
        if (e1 == null && e2 != null && Edges.isUndirectedEdge(e2)) {
            return 1;
        } else if (e1 == null && e2 != null && Edges.isDirectedEdge(e2)) {
            return 2;
        } else if (e1 != null && Edges.isUndirectedEdge(e1) && e2 == null) {
            return 1;
        } else if (e1 != null && Edges.isUndirectedEdge(e1) && e2 != null && Edges.isDirectedEdge(e2)) {
            return 1;
        } else if (e1 != null && Edges.isDirectedEdge(e1) && e2 == null) {
            return 2;
        } else if (e1 != null && Edges.isDirectedEdge(e1) && e2 != null && Edges.isUndirectedEdge(e2)) {
            return 1;
        } else if (e1 != null && Edges.isDirectedEdge(e1) && e2 != null && Edges.isDirectedEdge(e2)) {
            if (Edges.getDirectedEdgeHead(e1) == Edges.getDirectedEdgeTail(e2)) {
                return 1;
            }
        }

        return 0;
    }

    private List<Node> getLatents(Graph graph) {
        List<Node> latents = new ArrayList<Node>();
        for (Node node : graph.getNodes()) if (node.getNodeType() == NodeType.LATENT) latents.add(node);
        return latents;
    }

    public static Test suite() {
        return new TestSuite(TestMimbuild3.class);
    }
}
