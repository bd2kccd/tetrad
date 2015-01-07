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
import edu.cmu.tetrad.sem.*;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.TetradMatrix;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: jdramsey Date: Jun 17, 2010 Time: 9:39:48 AM To change this template use File |
 * Settings | File Templates.
 */
public class Mimbuild2 {
    private Graph mim;
    private int minClusterSize = 3;
    private int numCovSamples = 1000;
    private List<Node> latents;
    private List<Node> _latents;
    private Graph structureGraph;
    private List<List<Node>> clustering;
    private double alpha = 1e-6;
    private Knowledge knowledge = new Knowledge();
    private ICovarianceMatrix covMatrix;
    private TetradMatrix latentsCov;
    private List<String> latentNames;

    public Mimbuild2() {
    }

    public void setTrueMim(Graph mim) {
        this.mim = mim;
    }

    public Graph search(List<List<Node>> clustering, DataSet data) {
        return search(clustering, null, data);
    }

    public Graph search(List<List<Node>> clustering, List<String> names, DataSet data) {
        List<List<Node>> _clustering = new ArrayList<List<Node>>();

        for (List<Node> cluster : clustering) {
            List<Node> _cluster = new ArrayList<Node>();

            for (Node node : cluster) {
                _cluster.add(data.getVariable(node.getName()));
            }

            _clustering.add(_cluster);
        }

        clustering = _clustering;


//        System.out.println("Create a latent for each cluster.");

        List<Node> latents = defineLatents(clustering, names);

//        System.out.println("Center data.");

        TetradMatrix _data = DataUtils.centerData(data.getDoubleData());

//        printClusterSizes(clustering);

        List<Node> allNodes = new ArrayList<Node>();

        for (List<Node> cluster : clustering) {
            allNodes.addAll(cluster);
        }

//        printSubgraph(mim, allNodes);
//        printClusterSizes(clustering);

//        System.out.println("Remove small clusters.");

        List<Node> _latents = removeSmallClusters(latents, clustering, getMinClusterSize());

//        if (clustering.isEmpty()) {
//            System.out.println("There were no clusters after small clusters were removed..");
//        }

//        printClusterSizes(clustering);
//        printClustering(clustering);

        this.clustering = clustering;

//        System.out.println("Calculate loadings");

        Node[][] indicators = getIndicators(clustering);
        double[][] loadings = getLoadings(clustering, data);

//        System.out.println("Estimate covariance matrix over latents");

        TetradMatrix cov = getCov(data, _data, _latents, loadings, indicators);
        this.latentsCov = cov;
        this.latentNames = new ArrayList<String>();
        for (Node node : _latents) latentNames.add(node.getName());
        CovarianceMatrix covMatrix = new CovarianceMatrix(_latents, cov, data.getNumRows());

//        System.out.println("Run pattern search over latent cov matrix");

//        Ges patternSearch = new Ges(covMatrix);
//        patternSearch.setKnowledge(getKnowledge());
//
//        IndependenceTest testFisherZ = new IndTestFisherZ(covMatrix, getAlpha());
//        Jcpc patternSearch = new Jcpc(testFisherZ);
//        patternSearch.setKnowledge(getKnowledge());

        IndependenceTest testFisherZ = new IndTestFisherZ(covMatrix, getAlpha());
        Cpc patternSearch = new Cpc(testFisherZ);
//        patternSearch.setDepth(0);
        patternSearch.setKnowledge(getKnowledge());
//
        Graph _graph = patternSearch.search();

        this._latents = _latents;
        this.structureGraph = new EdgeListGraph(_graph);
        GraphUtils.fruchtermanReingoldLayout(this.structureGraph);

        this.covMatrix = covMatrix;

        return this.structureGraph;
    }

    private List<Node> defineLatents(List<List<Node>> clustering, List<String> names) {
        if (names != null) {
            List<Node> latents = new ArrayList<Node>();

            for (String name : names) {
                Node node = new GraphNode(name);
                node.setNodeType(NodeType.LATENT);
                latents.add(node);
            }

            return latents;
        } else {
            List<Node> latents;

            if (this.latents == null) {
                latents = new ArrayList<Node>();

                for (int i = 0; i < clustering.size(); i++) {
                    Node node = new GraphNode("_L" + (i + 1));
                    node.setNodeType(NodeType.LATENT);
                    latents.add(node);
                }
            } else {
                latents = this.latents;
            }

            return latents;
        }
    }


//    private void printClusterSizes(List<List<Node>> clustering) {
//        System.out.print("\nCluster sizes");
//
//        for (List<Node> cluster : clustering) {
//            System.out.print("\t" + cluster.size());
//        }
//
//        System.out.println();
//    }

//    private void printClustering(List<List<Node>> clustering) {
//        for (int i = 0; i < clustering.size(); i++) {
//            System.out.println("Cluster " + i + ": " + clustering.get(i));
//        }
//    }

//    private void printSubgraph(Graph mim, List<Node> nodes) {
//        if (mim != null) {
//            System.out.println(mim.subgraph(nodes));
//        }
//    }

    private List<Node> removeSmallClusters(List<Node> latents, List<List<Node>> clustering, int minimumSize) {
        List<Node> _latents = new ArrayList<Node>(latents);

        for (int i = _latents.size() - 1; i >= 0; i--) {
            Collections.shuffle(clustering.get(i));

            if (clustering.get(i).size() < minimumSize) {
                clustering.remove(clustering.get(i));
                Node latent = _latents.get(i);
                _latents.remove(latent);
//                System.out.println("Removing " + latent);
            }

        }
        return _latents;
    }

    private Node[][] getIndicators(List<List<Node>> clustering) {
        Node[][] indicators = new Node[clustering.size()][];

        for (int i = 0; i < clustering.size(); i++) {
            List<Node> _indicators = clustering.get(i);
            indicators[i] = _indicators.toArray(new Node[clustering.get(i).size()]);
        }

        return indicators;
    }

    private double[][] getLoadings(List<List<Node>> clustering, DataSet data) {
        double[][] loadings = new double[clustering.size()][];

        for (int i = 0; i < clustering.size(); i++) {
            List<Node> cluster = clustering.get(i);
//            System.out.println("Estimating loadings for cluster " + i);

            if (cluster.isEmpty()) throw new IllegalArgumentException("Empty cluster.");

            List<Node> indicators = cluster;
            DataSet data1 = data.subsetColumns(indicators);

            CovarianceMatrix cov1 = new CovarianceMatrix(data1);

            Node latent = new ContinuousVariable("L");
            latent.setNodeType(NodeType.LATENT);

            Graph _graph = new EdgeListGraph();
            _graph.addNode(latent);

            for (Node indicator : indicators) {
                _graph.addNode(indicator);
                _graph.addDirectedEdge(latent, indicator);
            }

            SemPm pm = new SemPm(_graph);

//            Parameter parameter = pm.getParameter(latent, indicators.get(0));
//            parameter.setFixed(true);
//            parameter.setStartingValue(1.0);

            Parameter parameter2 = pm.getParameter(latent, latent);
            parameter2.setFixed(true);
            parameter2.setStartingValue(1.0);

            SemOptimizer optimizer = new SemOptimizerEm();

            SemEstimator semEstimator = new SemEstimator(cov1, pm, optimizer);
            SemIm estIm = semEstimator.estimate();

            loadings[i] = new double[indicators.size()];

            for (int j = 0; j < indicators.size(); j++) {
                double edgeCoef = estIm.getEdgeCoef(latent, indicators.get(j));
                loadings[i][j] = edgeCoef;
            }
        }

        return loadings;
    }

    private TetradMatrix getCov(DataSet data, TetradMatrix _data, List<Node> latents, double[][] loadings, Node[][] indicators) {
        TetradMatrix cov = new TetradMatrix(latents.size(), latents.size());

        double[] vars = new double[latents.size()];

        Map<Node, Integer> nodeMap = new HashMap<Node, Integer>();
        List<Node> variables = data.getVariables();

        for (int i = 0; i < data.getNumColumns(); i++) {
            nodeMap.put(variables.get(i), i);
        }

        for (int m = 0; m < latents.size(); m++) {
//            System.out.println("Calculating variance " + m);
            vars[m] = var(_data, loadings, indicators, nodeMap, m);
            cov.set(m, m, vars[m]);
        }

        for (int m = 0; m < latents.size(); m++) {
//            System.out.println("Calculating covariances for variable " + m);

            for (int n = m + 1; n < latents.size(); n++) {
                double thetamn = covar(_data, loadings, indicators, nodeMap, m, n);

                cov.set(m, n, thetamn);
                cov.set(n, m, thetamn);
            }
        }

        return cov;
    }

    private double var(TetradMatrix _data, double[][] loadings, Node[][] indicators,
                       Map<Node, Integer> nodeMap, int m) {
        double sum = 0.0;
        int count = 0;

        if (indicators[m].length == 1) {
            throw new IllegalArgumentException("Must have at least 2 indicators.");
        }

        for (int i = 0; i < loadings[m].length; i++) {
            for (int j = i + 1; j < loadings[m].length; j++) {
                double ai = loadings[m][i];
                double aj = loadings[m][j];

                double[] coli = _data.getColumn(nodeMap.get(indicators[m][i])).toArray();
                double[] colj = _data.getColumn(nodeMap.get(indicators[m][j])).toArray();

                double covij = StatUtils.covariance(coli, colj);

                double numerator = covij;
                double denominator = ai * aj;
                sum += numerator / denominator;
                count++;
            }
        }

        return sum / count;
    }

    private double covar(TetradMatrix _data, double[][] loadings, Node[][] indicators,
                         Map<Node, Integer> nodeMap, int m, int n) {
        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < loadings[m].length; i++) {
            for (int j = 0; j < loadings[n].length; j++) {
                double ai = loadings[m][i];
                double aj = loadings[n][j];

                double[] coli = _data.getColumn(nodeMap.get(indicators[m][i])).toArray();
                double[] colj = _data.getColumn(nodeMap.get(indicators[n][j])).toArray();

                double covij = StatUtils.covariance(coli, colj);

                double numerator = covij;
                double denominator = ai * aj;
                sum += numerator / denominator;
                count++;
            }
        }

        return sum / count;
    }

    public int getMinClusterSize() {
        return minClusterSize;
    }

    public void setMinClusterSize(int minClusterSize) {
        if (minClusterSize < 2) {
            throw new IllegalArgumentException("Must have at least 2 indicators in every cluster, or else the " +
                    "program will go into convulsions: " + minClusterSize);
        }

        this.minClusterSize = minClusterSize;
    }

    public void setLatentsBeforeSearch(List<Node> latents) {
        this.latents = latents;
    }

    public List<Node> latentsAfterSearch() {
        return _latents;
    }

    /**
     * Returns the full discovered graph, with latents and indicators.
     *
     */
    public Graph getFullGraph() {
        Graph graph = new EdgeListGraph(structureGraph);

//        if (newNodes) {
//            graph = new EdgeListGraph(GraphUtils.newNodes(structureGraph));
//        } else {
//            graph = new EdgeListGraph(structureGraph);
//        }

        for (int i = 0; i < _latents.size(); i++) {
            Node latent = _latents.get(i);

//            if (!graph.containsNode(latent)) {
//                graph.addNode(latent);
//            }

            List<Node> measuredGuys = getClustering().get(i);

            for (Node measured : measuredGuys) {
                if (!graph.containsNode(measured)) {
                    graph.addNode(measured);
                }

                graph.addDirectedEdge(latent, measured);
            }
        }

        return graph;
    }

    public List<List<Node>> getClustering() {
        return clustering;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public Knowledge getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(Knowledge knowledge) {
        this.knowledge = knowledge;
    }

    public ICovarianceMatrix getCovMatrix() {
        return this.covMatrix;
    }

    public TetradMatrix getLatentsCov() {
        return latentsCov;
    }

    public List<String> getLatentNames() {
        return latentNames;
    }
}

