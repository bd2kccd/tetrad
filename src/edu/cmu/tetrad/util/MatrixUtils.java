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

package edu.cmu.tetrad.util;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import cern.colt.matrix.linalg.Property;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Matrix includes several public static functions performing matrix
 * operations. These function include: determinant, GJinverse, inverse,
 * multiple, difference, transpose, trace, duplicate, minor, identity, mprint,
 * impliedCovar, SEMimpliedCovar.
 *
 * @author Tianjiao Chu
 * @author Joseph Ramsey
 */
public final class MatrixUtils {

    //=========================PUBLIC METHODS===========================//

    /**
     * Tests two matrices for equality.
     *
     * @param ma The first 2D matrix to check.
     * @param mb The second 2D matrix to check.
     * @return True iff the first and second matrices are equal.
     */
    public static boolean equals(double[][] ma, double[][] mb) {
        return new TetradMatrix(ma).equals(new TetradMatrix(mb));
    }

    /**
     * Tests two vectors for equality.
     *
     * @param va The first 1D matrix to check.
     * @param vb The second 1D matrix to check.
     * @return True iff the first and second matrices are equal.
     */
    public static boolean equals(double[] va, double[] vb) {
        return new TetradVector(va).equals(new TetradVector(vb));
    }

    /**
     * Tests to see whether two matrices are equal within the given tolerance.
     * If any two corresponding elements differ by more than the given
     * tolerance, false is returned.
     *
     * @param ma        The first 2D matrix to check.
     * @param mb        The second 2D matrix to check.
     * @param tolerance A double >= 0.
     * @return Ibid.
     */
    public static boolean equals(double[][] ma, double[][] mb,
                                 double tolerance) {
        return new TetradMatrix(ma).equals(new TetradMatrix(mb), tolerance);
        //new Property(tolerance).equals(TetradMatrix.instance(ma),
           //     TetradMatrix.instance(mb));
    }

    /**
     * Tests to see whether two vectors are equal within the given tolerance. If
     * any two corresponding elements differ by more than the given tolerance,
     * false is returned.
     *
     * @param va        The first matrix to check.
     * @param vb        The second matrix to check.
     * @param tolerance A double >= 0.
     * @return Ibid.
     */
    public static boolean equals(double[] va, double[] vb, double tolerance) {
        return new Property(tolerance).equals(new DenseDoubleMatrix1D(va),
                new DenseDoubleMatrix1D(vb));
    }

    /**
     * Returns true iff <code>m</code> is square.
     *
     * @param m A 2D double matrix.
     * @return Ibid.
     */
    public static boolean isSquare(double[][] m) {
        return new TetradMatrix(m).isSquare();

    }

    /**
     * Returns true iff <code>m</code> is symmetric to within the given
     * tolerance--that is, Math.abs(m[i][j] - m[j][i]) <= tolernance for each i,
     * j.
     *
     * @param m         The matrix to check.
     * @param tolerance A double >= 0.
     * @return Ibid.
     */
    @SuppressWarnings({"SameParameterValue", "BooleanMethodIsAlwaysInverted"})
    public static boolean isSymmetric(double[][] m, double tolerance) {
        for (int i = 0; i < m.length; i++) {
            for (int j = i; j < m[0].length; j++) {
                if (Math.abs(m[i][j] - m[j][i]) > tolerance) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the determinant of a (square) m.
     *
     * @param m The matrix whose determinant is sought. Must be square.
     * @return Ibid.
     */
    public static double determinant(double[][] m) {
        return new TetradMatrix(m).det();
    }

    /**
     * A copy of the original (square) matrix with the stated index row/column
     * removed
     */
    public static double[][] submatrix(double[][] m, int rem) {
        int[] indices = new int[m.length];

        int j = -1;
        for (int i = 0; i < m.length; i++) {
            j++;
            if (j == rem) {
                j++;
            }
            indices[i] = j;
        }

        return new TetradMatrix(m).getSelection(indices,
                indices).toArray();
    }

    /**
     * Returns the inverse of the given square matrix if it is nonsingular,
     * otherwise the pseudoinverse.
     */
    public static double[][] inverse(double[][] m) {
        TetradMatrix mm = new TetradMatrix(m);
        return mm.inverse().toArray();
    }

    public static double[][] pseudoInverse(double[][] x) {
        SingularValueDecomposition svd = new SingularValueDecomposition(new Matrix(x));

        Matrix U = svd.getU();
        Matrix V = svd.getV();
        Matrix S = svd.getS();

        for (int i = 0; i < S.getRowDimension(); i++) {
            for (int j = 0; j < S.getColumnDimension(); j++) {
                double v = S.get(i, j);
                S.set(i,  j, v == 0 ? 0.0 : 1.0 / v);
            }
        }

        return V.times(S.times(U.transpose())).getArray();
    }

    /**
     * Returns the outerProduct of ma and mb. The dimensions of ma and mb must
     * be compatible for multiplication.
     */
    public static double[][] product(double[][] ma, double[][] mb) {
        TetradMatrix d = new TetradMatrix(ma);
        TetradMatrix e = new TetradMatrix(mb);
        return d.times(e).toArray();
    }

    public static double[] product(double[] ma, double[][] mb) {
        return new TetradMatrix(mb).transpose().times(new TetradVector(ma)).toArray();
    }

    public static TetradVector product(TetradVector ma, TetradMatrix mb) {
        return mb.transpose().times(ma);
    }

    public static double[] product(double[][] ma, double[] mb) {
        return new TetradMatrix(ma).times(new TetradVector(mb)).toArray();
    }

    public static double[][] outerProduct(double[] ma, double[] mb) {
        return TetradAlgebra.multOuter(new TetradVector(ma), new TetradVector(mb)).toArray();
    }

    public static double innerProduct(double[] ma, double[] mb) {
        return new TetradVector(ma).dotProduct(new TetradVector(mb));
    }

    /**
     * Returns the transpose of the given matrix.
     */
    public static double[][] transpose(double[][] m) {
        return new TetradMatrix(m).transpose().toArray();
    }

    /**
     * Returns the trace of the given (square) m.
     */
    public static double trace(double[][] m) {
        return new TetradMatrix(m).trace();
    }

    //Returns the sum of all values in a double matrix.
    public static double zSum(double[][] m) {
        return new TetradMatrix(m).zSum();
    }

    /**
     * Returns the identity matrix of the given order.
     */
    public static double[][] identity(int size) {
        return TetradAlgebra.identity(size).toArray();
    }

    /**
     * Returns the sum of ma and mb.
     */
    public static double[][] sum(double[][] ma, double[][] mb) {
        TetradMatrix _ma = new TetradMatrix(ma);
        TetradMatrix _mb = new TetradMatrix(mb);
        _ma = _ma.plus(_mb);
        return _ma.toArray();
    }

    public static double[] sum(double[] ma, double[] mb) {
        TetradVector _ma = new TetradVector(ma);
        TetradVector _mb = new TetradVector(mb);
        _ma = _ma.plus(_mb);
        return _ma.toArray();
    }

    public static double[][] subtract(double[][] ma, double[][] mb) {
        TetradMatrix _ma = new TetradMatrix(ma);
        TetradMatrix _mb = new TetradMatrix(mb);
        _ma = _ma.minus(_mb);
        return _ma.toArray();
    }

    public static double[] subtract(double[] ma, double[] mb) {
        TetradVector _ma = new TetradVector(ma);
        TetradVector _mb = new TetradVector(mb);
        _ma = _ma.minus(_mb);
        return _ma.toArray();
    }

    /**
     * Computes the direct (Kronecker) outerProduct.
     */
    public static double[][] directProduct(double[][] ma, double[][] mb) {
        int arow = ma.length;
        int brow = mb.length;
        int acol = ma[0].length;
        int bcol = mb[0].length;

        double[][] product = new double[arow * brow][acol * bcol];

        for (int i1 = 0; i1 < arow; i1++) {
            for (int j1 = 0; j1 < acol; j1++) {
                for (int i2 = 0; i2 < brow; i2++) {
                    for (int j2 = 0; j2 < bcol; j2++) {
                        int i = i1 * brow + i2;
                        int j = j1 * bcol + j2;
                        product[i][j] = ma[i1][j1] * mb[i2][j2];
                    }
                }
            }
        }

        return product;
    }

    /**
     * Multiplies the given matrix through by the given scalar.
     */
    public static double[][] scalarProduct(double scalar, double[][] m) {
        TetradMatrix _m = new TetradMatrix(m);
        return _m.scalarMult(scalar).toArray();
    }

    public static double[] scalarProduct(double scalar, double[] m) {
        TetradVector _m = new TetradVector(m);
        _m = _m.scalarMult(scalar);
        return _m.toArray();
    }

    /**
     * Concatenates the vectors rows[i], i = 0...rows.length, into a single
     * vector.
     */
    public static double[] concatenate(double[][] vectors) {
        int numVectors = vectors.length;
        int length = vectors[0].length;
        double[] concat = new double[numVectors * length];

        for (int i = 0; i < vectors.length; i++) {
            System.arraycopy(vectors[i], 0, concat, i * length, length);
        }

        return concat;
    }

    /**
     * Returns the vector as a 1 x n row matrix.
     */
    public static double[][] asRow(double[] v) {
        double[][] arr = new double[1][v.length];
        System.arraycopy(v, 0, arr[0], 0, v.length);
        return arr;
    }

    /**
     * Returns the vector as an n x 1 column matrix.
     */
    public static double[][] asCol(double[] v) {
        double[][] arr = new double[v.length][1];
        for (int i = 0; i < v.length; i++) {
            arr[i][0] = v[i];
        }
        return arr;
    }

    /**
     * Returns the implied covariance matrix for a structural equation model
     * with measured and possibly latent variables. ErrCovar should be
     * positive definite for this method to work properly, though this is
     * not checked. To check it, call <code>MatrixUtils.isSymmetricPositiveDefinite(errCovar)</code>.
     *
     * @param edgeCoef The edge covariance matrix. edgeCoef(i, j) is a parameter
     *                 in this matrix just in case i-->j is an edge in the model. All other
     *                 entries in the matrix are zero.
     * @param errCovar The error covariance matrix. errCovar(i, i) is the
     *                 variance of i; off-diagonal errCovar(i, j) are covariance parameters
     *                 that are specified in the model. All other matrix entries are zero.
     * @return The implied covariance matrix, which is the covariance matrix
     *         over the measured variables that is implied by all the given information.
     * @throws IllegalArgumentException if edgeCoef or errCovar contains an
     *                                  undefined value (Double.NaN).
     */
    public static TetradMatrix impliedCovar2(TetradMatrix edgeCoef, TetradMatrix errCovar) {
        if (containsNaN(edgeCoef)) {
            throw new IllegalArgumentException("Edge coefficient matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there.");
        }

        if (containsNaN(errCovar)) {
            throw new IllegalArgumentException("Error covariance matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there.");
        }

        int sampleSize = 10000;

        TetradMatrix iMinusBInverse = TetradAlgebra.identity(edgeCoef.rows()).minus(edgeCoef).inverse();

        TetradMatrix sample = new TetradMatrix(sampleSize, edgeCoef.columns());
        TetradVector e = new TetradVector((edgeCoef.columns()));

        for (int i = 0; i < sampleSize; i++) {
            for (int j = 0; j < e.size(); j++) {
                e.set(j, RandomUtil.getInstance().nextNormal(0, errCovar.get(j, j)));
            }

            sample.assignRow(i, iMinusBInverse.times(e));
        }

        return sample.transpose().times(sample).scalarMult(1.0 / sampleSize);
    }

    public static TetradMatrix impliedCovar(TetradMatrix edgeCoef, TetradMatrix errCovar) {
        if (containsNaN(edgeCoef)) {
            System.out.println(edgeCoef);
            throw new IllegalArgumentException("Edge coefficient matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there.");
        }

        if (containsNaN(errCovar)) {
            throw new IllegalArgumentException("Error covariance matrix must not " +
                    "contain undefined values. Probably the search put them " +
                    "there.");
        }

        // I - B
        TetradMatrix m1 =  TetradAlgebra.identity(edgeCoef.rows()).minus(edgeCoef);

        // (I - B) ^ -1
        TetradMatrix m3 = m1.inverse();

        // ((I - B) ^ -1)'
        TetradMatrix m4 = m3.transpose();

        // ((I - B) ^ -1) Cov(e)
        TetradMatrix m5 = m3.times(errCovar);

        // ((I - B) ^ -1) Cov(e) ((I - B) ^ -1)'
        TetradMatrix m6 = m5.times(m4);

        return m6;
    }

    public static boolean containsNaN(TetradMatrix m) {
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.columns(); j++) {
                if (Double.isNaN(m.get(i, j))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns vech of the given array. (This is what you get when you stack all
     * of the elements of m in the lower triangular of m to form a vector. The
     * elements are stacked in columns left to right, top to bottom.)
     */
    public static double[][] vech(double[][] m) {
        if (!isSymmetric(m, 1.e-5)) {
            throw new IllegalArgumentException("m must be a symmetric matrix.");
        }

        int order = m.length;
        int vechSize = sum0ToN(order);
        double[] vech = new double[vechSize];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = i; j < order; j++) {
                vech[++index] = m[i][j];
            }
        }

        return asCol(vech);
    }

    /**
     * Returns the symmetric matrix for which the given array is the vech.
     */
    public static double[][] invVech(double[] vech) {

        int order = vechOrder(vech);

        // Recreate the symmetric matrix.
        double[][] m = new double[order][order];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = i; j < order; j++) {
                ++index;
                m[i][j] = vech[index];
                m[j][i] = vech[index];
            }
        }

        return m;
    }

    /**
     * Returns vech of the given array. (This is what you get when you stack all
     * of the elements of m to form a vector. The elements are stacked in
     * columns left to right, top to bottom.)
     */
    public static double[][] vec(double[][] m) {
        assert isSquare(m);

        int order = m.length;
        int vecSize = order * order;
        double[] vec = new double[vecSize];

        int index = -1;
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                vec[++index] = m[i][j];
            }
        }

        return asCol(vec);
    }

    /**
     * Returns the sum of integers from 0 up to n.
     */
    public static int sum0ToN(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Argument must be >= 0: " + n);
        }

        return n * (n + 1) / 2;
    }

    /**
     * The matrix which, when postmultiplied by vech, return vec.
     *
     * @param n the size of the square matrix that vec and vech come from.
     */
    public static double[][] vechToVecLeft(int n) {
        int row = n * n;
        int col = sum0ToN(n);
        double[][] m = new double[row][col];

        int index = -1;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int _row = i * n + j;
                int _col = ++index;
                m[_row][_col] = 1.0;
                _row = j * n + i;
                _col = index;
                m[_row][_col] = 1.0;
            }
        }

        return m;
    }

    /**
     * Returns true just in case the given matrix has the given dimensions
     * --that is, just in case m.length == i and m[0].length == j.
     */
    public static boolean hasDimensions(double[][] m, int i, int j) {
        TetradMatrix _m = new TetradMatrix(m);
        return _m.rows() == i && _m.columns() == j;
    }

    public static double[][] zeros(int rows, int cols) {
        return new TetradMatrix(rows, cols).toArray();
    }

    /**
     * Return true if the given matrix is symmetric positive definite--that is,
     * if it would make a valid covariance matrix.
     */
    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public static boolean isPositiveDefinite(TetradMatrix matrix) {
        DoubleMatrix2D _matrix = new DenseDoubleMatrix2D(matrix.toArray());
//        System.out.println(MatrixUtils.toString(new CholeskyDecomposition(_matrix).getL().toArray()));
        return new CholeskyDecomposition(_matrix).isSymmetricPositiveDefinite();
    }

    public static double[][] cholesky(double[][] covar) {
        return new CholeskyDecomposition(
                new DenseDoubleMatrix2D(covar)).getL().toArray();
    }

    public static TetradMatrix choleskyC(TetradMatrix covar) {
        DoubleMatrix2D _covar = new DenseDoubleMatrix2D(covar.toArray());
        DoubleMatrix2D l = new CholeskyDecomposition(_covar).getL();
        return new TetradMatrix(l.toArray());
    }

    /**
     * Converts a covariance matrix to a correlation matrix.
     */
    public static TetradMatrix convertCovToCorr(TetradMatrix m) {
        DenseDoubleMatrix2D cov = new DenseDoubleMatrix2D(m.toArray());
        DoubleMatrix2D correlation = Statistic.correlation(cov);
        return new TetradMatrix(correlation.toArray());
    }

    /**
     * Converts a matrix in lower triangular form to a symmetric matrix in
     * square form. The lower triangular matrix need not contain matrix elements
     * to represent elements in the upper triangle.
     */
    public static double[][] convertLowerTriangleToSymmetric(double[][] arr) {
        int size = arr.length;
        double[][] m = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= i; j++) {
                m[i][j] = arr[i][j];
                m[j][i] = arr[i][j];
            }
        }

        return m;
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with a tab character. The number format
     * is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[][] m) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toString(m, nf);
    }

    public static String toString(double[][] m, List<String> variables) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toString(m, nf, variables);
    }

    public static String toString(double[][] m, NumberFormat nf) {
        return toString(m, nf, null);
    }

    /**
     * Copies the given array, using a standard scientific notation number
     * formatter and beginning each line with the given lineInit. The number
     * format is DecimalFormat(" 0.0000;-0.0000").
     */
    public static String toString(double[][] m, NumberFormat nf, List<String> variables) {
        String result;
        if (nf == null) {
            throw new NullPointerException("NumberFormat must not be null.");
        }

        if (variables == null) {
            variables = new ArrayList<String>();

            for (int i = 0; i < m[0].length; i++) {
                variables.add("V" + (i + 1));
            }
        }

        if (m == null) {
            result = nullMessage();
        } else {
            TextTable textTable = new TextTable(m.length + 1, m[0].length);

            for (int i = 0; i < variables.size(); i++) {
                textTable.setToken(0, i, variables.get(i));
            }

            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    textTable.setToken(i + 1, j, nf.format(m[i][j]));
                }
            }

            result = "\n" + textTable.toString();
        }

        return result;
    }

    public static String toStringSquare(double[][] m, List<String> variables) {
        NumberFormat nf = new DecimalFormat(" 0.0000;-0.0000");
        return toStringSquare(m, nf, variables);
    }

    public static String toStringSquare(double[][] m, NumberFormat nf, List<String> variables) {
        String result;
        if (nf == null) {
            throw new NullPointerException("NumberFormat must not be null.");
        }

        if (variables == null) {
            variables = new ArrayList<String>();

            for (int i = 0; i < m.length; i++) {
                variables.add("V" + (i + 1));
            }
        }

        if (m == null) {
            result = nullMessage();
        } else {
            TextTable textTable = new TextTable(m.length + 1, m[0].length + 1);

            for (int i = 0; i < variables.size(); i++) {
                textTable.setToken(0, i + 1, variables.get(i));
            }

            for (int i = 0; i < m.length; i++) {
                textTable.setToken(i + 1, 0, variables.get(i));

                for (int j = 0; j < m[0].length; j++) {
                    textTable.setToken(i + 1, j + 1, nf.format(m[i][j]));
                }
            }

            result = "\n" + textTable.toString();
        }
        return result;
    }

    public static String toString(int[] m) {
        StringBuilder buf = new StringBuilder();

        for (int aM : m) {
            buf.append(aM).append("\t");
        }

        return buf.toString();
    }

    public static String toString(int[][] m, List<String> variables) {
        String result;

        if (variables == null) {
            variables = new ArrayList<String>();

            for (int i = 0; i < m.length; i++) {
                variables.add("V" + (i + 1));
            }
        }

        if (m == null) {
            result = nullMessage();
        } else {
            TextTable textTable = new TextTable(m.length + 1, m[0].length);

            for (int i = 0; i < variables.size(); i++) {
                textTable.setToken(0, i, variables.get(i));
            }

            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    textTable.setToken(i + 1, j, Integer.toString(m[i][j]));
                }
            }

            result = "\n" + textTable.toString();
        }

        return result;
    }

    public static String toStringSquare(int[][] m, List<String> variables) {
        String result;

        if (m == null) {
            result = nullMessage();
        } else {
            if (variables == null) {
                variables = new ArrayList<String>();

                for (int i = 0; i < m.length; i++) {
                    variables.add("V" + (i + 1));
                }
            }

            TextTable textTable = new TextTable(m.length + 1, m[0].length + 1);

            for (int i = 0; i < variables.size(); i++) {
                textTable.setToken(0, i + 1, variables.get(i));
            }

            for (int i = 0; i < m.length; i++) {
                textTable.setToken(i + 1, 0, variables.get(i));

                for (int j = 0; j < m[0].length; j++) {
                    textTable.setToken(i + 1, j + 1, Integer.toString(m[i][j]));
                }
            }

            result = "\n" + textTable.toString();
        }
        return result;
    }


    public static String toString(double[] m) {
        StringBuilder buf = new StringBuilder();

        for (double aM : m) {
            buf.append(aM).append("\t");
        }

        return buf.toString();
    }

    public static String toString(double[] m, NumberFormat nf) {
        StringBuilder buf = new StringBuilder();

        for (double aM : m) {
            buf.append(nf.format(aM)).append("\t");
        }

        return buf.toString();
    }

    public static String toString(int[][] m) {
        TextTable textTable = new TextTable(m.length, m[0].length);

        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                textTable.setToken(i, j, Integer.toString(m[i][j]));
            }
        }

        return "\n" + textTable.toString();
    }

    /**
     * Copies the given array, starting each line with a tab character..
     */
    public static String toString(boolean[][] m) {
        String result;

        if (m == null) {
            result = nullMessage();
        } else {
            TextTable textTable = new TextTable(m.length, m[0].length);

            for (int i = 0; i < m.length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    textTable.setToken(i, j, Boolean.toString(m[i][j]));
                }
            }

            result = "\n" + textTable.toString();
        }
        return result;
    }

    //=========================PRIVATE METHODS===========================//

    private static String nullMessage() {
        StringBuilder buf = new StringBuilder();
        buf.append("\n");
        buf.append("\t");
        buf.append("<Matrix is null>");
        return buf.toString();
    }

    /**
     * Returns the order of the matrix for which this is the vech.
     *
     * @throws IllegalArgumentException in case this matrix does not have
     *                                  dimension n x 1 for some n = 0 + 1 + 2 +
     *                                  ... + k for some k.
     */
    private static int vechOrder(double[] vech) {
        int difference = vech.length;
        int order = 0;
        while (difference > 0) {
            difference -= (++order);
            if (difference < 0) {
                throw new IllegalArgumentException(
                        "Illegal length for vech: " + vech.length);
            }
        }
        return order;
    }


    public static int[] copyOf(int[] arr, int length) {
        int[] copy = new int[arr.length];
        System.arraycopy(arr, 0, copy, 0, length);
        return copy;
    }

    public static double[][] copyOf(double[][] arr) {
        double[][] copy = new double[arr.length][arr[0].length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                copy[i][j] = arr[i][j];
            }
        }

        return copy;
    }
}



