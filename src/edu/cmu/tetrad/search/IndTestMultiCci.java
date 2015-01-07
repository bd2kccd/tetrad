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
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.util.*;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.abs;

/**
 * @author Joseph Ramsey
 */
public final class IndTestMultiCci implements IndependenceTest {

    private PrintWriter out = null;
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
    private final Map<Node, TetradMatrix> coords;
    private List<int[][][]> all3D = new ArrayList<int[][][]>();
    private int threshold = 7;

    //==========================CONSTRUCTORS=============================//

    public IndTestMultiCci(Map<Node, List<Node>> nodeMap, Map<Node, TetradMatrix> coords, CovarianceMatrix cov, DataSet data, double alpha) {
        this.nodeMap = nodeMap;
        this.alpha = alpha;
        this.sampleSize = cov.getSampleSize();
        this.variables = new ArrayList<Node>(nodeMap.keySet());
        this.cov = cov;
        this.dataSet = data;
        this.coords = coords;

        File file = new File("/Users/josephramsey/Documents/LAB_NOTEBOOK.2012.04.20/2014.02.20/hippocampus/multifisherzdump.txt");

        try {
            out = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            out.println("\n" + x + " _||_ " + y + " | " + z);
        }

        List<Node> aa = nodeMap.get(x);
        List<Node> bb = nodeMap.get(y);

//        int[][] twod = new int[aa.size()][bb.size()];

        List<Node> cc = new ArrayList<Node>();

        for (Node _z : z) {
            cc.addAll(nodeMap.get(_z));
        }

        TetradMatrix submatrix = TestHippocampusUtils.subMatrix(cov, aa, bb, cc);

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

        System.out.println("# voxels for " + x.getName() + " = " + aa.size());
        System.out.println("# voxels for " + y.getName() + " = " + bb.size());
        System.out.println("# p values = " + aa.size() * bb.size());

        IndTestConditionalCorrelation cciTest = new IndTestConditionalCorrelation(dataSet, alpha);

        for (int i = 0; i < aa.size(); i++) {
            for (int m = 0; m < bb.size(); m++) {
                int j = aa.size() + m;
                double a = -1.0 * inverse.get(i, j);
                double v0 = inverse.get(i, i);
                double v1 = inverse.get(j, j);
                double b = Math.sqrt(v0 * v1);

                double r = a / b;

                int dof = cov.getSampleSize() - 1 - rank;

                if (dof < 0) {
                    out.println("Negative dof: " + dof + " n = " + cov.getSampleSize() + " cols = " + inverse.columns());
                    dof = 0;
                }

                double z_ = Math.sqrt(dof) * 0.5 * (Math.log(1.0 + r) - Math.log(1.0 - r));
                double p = 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, abs(z_)));

                cciTest.isIndependent(aa.get(i), bb.get(m), cc);
                pValues.add(p);

                if (m == 0) {
                    System.out.println("i = " + i + " m = " + m + " p = " + p);
                }

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

        int nonzero = -1;

        for (int i = 0; i < pValues2.size(); i++) {
            if (pValues2.get(i) != 0) {
                nonzero = i;
                break;
            }
        }

        boolean dependent = k > nonzero;
        boolean independent = !dependent;

        TetradMatrix xCoords = coords.get(x);
        TetradMatrix yCoords = coords.get(y);

        int[][][] X = TestHippocampusUtils.threeDView(_iIndices, k, xCoords, independent, nonzero);
        int[][][] Y = TestHippocampusUtils.threeDView(_mIndices, k, yCoords, independent, nonzero);

        all3D.add(X);
        all3D.add(Y);

        String fact = SearchLogUtils.independenceFact(x, y, z);

        // Printing out stuff for Ruben. First print out dependent voxels.

//        2.Second file, contain a list of all the dependencies between voxels.
//
//        10 -- 50
//        30 -- 2

        int[][][] Cx = getC(X);
        int[][][] Cy = getC(Y);

        out.println("\n\n" + fact);

        for (int g = independent ? nonzero : 0; g < k; g++) {
            int i = getIndex(_iIndices, Cx, g, coords.get(x));
            int j = getIndex(_mIndices, Cy, g, coords.get(y));

            if (i == -1 || j == -1) throw new IllegalArgumentException();

            out.println(i + " -- " + j);
        }

        out.println();


//        1. First file, containing info of both ROIs and all their voxels.
//        Example:
//
//        ROI_LABEL  voxel_LABEL  COORDINATES  #Dependencies
//        ENT          10         -80 50 38     6
//        CA1          50         -70 15 90     2

        printDependencies(x, fact, X, Cx);
        printDependencies(y, fact, Y, Cy);


        // OK back to work.
        int xCount = countAboveThreshold(X, threshold);
        int yCount = countAboveThreshold(Y, threshold);

        System.out.println("Total above threshold count = " + (xCount + yCount));
        out.println("Total above threshold count = " + (xCount + yCount));

        boolean thresholdIndep = !(xCount > 0 && yCount > 0);

        String projection;

        projection = "Axial";

        TestHippocampusUtils.printChart(X, xCoords, 0, 1, x.getName(), projection, fact, false, false, out, threshold);
        TestHippocampusUtils.printChart(Y, yCoords, 0, 1, y.getName(), projection, fact, false, false, out, threshold);

        projection = "Coronal";

        TestHippocampusUtils.printChart(X, xCoords, 0, 2, x.getName(), projection, fact, true, false, out, threshold);
        TestHippocampusUtils.printChart(Y, yCoords, 0, 2, y.getName(), projection, fact, true, false, out, threshold);

        projection = "Saggital";

        TestHippocampusUtils.printChart(X, xCoords, 1, 2, x.getName(), projection, fact, true, false, out, threshold);
        TestHippocampusUtils.printChart(Y, yCoords, 1, 2, y.getName(), projection, fact, true, false, out, threshold);

        if (thresholdIndep) {
//        if (independent) {
            if (verbose) {
                System.out.println("Independent");
                out.println("Independent");
            }

            out.flush();
            return true;
        } else {
            if (verbose) {
                System.out.println("Dependent\n");
                out.println("Dependent\n");
            }

            out.flush();
            return false;
        }
    }

    private int getIndex(List<Integer> iIndices, int[][][] C, int g, TetradMatrix coords) {
        int min0 = TestHippocampusUtils.min(coords, 0);
        int min1 = TestHippocampusUtils.min(coords, 1);
        int min2 = TestHippocampusUtils.min(coords, 2);
        TetradVector coord = coords.getRow(iIndices.get(g));
        return C[(int) coord.get(0) - min0][(int) coord.get(1) - min1][(int) coord.get(2) - min2];
    }

    private void printDependencies(Node nx, String fact, int[][][] X, int[][][] C) {

        out.println("\n\n" + fact);
//        1. First file, containing info of both ROIs and all their voxels.
//        Example:
//
//        ROI_LABEL  voxel_LABEL  COORDINATES  #Dependencies
//        ENT          10         -80 50 38     6
//        CA1          50         -70 15 90     2

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] != -1) {
                        out.println(nx.getName() + "\t" + C[x][y][z] + "\t" + x + "\t" + y + "\t" + z + "\t" + X[x][y][z]);
                    }
                }
            }
        }

        out.println();
    }

    private int[][][] getC(int[][][] X) {
        int[][][] C = new int[X.length][X[0].length][X[0][0].length];

        int index = 0;

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] != -1) {
                        C[x][y][z] = index++;
                    }
                    else {
                        C[x][y][z] = -1;
                    }
                }
            }
        }

        return C;
    }

    private int countAboveThreshold(int[][][] X, int threshold) {
        int count = 0;

        for (int x = 0; x < X.length; x++) {
            for (int y = 0; y < X[0].length; y++) {
                for (int z = 0; z < X[0][0].length; z++) {
                    if (X[x][y][z] >= threshold) {
                        count++;
                    }
                }
            }
        }

        return count;
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

    public List<int[][][]> getAll3D() {
        return all3D;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}



