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
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.*;
import edu.cmu.tetrad.util.dist.ChiSquare;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.RealMatrix;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import org.apache.commons.math3.linear.*;

/**
 * @author Joseph Ramsey
 */
public final class IndTestMultiFisherZ implements IndependenceTest {

    private final DataSet stdData;
    /**
     * The variables of the covariance matrix, in order. (Unmodifiable list.)
     */
    private List<Node> variables;

    /**
     * The significance level of the independence tests.
     */
    private double alpha;

    /**
     * The value of the Fisher's Z statistic associated with the las calculated partial correlation.
     */
    private double pValue;

    /**
     * Formats as 0.0000.
     */
    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    /**
     * Stores a reference to the dataset being analyzed.
     */
    private DataSet dataSet;

    /**
     * A map from metavariables to detail variables.
     */
    private Map<Node, List<Node>> nodeMap;

    /**
     * Obvious.
     */
    private int sampleSize;

    /**
     * Covariance matrix over all of the compared variables (or more).
     */
    CovarianceMatrix cov;

    private boolean verbose;

    int numDependent = 0;
    int numTests = 0;
    List<Double> gValues = new ArrayList<Double>();

    //==========================CONSTRUCTORS=============================//

    public IndTestMultiFisherZ(Map<Node, List<Node>> nodeMap, CovarianceMatrix cov, double alpha, DataSet dataSet) {
        this.nodeMap = nodeMap;
        this.alpha = alpha;
        this.sampleSize = cov.getSampleSize();
        this.variables = new ArrayList<Node>(nodeMap.keySet());
        this.cov = cov;
        this.stdData = DataUtils.standardizeData(dataSet);
    }

    //==========================PUBLIC METHODS=============================//

    /**
     * Creates a new independence test instance for a subset of the variables.
     */
    public IndependenceTest indTestSubset(List<Node> vars) {
        throw new UnsupportedOperationException();
    }

    public boolean isIndependent(Node x, Node y, List<Node> z) {
        if (verbose) {
            System.out.println("\n" + x + " _||_ " + y + " | " + z);
        }

        List<Node> aa = nodeMap.get(x);
        List<Node> bb = nodeMap.get(y);

        List<Node> cc = new ArrayList<Node>();

        for (Node _z : z) {
            cc.addAll(nodeMap.get(_z));
        }

        TetradMatrix submatrix = subMatrix(cov, aa, bb, cc);

        TetradMatrix inverse;
        int rank;

        try {
            inverse = submatrix.inverse();
            rank = inverse.columns();
        } catch (Exception e) {
            SingularValueDecomposition svd
                    = new SingularValueDecomposition(submatrix.getRealMatrix());
            RealMatrix _inverse = svd.getSolver().getInverse();
            inverse = new TetradMatrix(_inverse, _inverse.getRowDimension(), _inverse.getColumnDimension());
            rank = svd.getRank();
        }

        final List<Double> pValues = new ArrayList<Double>();
        List<Integer> _i = new ArrayList<Integer>();
        List<Integer> _m = new ArrayList<Integer>();

        TetradMatrix stdSubmatrix = TestHippocampusUtils.subset(stdData, aa, bb, cc);

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - rank;

                if (dof < 0) {
//                    out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                    dof = 0;
                }

                double t2 = moment22(stdSubmatrix, i, j);

                double z_ = sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, sqrt(t2), abs(z_)));

//                if (p < alpha) p = 0;

//                System.out.println("z = " + z_ + " p = " + p);

                pValues.add(p);
                _i.add(i);
                _m.add(m);
            }
        }

        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < pValues.size(); i++) {
            indices.add(i);
        }

        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return pValues.get(o1).compareTo(pValues.get(o2));
            }
        });

        List<Double> pValues2 = new ArrayList<Double>();
        List<Integer> _iIndices = new ArrayList<Integer>();
        List<Integer> _mIndices = new ArrayList<Integer>();

        for (int _y = 0; _y < indices.size(); _y++) {
            pValues2.add(pValues.get(indices.get(_y)));
            _iIndices.add(_i.get(indices.get(_y)));
            _mIndices.add(_m.get(indices.get(_y)));
        }

        int k = StatUtils.fdr(alpha, pValues2, false);
        double cutoff = StatUtils.fdrCutoff(alpha, pValues2, false);
        System.out.println("** cutoff = " + cutoff);

        int nonzero = -1;

        for (int i = 0; i < pValues2.size(); i++) {
            if (pValues2.get(i) != 0) {
                nonzero = i;
                break;
            }
        }

        double q = StatUtils.fdrQ(pValues2, nonzero);
        System.out.println("Q = " + q);

//        boolean independent = q >= alpha;

////        boolean independent = k > nonzero;
//
        System.out.println("k = " + k);
        System.out.println("nonzero = " + nonzero);
        System.out.println("fisher = " + fisherMethodP(pValues2));
//
//        boolean independent = k <= nonzero;

        double ratio = ((double) k) / pValues2.size();
        System.out.println("ratio = " + ratio);
        boolean independent = ratio < 0.1;
//
        System.out.println(independent ? "Independent" : "Dependent");

        return independent;
    }

    public static double fisherMethodP(List<Double> p) {
        double tf = 0;
        int count = 0;

        for (double _p : p) {
            if (_p > 0) {
                tf += -2.0 * Math.log(_p);
                count++;
            }
        }
        int df = 2 * count;

        double chidist = new ChiSquaredDistribution(df).cumulativeProbability(tf);

//        double chidist = ProbUtils.chisqCdf(tf, df);
        return 1.0 - chidist;
    }

    private double moment22(TetradMatrix m, int i, int j) {
        int N = m.rows();
        double sum = 0.0;

        for (int k = 0; k < N; k++) {
            double v = m.get(k, i);
            double w = m.get(k, j);
            sum += v * v * w * w;
        }

        return sum / N;
    }

//    public boolean isIndependent(Node x, Node y, List<Node> z) {
//        if (verbose) {
//            System.out.println("\n" + x + " _||_ " + y + " | " + z);
//        }
//
//        List<Node> aa = nodeMap.get(x);
//        List<Node> bb = nodeMap.get(y);
//
//        List<Node> cc = new ArrayList<Node>();
//
//        for (Node _z : z) {
//            cc.addAll(nodeMap.get(_z));
//        }
//
//        TetradMatrix submatrix = subMatrix(cov, aa, bb, cc);
//
//        TetradMatrix inverse;
//        int rank;
//
//        try {
//            inverse = submatrix.inverse();
//            rank = inverse.columns();
//        } catch (Exception e) {
//            SingularValueDecomposition svd
//                    = new SingularValueDecomposition(submatrix.getRealMatrix());
//            RealMatrix _inverse = svd.getSolver().getInverse();
//            inverse = new TetradMatrix(_inverse, _inverse.getRowDimension(), _inverse.getColumnDimension());
//            rank = svd.getRank();
//        }
//
//        List<Double> pValues = new ArrayList<Double>();
//
//        for (int i = 0; i < aa.size(); i++) {
//            for (int m = 0; m < bb.size(); m++) {
//                int j = aa.size() + m;
//                double a = -1.0 * inverse.get(i, j);
//                double v0 = inverse.get(i, i);
//                double v1 = inverse.get(j, j);
//                double b = Math.sqrt(v0 * v1);
//
//                double r = a / b;
//
//                int dof = cov.getSampleSize() - 1 - rank;
//
//                if (dof < 0) {
//                    System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
//                    dof = 0;
//                }
//
//                double z_ = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
//                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z_)));
//
//                pValues.add(p);
//            }
//        }
//
//        Collections.sort(pValues);
//
//        int k = StatUtils.fdr(alpha, pValues, false);
//
//        int nonzero = -1;
//
//        for (int i = 0; i < pValues.size(); i++) {
//            if (pValues.get(i) != 0) {
//                nonzero = i;
//                break;
//            }
//        }
//
//        if (k > 0.005 * (aa.size() * bb.size())) {
//            if (verbose) {
//                System.out.println("Dependent");
//            }
//            return false;
//        }
//        else {
//            if (verbose) {
//                System.out.println("Independent");
//            }
//            return true;
//        }
//    }


    /**
     * Returns the submatrix of m with variables in the order of the x variables.
     */
    public static TetradMatrix subMatrix(ICovarianceMatrix m, List<Node> x, List<Node> y, List<Node> z) {
        List<Node> variables = m.getVariables();
        TetradMatrix _covMatrix = m.getMatrix();

        // Create index array for the given variables.
        int[] indices = new int[x.size() + y.size() + z.size()];

        for (int i = 0; i < x.size(); i++) {
            indices[i] = variables.indexOf(x.get(i));
        }

        for (int i = 0; i < y.size(); i++) {
            indices[x.size() + i] = variables.indexOf(y.get(i));
        }

        for (int i = 0; i < z.size(); i++) {
            indices[x.size() + y.size() + i] = variables.indexOf(z.get(i));
        }

        // Extract submatrix of correlation matrix using this index array.
        TetradMatrix submatrix = _covMatrix.getSelection(indices, indices);

        return submatrix;
    }

    public boolean isIndependent(Node x, Node y, Node... z) {
        return isIndependent(x, y, Arrays.asList(z));
    }

    public boolean isDependent(Node x, Node y, List<Node> z) {
        return !isIndependent(x, y, z);
    }

    public boolean isDependent(Node x, Node y, Node... z) {
        List<Node> zList = Arrays.asList(z);
        return isDependent(x, y, zList);
    }

    /**
     * Returns the probability associated with the most recently computed independence test.
     */
    public double getPValue() {
        return pValue;
    }

    /**
     * Sets the significance level at which independence judgments should be made.  Affects the cutoff for partial
     * correlations to be considered statistically equal to zero.
     */
    public void setAlpha(double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Significance out of range.");
        }

        this.alpha = alpha;
    }

    /**
     * Gets the getModel significance level.
     */
    public double getAlpha() {
        return this.alpha;
    }

    public double gAvg() {
        double[] _gValues = new double[gValues.size()];
        for (int i = 0; i < gValues.size(); i++) _gValues[i] = gValues.get(i);
        double mean = StatUtils.mean(_gValues);
        return mean;
    }

    public double gMedian() {
        double[] _gValues = new double[gValues.size()];
        for (int i = 0; i < gValues.size(); i++) _gValues[i] = gValues.get(i);
        double median = StatUtils.median(_gValues);
        return median;
    }

    public double gMin() {
        Collections.sort(gValues);
        return gValues.get(0);
    }

    public double gMax() {
        Collections.sort(gValues);
        return gValues.get(gValues.size() - 1);
    }

    public double percentDependent() {
        return numDependent / (double) numTests;
    }

    public List<Double> gValues() {
        return gValues;
    }

    /**
     * Returns the list of variables over which this independence checker is capable of determinine independence
     * relations-- that is, all the variables in the given graph or the given data set.
     */
    public List<Node> getVariables() {
        return this.variables;
    }

    /**
     * Returns the variable with the given name.
     */
    public Node getVariable(String name) {
        for (Node node : nodeMap.keySet()) {
            if (node.getName().equals(name)) return node;
        }

        throw new IllegalArgumentException();
    }

    /**
     * Returns the list of variable varNames.
     */
    public List<String> getVariableNames() {
        List<Node> variables = getVariables();
        List<String> variableNames = new ArrayList<String>();
        for (Node variable1 : variables) {
            variableNames.add(variable1.getName());
        }
        return variableNames;
    }

    /**
     * If <code>isDeterminismAllowed()</code>, deters to IndTestFisherZD; otherwise throws
     * UnsupportedOperationException.
     */
    public boolean determines(List<Node> z, Node x) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the data set being analyzed.
     */
    public DataSet getData() {
        return dataSet;
    }

    public void shuffleVariables() {
        ArrayList<Node> nodes = new ArrayList<Node>(this.variables);
        Collections.shuffle(nodes);
        this.variables = Collections.unmodifiableList(nodes);
    }

    /**
     * Returns a string representation of this test.
     */
    public String toString() {
        return "Fisher's Z, alpha = " + nf.format(getAlpha());
    }

    //==========================PRIVATE METHODS============================//

    private Map<String, Node> mapNames(List<Node> variables) {
        Map<String, Node> nameMap = new ConcurrentHashMap<String, Node>();

        for (Node node : variables) {
            nameMap.put(node.getName(), node);
        }

        return nameMap;
    }

    private Map<Node, Integer> indexMap(List<Node> variables) {
        Map<Node, Integer> indexMap = new ConcurrentHashMap<Node, Integer>();

        for (int i = 0; i < variables.size(); i++) {
            indexMap.put(variables.get(i), i);
        }

        return indexMap;
    }

    public ICovarianceMatrix getCov() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DataSet> getDataSets() {

        List<DataSet> dataSets = new ArrayList<DataSet>();

        dataSets.add(dataSet);

        return dataSets;
    }

    @Override
    public int getSampleSize() {
        return sampleSize;
    }

    @Override
    public List<TetradMatrix> getCovMatrices() {
        return null;
    }


    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}



