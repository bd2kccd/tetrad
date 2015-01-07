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

import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.CovarianceMatrix;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.ICovarianceMatrix;
import edu.cmu.tetrad.graph.EdgeListGraph;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;
import edu.cmu.tetrad.util.TetradMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.abs;

/**
 * @author Joseph Ramsey
 */
public final class IndTestMultiFisherZ2 implements IndependenceTest {

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

    public IndTestMultiFisherZ2(Map<Node, List<Node>> nodeMap, CovarianceMatrix cov, double alpha) {
        this.nodeMap = nodeMap;
        this.alpha = alpha;
        this.sampleSize = cov.getSampleSize();
        this.variables = new ArrayList<Node>(nodeMap.keySet());
        this.cov = cov;
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

        int[][] twod = new int[aa.size()][bb.size()];

        List<Node> cc = new ArrayList<Node>();

        for (Node _z : z) {
            cc.addAll(nodeMap.get(_z));
        }

        TetradMatrix submatrix = subMatrix(cov, aa, bb, cc);

        TetradMatrix inverse;
//        int rank;

        try {
            inverse = submatrix.inverse();
//            rank = inverse.columns();
        } catch (Exception e) {
            SingularValueDecomposition svd
                    = new SingularValueDecomposition(submatrix.getRealMatrix());
            RealMatrix _inverse = svd.getSolver().getInverse();
            inverse = new TetradMatrix(_inverse, _inverse.getRowDimension(), _inverse.getColumnDimension());
//            rank = svd.getRank();
        }

        final List<Double> pValues = new ArrayList<Double>();
        List<Integer> _i = new ArrayList<Integer>();
        List<Integer> _m = new ArrayList<Integer>();

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - inverse.columns();

                if (dof < 0) {
                    dof = 0;
                }

                double z_ = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z_)));

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

        // Sort pvalues, _i, and _m together.
        List<Double> pValues2 = new ArrayList<Double>();
        List<Integer> _i2 = new ArrayList<Integer>();
        List<Integer> _m2 = new ArrayList<Integer>();

        for (int _y = 0; _y < indices.size(); _y++) {
            pValues2.add(pValues.get(indices.get(_y)));
            _i2.add(_i.get(indices.get(_y)));
            _m2.add(_m.get(indices.get(_y)));
        }

        Collections.sort(pValues2);

        int k = StatUtils.fdr(alpha, pValues, false);

        int nonzero = -1;

        for (int i = 0; i < pValues2.size(); i++) {
            if (pValues.get(i) != 0) {
                nonzero = i;
                break;
            }
        }

        if (nonzero < k) {
            for (int g = 0; g < k; g++) {
                int x3 = _i2.get(g);
                int y3 = _m2.get(g);
                twod[x3][y3] = 1;
            }

//            if (verbose) {
//                System.out.println("Dependent");
//            }

            return false;
        }
        else {
            if (verbose) {
                System.out.println("Independent");
            }

            return true;
        }
    }

//    public boolean isIndependent(Node x, Node y, List<Node> z) {
//        if (verbose) {
//            System.out.println("\n" + x + " _||_ " + y + " | " + z);
//        }
//
//        List<Node> AA = nodeMap.get(x);
//        List<Node> BB = nodeMap.get(y);
//
//        List<Node> CC = new ArrayList<Node>();
//
//        for (Node _z : z) {
//            CC.addAll(nodeMap.get(_z));
//        }
//
//        return indepCollection(AA, BB, CC, sampleSize, alpha);
//    }

    private boolean indepCollection(List<Node> aa, List<Node> bb, List<Node> cc, int n, double alpha) {
//        List<Double> ret = getCutoff1(aa, bb, cc);
//        List<Double> ret = getCutoff2(aa, bb, cc);
//        double mean = ret.get(0);
//        double sd = ret.get(1);

        int numPerm = 10;

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

        List<Double> pValues = new ArrayList<Double>();

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = n - 1 - rank;

                if (dof < 0) {
                    System.out.println("Negative dof: " + dof + " n = " + n + " cols = " + inverse.columns());
                    dof = 0;
                }

                double z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z)));

                pValues.add(p);
            }
        }

        List<Double> zeroes = new ArrayList<Double>();

        for (double d : pValues) {
            if (d == 0) zeroes.add(d);
        }

        int k = 0;

        for (double p : pValues) {
            if (p < alpha) {
                k++;
            }
        }

        numTests++;

        int count = countGreater(aa, bb, cc, numPerm, k);
//        int count = countGreater2(aa, bb, cc, numPerm, k);
//        int count = countGreater3(aa, bb, cc, numPerm, k, submatrix, inverse);
//        int count = countGreater4(aa, bb, cc, numPerm, k);

        double p = count / (double) numPerm;

        boolean indep = p > alpha;

//        double p = (1.0 - RandomUtil.getInstance().normalCdf(0, 1, (k - mean) / sd));

//        boolean indep = p > alpha;

        if (verbose) {
//            System.out.println("\n#accepted " + k + " cutoff = " + kCutoff + " indep = " + indep);
//            System.out.println("\n#accepted " + k + " meam = " + mean + " sd = " + sd + " indep = " + indep);
//            System.out.println("standard deviations " + (k - mean) / sd + " p = " + p + " alpha = " + alpha);
            System.out.println("\n#accepted " + k + " indep = " + indep);
            System.out.println("p = " + p + " alpha = " + alpha);
        }

        numTests++;
        if (!indep) numDependent++;

        return indep;

    }

    private boolean indepCollection2(List<Node> aa, List<Node> bb, List<Node> cc, int n, double alpha) {
//        List<Double> ret = getCutoff1(aa, bb, cc);
//        List<Double> ret = getCutoff2(aa, bb, cc);
//        double mean = ret.get(0);
//        double sd = ret.get(1);

        int numPerm = 10;

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

        List<Double> pValues = new ArrayList<Double>();

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = n - 1 - rank;

                if (dof < 0) {
                    System.out.println("Negative dof: " + dof + " n = " + n + " cols = " + inverse.columns());
                    dof = 0;
                }

                double z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z)));

                pValues.add(p);
            }
        }

        List<Double> zeroes = new ArrayList<Double>();

        for (double d : pValues) {
            if (d == 0) zeroes.add(d);
        }

        int k = 0;

        for (double p : pValues) {
            if (p < alpha) {
                k++;
            }
        }

        numTests++;

        int count = countGreater(aa, bb, cc, numPerm, k);
//        int count = countGreater2(aa, bb, cc, numPerm, k);

        double p = count / (double) numPerm;

        boolean indep = p > alpha;

//        double p = (1.0 - RandomUtil.getInstance().normalCdf(0, 1, (k - mean) / sd));

//        boolean indep = p > alpha;

        if (verbose) {
//            System.out.println("\n#accepted " + k + " cutoff = " + kCutoff + " indep = " + indep);
//            System.out.println("\n#accepted " + k + " meam = " + mean + " sd = " + sd + " indep = " + indep);
//            System.out.println("standard deviations " + (k - mean) / sd + " p = " + p + " alpha = " + alpha);
            System.out.println("\n#accepted " + k + " indep = " + indep);
            System.out.println("p = " + p + " alpha = " + alpha);
        }

        numTests++;
        if (!indep) numDependent++;

        return indep;

    }


    private List<Double> getCutoff1(List<Node> aa, List<Node> bb, List<Node> cc) {
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");
        Node z = new ContinuousVariable("Z");

        int numPermutations = 5;
        double[] k_ = new double[numPermutations];

        for (int c = 0; c < numPermutations; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            for (int j = 0; j < cov.getDimension(); j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> _nodes = cov.getVariables();
            int _count = 0;

            List<Node> nx = new ArrayList<Node>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(x, nx);

            List<Node> ny = new ArrayList<Node>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(y, ny);

            List<Node> nz = new ArrayList<Node>();

            for (int k = 0; k < cc.size(); k++) {
                nz.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(z, nz);

            IndTestMultiFisherZ2 test = new IndTestMultiFisherZ2(nodeMap, cov, alpha);
            test.setVerbose(true);

            TetradMatrix submatrix = subMatrix(cov, nx, ny, nz);
            TetradMatrix inverse;
            int rank;

            try {
                inverse = submatrix.inverse();
                rank = inverse.columns();
            } catch (Exception e) {
                System.out.println("Couldn't invert " + submatrix.columns());
                throw new IllegalArgumentException();
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i = 0; i < nx.size(); i++) {
                for (int m = 0; m < ny.size(); m++) {
                    int j = nx.size() + m;
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - rank;

                    if (dof < 0) {
                        System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                        dof = 0;
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            k_[c] = k;
        }

        double mean = StatUtils.mean(k_);
        double sd = StatUtils.sd(k_);

        List<Double> ret = new ArrayList<Double>();
        ret.add(mean);
        ret.add(sd);

        return ret;
    }

    private int countGreater(List<Node> aa, List<Node> bb, List<Node> cc, int perm, int obs) {
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");
        Node z = new ContinuousVariable("Z");

        int count = 0;

        int numPermutations = perm;
        double[] k_ = new double[numPermutations];

        for (int c = 0; c < numPermutations; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            for (int j = 0; j < cov.getDimension(); j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> _nodes = cov.getVariables();
            int _count = 0;

            List<Node> nx = new ArrayList<Node>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(x, nx);

            List<Node> ny = new ArrayList<Node>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(y, ny);

            List<Node> nz = new ArrayList<Node>();

            for (int k = 0; k < cc.size(); k++) {
                nz.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(z, nz);

            IndTestMultiFisherZ2 test = new IndTestMultiFisherZ2(nodeMap, cov, alpha);
            test.setVerbose(true);

            TetradMatrix submatrix = subMatrix(cov, nx, ny, nz);
            TetradMatrix inverse;

            try {
                inverse = submatrix.inverse();
            } catch (Exception e) {
                System.out.println("Couldn't invert " + submatrix.columns());
                throw new IllegalArgumentException();
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i = 0; i < nx.size(); i++) {
                for (int m = 0; m < ny.size(); m++) {
                    int j = nx.size() + m;
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - inverse.columns();

                    if (dof < 0) {
                        System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                        dof = 0;
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;
//            double alpha = 0.2;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            k_[c] = k;

//            System.out.println("k_[c] = " + k_[c] + " obs = " + obs);

            if (k_[c] > obs) {
                count++;
            }
        }

        double mean = StatUtils.mean(k_);
        double sd = StatUtils.sd(k_);

        List<Double> ret = new ArrayList<Double>();
        ret.add(mean);
        ret.add(sd);

        return count;
    }

    private List<Double> getCutoff2(List<Node> aa, List<Node> bb, List<Node> cc) {
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");
        Node z = new ContinuousVariable("Z");

        List<Node> all = new ArrayList<Node>();

        for (int i = 0; i < aa.size() + bb.size() + cc.size(); i++) {
            all.add(new ContinuousVariable("X" + i));
        }

        Graph graph = new EdgeListGraph(all);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);

        for (int i = 0; i < aa.size() + bb.size() + cc.size(); i++) {
            double var = this.cov.getValue(i, i);
            Node semNode = all.get(i);
            im.setErrCovar(semNode, var);
        }

        DataSet data = im.simulateData(cov.getSampleSize(), false);
        CovarianceMatrix cov = new CovarianceMatrix(data);

        double[] k_ = new double[5];

        for (int c = 0; c < 5; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            for (int j = 0; j < cov.getDimension(); j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> _nodes = cov.getVariables();
            int _count = 0;

            List<Node> nx = new ArrayList<Node>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(x, nx);

            List<Node> ny = new ArrayList<Node>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(y, ny);

            List<Node> nz = new ArrayList<Node>();

            for (int k = 0; k < cc.size(); k++) {
                nz.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(z, nz);

            TetradMatrix submatrix = subMatrix(cov, nx, ny, nz);
            TetradMatrix inverse;
            int rank;

            try {
                inverse = submatrix.inverse();
                rank = inverse.columns();
            } catch (Exception e) {
                System.out.println("Couldn't invert " + submatrix.columns());
                throw new IllegalArgumentException();
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i = 0; i < nx.size(); i++) {
                for (int m = 0; m < ny.size(); m++) {
                    int j = nx.size() + m;
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - rank;

                    if (dof < 0) {
                        System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                        dof = 0;
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            k_[c] = k;
        }

        double mean = StatUtils.mean(k_);
        double sd = StatUtils.sd(k_);

        List<Double> ret = new ArrayList<Double>();
        ret.add(mean);
        ret.add(sd);

        return ret;
    }

    private int  countGreater2(List<Node> aa, List<Node> bb, List<Node> cc, int perm, double obs) {
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");
        Node z = new ContinuousVariable("Z");

        int count = 0;

        List<Node> all = new ArrayList<Node>();

        for (int i = 0; i < aa.size() + bb.size() + cc.size(); i++) {
            all.add(new ContinuousVariable("X" + i));
        }

        Graph graph = new EdgeListGraph(all);
        SemPm pm = new SemPm(graph);
        SemIm im = new SemIm(pm);

        for (int i = 0; i < aa.size() + bb.size() + cc.size(); i++) {
            double var = this.cov.getValue(i, i);
            Node semNode = all.get(i);
            im.setErrCovar(semNode, var);
        }

        DataSet data = im.simulateData(cov.getSampleSize(), false);
        CovarianceMatrix cov = new CovarianceMatrix(data);

        double[] k_ = new double[perm];

        for (int c = 0; c < perm; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            for (int j = 0; j < cov.getDimension(); j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> _nodes = cov.getVariables();
            int _count = 0;

            List<Node> nx = new ArrayList<Node>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(x, nx);

            List<Node> ny = new ArrayList<Node>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(y, ny);

            List<Node> nz = new ArrayList<Node>();

            for (int k = 0; k < cc.size(); k++) {
                nz.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(z, nz);

            TetradMatrix submatrix = subMatrix(cov, nx, ny, nz);
            TetradMatrix inverse;
            int rank;

            try {
                inverse = submatrix.inverse();
                rank = inverse.columns();
            } catch (Exception e) {
                System.out.println("Couldn't invert " + submatrix.columns());
                throw new IllegalArgumentException();
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i = 0; i < nx.size(); i++) {
                for (int m = 0; m < ny.size(); m++) {
                    int j = nx.size() + m;
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - rank;

                    if (dof < 0) {
                        System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                        dof = 0;
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            k_[c] = k;

//            System.out.println(k + " " + obs);

            if (k_[c] > obs) {
                count++;
            }
        }

        return count;
    }

    private int countGreater3(List<Node> aa, List<Node> bb, List<Node> cc, int perm, int obs, TetradMatrix submatrix,
                              TetradMatrix inverse) {
        List<Integer> nx2 = new ArrayList<Integer>();
        int count2 = 0;

        for (int k = 0; k < aa.size(); k++) {
            nx2.add(count2++);
        }

        List<Integer> ny2 = new ArrayList<Integer>();

        for (int k = 0; k < bb.size(); k++) {
            ny2.add(count2++);
        }

        List<Double> p2 = new ArrayList<Double>();

        for (int i : nx2) {
            for (int j : ny2) {
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - submatrix.columns();

                if (dof < 0) {
                    throw new IllegalArgumentException("Negative dof: " + dof + " n = " + cov.getSampleSize() +
                            " cols = " + inverse.columns());
                }

                double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                p2.add(p);
            }
        }

        int obs2 = 0;

        for (double p : p2) {
            if (p < alpha) obs2++;
        }

        int count = 0;
        double[] K = new double[perm];

        for (int c = 0; c < perm; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            for (int j = 0; j < submatrix.columns(); j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            int _count = 0;

            List<Integer> nx = new ArrayList<Integer>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(indices.get(_count++));
            }

            List<Integer> ny = new ArrayList<Integer>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(indices.get(_count++));
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i : nx) {
                for (int j : ny) {
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - submatrix.columns();

                    if (dof < 0) {
                        throw new IllegalArgumentException("Negative dof: " + dof + " n = " + cov.getSampleSize() +
                                " cols = " + inverse.columns());
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            K[c] = k;

            System.out.println(k + " >? " + obs2);

            if (K[c] > obs2) {
                count++;
            }
        }

        return count;
    }

    private int countGreater4(List<Node> aa, List<Node> bb, List<Node> cc, int perm, double obs) {
        Node x = new ContinuousVariable("X");
        Node y = new ContinuousVariable("Y");
        Node z = new ContinuousVariable("Z");

        int count = 0;

        int numPermutations = perm;
        double[] k_ = new double[numPermutations];

        for (int c = 0; c < numPermutations; c++) {
            List<Integer> indices = new ArrayList<Integer>();

            int numVars = aa.size() + bb.size() + cc.size();

            for (int j = 0; j < numVars; j++) {
                indices.add(j);
            }

            Collections.shuffle(indices);

            Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
            List<Node> _nodes = cov.getVariables();
            int _count = 0;

            List<Node> nx = new ArrayList<Node>();

            for (int k = 0; k < aa.size(); k++) {
                nx.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(x, nx);

            List<Node> ny = new ArrayList<Node>();

            for (int k = 0; k < bb.size(); k++) {
                ny.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(y, ny);

            List<Node> nz = new ArrayList<Node>();

            for (int k = 0; k < cc.size(); k++) {
                nz.add(_nodes.get(indices.get(_count++)));
            }

            nodeMap.put(z, nz);

            TetradMatrix submatrix = subMatrix(cov, nx, ny, nz);
            TetradMatrix inverse;
            int rank;

            try {
                inverse = submatrix.inverse();
                rank = inverse.columns();
            } catch (Exception e) {
                System.out.println("Couldn't invert " + submatrix.columns());
                throw new IllegalArgumentException();
            }

            List<Double> pValues = new ArrayList<Double>();

            for (int i = 0; i < nx.size(); i++) {
                for (int m = 0; m < ny.size(); m++) {
                    int j = nx.size() + m;
                    double a = -1.0 * inverse.get(i, j);
                    double v0 = inverse.get(i, i);
                    double v1 = inverse.get(j, j);
                    double b = Math.sqrt(v0 * v1);

                    double r = a / b;

                    int dof = cov.getSampleSize() - 1 - rank;

                    if (dof < 0) {
                        System.out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                        dof = 0;
                    }

                    double _z = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                    double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(_z)));

                    pValues.add(p);
                }
            }

            int k = 0;

            for (double p : pValues) {
                if (p < alpha) k++;
            }

            k_[c] = k;

            System.out.println(k + " " + obs);

            if (k_[c] > obs) {
                count++;
            }
        }

        double mean = StatUtils.mean(k_);
        double sd = StatUtils.sd(k_);

        List<Double> ret = new ArrayList<Double>();
        ret.add(mean);
        ret.add(sd);

        return count;
    }


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



