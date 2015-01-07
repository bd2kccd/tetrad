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
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.data.Knowledge;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.TetradMatrix;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import pal.math.*;

import java.util.ArrayList;
import java.util.List;

public class Mimbuild3 {
    private List<Node> latents;
    private Graph structureGraph;
    private List<List<Node>> clustering;
    private double alpha = 1e-6;
    private Knowledge knowledge = new Knowledge();
    private ICovarianceMatrix covMatrix;
    private int minimumSize = 3;
    private TetradMatrix latentsCov;
    private double minimum;
    private boolean fixLatentVariances = false;
    private double pValue;

    public Mimbuild3() {
    }

    public Graph search(List<List<Node>> clustering, List<String> names, ICovarianceMatrix _cov) {
        List<List<Node>> _clustering = new ArrayList<List<Node>>();

        for (List<Node> cluster : clustering) {
            List<Node> _cluster = new ArrayList<Node>();

            for (Node node : cluster) {
                _cluster.add(_cov.getVariable(node.getName()));
            }

            _clustering.add(_cluster);
        }

        clustering = _clustering;

        List<Node> latents = defineLatents(clustering, names);

        removeSmallClusters(latents, clustering, minimumSize());
        this.clustering = clustering;

        Node[][] indicators = new Node[latents.size()][];

        for (int i = 0; i < latents.size(); i++) {
            indicators[i] = new Node[clustering.get(i).size()];

            for (int j = 0; j < clustering.get(i).size(); j++) {
                indicators[i][j] = clustering.get(i).get(j);
            }
        }

        TetradMatrix cov = getCov(_cov, latents, indicators);
        CovarianceMatrix covMatrix = new CovarianceMatrix(latents, cov, _cov.getSampleSize());

        // For some reason GES is throwing singularity exceptions but CPC is not.
        Ges patternSearch = new Ges(covMatrix);
        Graph graph = patternSearch.search();
//
//        Cpc patternSearch = new Cpc(new IndTestFisherZ(covMatrix, alpha));
//        Graph graph = patternSearch.search();

        this.latents = latents;
        this.structureGraph = new EdgeListGraph(graph);
        GraphUtils.fruchtermanReingoldLayout(this.structureGraph);

        this.covMatrix = covMatrix;

        return this.structureGraph;
    }

    public int minimumSize() {
        return minimumSize;
    }

    public void setMinimumSize(int minimumSize) {
        this.minimumSize = minimumSize;
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

    private void removeSmallClusters(List<Node> latents, List<List<Node>> clustering, int minimumSize) {
        for (int i = new ArrayList<Node>(latents).size() - 1; i >= 0; i--) {
            if (clustering.get(i).size() < minimumSize) {
                clustering.remove(clustering.get(i));
                latents.remove(latents.get(i));
            }
        }
    }

    private TetradMatrix getCov(ICovarianceMatrix cov, List<Node> latents, Node[][] clusters) {
        if (latents.size() != clusters.length) {
            throw new IllegalArgumentException();
        }

        TetradMatrix measurescov = cov.getMatrix();

        TetradMatrix latentscov = new TetradMatrix(latents.size(), latents.size());

        for (int i = 0; i < latentscov.rows(); i++) {
            for (int j = i; j < latentscov.columns(); j++) {
                if (i == j) latentscov.set(i, j, 1.0);
                else {
                    double v = RandomUtil.getInstance().nextDouble();
                    latentscov.set(i, j, v);
                    latentscov.set(j, i, v);
                }
            }
        }

        double[][] loadings = new double[clusters.length][];

        for (int i = 0; i < clusters.length; i++) {
            loadings[i] = new double[clusters[i].length];
        }

        for (int i = 0; i < clusters.length; i++) {
            loadings[i] = new double[clusters[i].length];

            for (int j = 0; j < clusters[i].length; j++) {
                loadings[i][j] = RandomUtil.getInstance().nextDouble() - .5;
            }
        }

        int[][] indicatorIndices = new int[clusters.length][];
        List<Node> vars = cov.getVariables();

        for (int i = 0; i < clusters.length; i++) {
            indicatorIndices[i] = new int[clusters[i].length];

            for (int j = 0; j < clusters[i].length; j++) {
                indicatorIndices[i][j] = vars.indexOf(clusters[i][j]);
            }
        }

        double[] delta = new double[measurescov.rows()];

        for (int i = 0; i < delta.length; i++) {
            delta[i] = 1;
        }

        int numParams = 0;

        for (int i = 0; i < latentscov.rows(); i++) {
            for (int j = i; j < latentscov.columns(); j++) {
                if (i == j && fixLatentVariances) continue;
                numParams++;
            }
        }

        for (int i = 0; i < clusters.length; i++) {
            numParams += clusters[i].length;
        }

        double[] values = new double[numParams];
        int count = 0;

        for (int i = 0; i < clusters.length; i++) {
            for (int j = i; j < clusters.length; j++) {
                if (i == j && fixLatentVariances) continue;
                values[count++] = latentscov.get(i, j);
            }
        }

        for (int i = 0; i < clusters.length; i++) {
            for (int j = 0; j < clusters[i].length; j++) {
                values[count++] = loadings[i][j];
            }
        }

        // First a fast search over the non-measured-varance parameters.
        Function1 function1 = new Function1(indicatorIndices, measurescov, loadings, latentscov, numParams);
        MultivariateMinimum search2 = new ConjugateGradientSearch();
        minimum = search2.findMinimum(function1, values, 8, 8);

        double[] values2 = new double[delta.length];
        int count2 = 0;

        for (int i = 0; i < delta.length; i++) {
            values2[count2++] = delta[i];
        }

        // Then a slower search for the measured-variance parameters from that starting point...
        Function2 function2 = new Function2(indicatorIndices, measurescov, loadings, latentscov, delta, delta.length);
        MultivariateMinimum search3 = new ConjugateGradientSearch();
        minimum = search3.findMinimum(function2, values2, 8, 8);

        double N = cov.getSampleSize();

        int p = 0;

        for (Node[] nodes : clusters) {
            p += nodes.length;
        }

        int df = (p) * (p + 1) / 2 - numParams;
        double x = (N - 1) * minimum;
        this.pValue = 1.0 - new ChiSquaredDistribution(df).cumulativeProbability(x);

        this.latentsCov = latentscov;
        return latentscov;
    }

    public TetradMatrix getLatentsCov() {
        return latentsCov;
    }

    public List<String> getLatentNames() {
        List<String> latentNames = new ArrayList<String>();

        for (Node node : latents) {
            latentNames.add(node.getName());
        }

        return latentNames;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getpValue() {
        return pValue;
    }

    private class Function1 implements MultivariateFunction {
        private final int[][] indicatorIndices;
        private final TetradMatrix measurescov;
        private final double[][] loadings;
        private final TetradMatrix latentscov;
        private final int numParams;

        public Function1(int[][] indicatorIndices, TetradMatrix measurescov, double[][] loadings, TetradMatrix latentscov, int numParams) {
            this.indicatorIndices = indicatorIndices;
            this.measurescov = measurescov;
            this.loadings = loadings;
            this.latentscov = latentscov;
            this.numParams = numParams;
        }

        @Override
        public double evaluate(double[] values) {
            for (int i = 0; i < values.length; i++) {
                if (Double.isNaN(values[i])) return 10000;
            }

            int count = 0;

            for (int i = 0; i < loadings.length; i++) {
                for (int j = i; j < loadings.length; j++) {
                    if (i == j && fixLatentVariances) continue;
                    latentscov.set(i, j, values[count]);
                    latentscov.set(j, i, values[count]);
                    count++;
                }
            }

            for (int i = 0; i < loadings.length; i++) {
                for (int j = 0; j < loadings[i].length; j++) {
                    loadings[i][j] = values[count];
                    count++;
                }
            }

            return sumOfDifferences(indicatorIndices, measurescov, loadings, latentscov);
        }

        @Override
        public int getNumArguments() {
            return numParams;
        }

        @Override
        public double getLowerBound(int i) {
            return -100;
        }

        @Override
        public double getUpperBound(int i) {
            return 100;
        }

        @Override
        public OrthogonalHints getOrthogonalHints() {
            return OrthogonalHints.Utils.getNull();
        }
    }

    private TetradMatrix impliedCovariance2(int[][] indicatorIndices, double[][] loadings, TetradMatrix cov, TetradMatrix loadingscov,
                                            double[] delta) {
        TetradMatrix implied = new TetradMatrix(cov.rows(), cov.columns());

        for (int i = 0; i < loadings.length; i++) {
            for (int j = 0; j < loadings.length; j++) {
                for (int k = 0; k < loadings[i].length; k++) {
                    for (int l = 0; l < loadings[j].length; l++) {
                        double prod = loadings[i][k] * loadings[j][l] * loadingscov.get(i, j);
                        implied.set(indicatorIndices[i][k], indicatorIndices[j][l], prod);
                    }
                }
            }
        }

        for (int i = 0; i < implied.rows(); i++) {
            implied.set(i, i, implied.get(i, i) + delta[i]);
        }

        return implied;
    }

    private double sumOfDifferences(int[][] indicatorIndices, TetradMatrix cov, double[][] loadings, TetradMatrix loadingscov) {
        double sum = 0;

        for (int i = 0; i < loadings.length; i++) {
            for (int k = 0; k < loadings[i].length; k++) {
                for (int l = k + 1; l < loadings[i].length; l++) {
                    double _cov = cov.get(indicatorIndices[i][k], indicatorIndices[i][l]);
                    double prod = loadings[i][k] * loadings[i][l] * loadingscov.get(i, i);
                    double diff = _cov - prod;
                    sum += diff * diff;
                }
            }
        }

        for (int i = 0; i < loadings.length; i++) {
            for (int j = i + 1; j < loadings.length; j++) {
                for (int k = 0; k < loadings[i].length; k++) {
                    for (int l = 0; l < loadings[j].length; l++) {
                        double _cov = cov.get(indicatorIndices[i][k], indicatorIndices[j][l]);
                        double prod = loadings[i][k] * loadings[j][l] * loadingscov.get(i, j);
                        double diff = _cov - prod;
                        sum += 2 * diff * diff;
                    }
                }
            }
        }

        return sum;
    }

    /**
     * Returns the full discovered graph, with latents and indicators.
     */
    public Graph getFullGraph() {
        Graph graph = new EdgeListGraph(structureGraph);

        for (int i = 0; i < latents.size(); i++) {
            Node latent = latents.get(i);
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

    private class Function2 implements MultivariateFunction {
        private final int[][] indicatorIndices;
        private final TetradMatrix measurescov;
        private TetradMatrix measuresCovInverse;
        private final double[][] loadings;
        private final TetradMatrix latentscov;
        private final int numParams;
        private final double[] delta;
        private final List<Integer> aboveZero = new ArrayList<Integer>();

        public Function2(int[][] indicatorIndices, TetradMatrix measurescov, double[][] loadings, TetradMatrix latentscov,
                         double[] delta, int numParams) {
            this.indicatorIndices = indicatorIndices;
            this.measurescov = measurescov;
            this.loadings = loadings;
            this.latentscov = latentscov;
            this.numParams = numParams;
            this.delta = delta;
            measuresCovInverse = measurescov.inverse();

            int count = 0;

            for (int i = 0; i < delta.length; i++) {
                aboveZero.add(count);
                count++;
            }
        }

        @Override
        public double evaluate(double[] values) {
            for (double v : values) {
                if (Double.isNaN(v)) return 10000;
            }

            int count = 0;

            for (int i = 0; i < delta.length; i++) {
                delta[i] = values[count];
                count++;
            }

            TetradMatrix implied = impliedCovariance2(indicatorIndices, loadings, measurescov, latentscov, delta);

            TetradMatrix I = TetradMatrix.identity(implied.rows());
            TetradMatrix diff = I.minus((implied.times(measuresCovInverse)));

            return 0.5 * (diff.times(diff)).trace();
        }

        @Override
        public int getNumArguments() {
            return numParams;
        }

        @Override
        public double getLowerBound(int i) {
            if (aboveZero.contains(i)) {
                return 0.00;
            }
            return -10;
        }

        @Override
        public double getUpperBound(int i) {
            return 10;
        }

        @Override
        public OrthogonalHints getOrthogonalHints() {
            return OrthogonalHints.Utils.getNull();
        }
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
}

