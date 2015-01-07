package edu.cmu.tetrad.util;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;

/**
 * Created with IntelliJ IDEA.
 * User: josephramsey
 * Date: 5/2/13
 * Time: 4:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class LingUtils {
    //Gustavo 7 May 2007
    //
    //makes the diagonal 1, scaling the remainder of each row appropriately
    //pre: 'matrix' must be square
    public static DoubleMatrix2D normalizeDiagonal(DoubleMatrix2D matrix) {
        DoubleMatrix2D resultMatrix = matrix.copy();
        for (int i = 0; i < resultMatrix.rows(); i++) {
            double factor = 1 / resultMatrix.get(i, i);
            for (int j = 0; j < resultMatrix.columns(); j++)
                resultMatrix.set(i, j, factor * resultMatrix.get(i, j));
        }
        return resultMatrix;
    }

    //Gustavo 7 May 2007
    //returns the identity matrix of dimension n
    public static DoubleMatrix2D identityMatrix(int n) {
        DoubleMatrix2D I = new DenseDoubleMatrix2D(n, n) {
        };
        I.assign(0);
        for (int i = 0; i < n; i++)
            I.set(i, i, 1);
        return I;
    }

    //Gustavo 7 May 2007
    //returns the linear combination of two vectors a, b (aw is the coefficient of a, bw is the coefficient of b)
    public static DoubleMatrix1D linearCombination(DoubleMatrix1D a, double aw, DoubleMatrix1D b, double bw) {
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(a.size());
        for (int i = 0; i < a.size(); i++) {
            resultMatrix.set(i, aw * a.get(i) + bw * b.get(i));
        }
        return resultMatrix;
    }

    //the vectors are in vecs
    //the coefficients are in the vector 'weights'
    public static DoubleMatrix1D linearCombination(DoubleMatrix1D[] vecs, double[] weights) {
        //the elements of vecs must be vectors of the same size
        DoubleMatrix1D resultMatrix = new DenseDoubleMatrix1D(vecs[0].size());

        for (int i = 0; i < vecs[0].size(); i++) { //each entry
            double sum = 0;
            for (int j = 0; j < vecs.length; j++) { //for each vector
                sum += vecs[j].get(i) * weights[j];
            }
            resultMatrix.set(i, sum);
        }
        return resultMatrix;
    }

    //linear combination of matrices a,b
    public static DoubleMatrix2D linearCombination(DoubleMatrix2D a, double aw, DoubleMatrix2D b, double bw) {
        if (a.rows() != b.rows()) {
            System.out.println();
        }

        DoubleMatrix2D resultMatrix = new DenseDoubleMatrix2D(a.rows(), a.columns());
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < a.columns(); j++) {
                resultMatrix.set(i, j, aw * a.get(i, j) + bw * b.get(i, j));
            }
        }
        return resultMatrix;
    }

    //Gustavo 7 May 2007
    //converts Colt vectors into double[]
    public static double[] convert(DoubleMatrix1D vector) {
        int n = vector.size();
        double[] v = new double[n];
        for (int i = 0; i < n; i++)
            v[i] = vector.get(i);
        return v;
    }

    //Gustavo 7 May 2007
    //converts Colt matrices into double[]
    public static double[][] convert(DoubleMatrix2D inVectors) {
        return inVectors.toArray();
//        if (inVectors == null) return null;
//
//        int m = inVectors.rows();
//        int n = inVectors.columns();
//
//        double[][] inV = new double[m][n];
//        for (int i = 0; i < m; i++)
//            for (int j = 0; j < n; j++)
//                inV[i][j] = inVectors.get(i, j);
//
//        return inV;
    }

    //Gustavo 7 May 2007
    //converts double[] into Colt matrices
    public static DoubleMatrix2D convertToColt(double[][] vectors) {
        int m = vectors.length; //Matrix.getNumOfRows(vectors);
        int n = vectors[0].length; //Matrix.getNumOfColumns(vectors);

        DoubleMatrix2D mat = new DenseDoubleMatrix2D(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                mat.set(i, j, vectors[i][j]);

        return mat;
    }

    public static DoubleMatrix2D inverse(DoubleMatrix2D mat) {
        Matrix m = new DenseMatrix(mat.toArray());

        DenseMatrix I = Matrices.identity(m.numRows());
        DenseMatrix AI = I.copy();
        Matrix inv;

        try {
            inv = new DenseMatrix(m.solve(I, AI));
        }
        catch (MatrixSingularException e) {
            throw new RuntimeException("Singular matrix.", e);
        }

        return new DenseDoubleMatrix2D(Matrices.getArray(inv));


//        return TetradAlgebra.ZERO.inverse(mat);
    }

    public static boolean isPositiveDefinite(DoubleMatrix2D matrix) {
        return new CholeskyDecomposition(matrix).isSymmetricPositiveDefinite();
    }

}
