package edu.cmu.tetrad.util;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * Created with IntelliJ IDEA.
 * User: josephramsey
 * Date: 5/2/13
 * Time: 9:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TetradAlgebra {

    public static TetradMatrix multOuter(TetradVector v1, TetradVector v2) {
        DoubleMatrix2D m = new Algebra().multOuter(new DenseDoubleMatrix1D(v1.toArray()),
                new DenseDoubleMatrix1D(v2.toArray()), null);
        return new TetradMatrix(m.toArray());
    }

    public static  TetradMatrix solve(TetradMatrix a, TetradMatrix b) {
        DoubleMatrix2D _a = new DenseDoubleMatrix2D(a.toArray());
        DoubleMatrix2D _b = new DenseDoubleMatrix2D(b.toArray());
        return new TetradMatrix(new Algebra().solve(_a, _b).toArray());
    }

    public  static TetradMatrix identity(int rows) {
        return new TetradMatrix(DoubleFactory2D.dense.identity(rows).toArray());
    }
}
