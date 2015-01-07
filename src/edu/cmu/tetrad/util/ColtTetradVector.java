package edu.cmu.tetrad.util;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

/**
 * Vector wrapping matrix library.
 */
public class ColtTetradVector implements TetradSerializable {
    static final long serialVersionUID = 23L;

    private DoubleMatrix1D data;

    private ColtTetradVector(double[] data) {
        this.data = new DenseDoubleMatrix1D(data);
    }

    private ColtTetradVector(int size) {
        this.data = new DenseDoubleMatrix1D(size);
    }

    private ColtTetradVector(DoubleMatrix1D fdata) {
        this.data = fdata;
    }

    public static ColtTetradVector instance(double[] data) {
        return new ColtTetradVector(data);
    }

    public static ColtTetradVector instance(int size) {
        return new ColtTetradVector(size);
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static ColtTetradVector serializableInstance() {
        return new ColtTetradVector(0);
    }

    public double[] toArray() {
        return this.data.toArray();
    }

    public int size() {
        return data.size();
    }

    public void set(int j, double v) {
        data.set(j, v);
    }

    public double times(ColtTetradVector v2) {
        return new Algebra().mult(data, v2.data);
    }

    public ColtTetradVector like() {
        return ColtTetradVector.instance(data.size());
    }

    public double get(int i) {
        return data.get(i);
    }

    public ColtTetradVector copy() {
        return new ColtTetradVector(data.copy());
    }

    public ColtTetradVector viewSelection(int[] selection) {
        DoubleMatrix1D doubleMatrix1D = data.viewSelection(selection);
        return new ColtTetradVector(doubleMatrix1D);
    }

    public ColtTetradVector minus(ColtTetradVector mb) {
        DoubleMatrix1D mc = data.copy();
        return new ColtTetradVector(mc.assign(mb.data, Functions.minus));
    }

    public ColtTetradVector plus(ColtTetradVector mb) {
        DoubleMatrix1D mc = data.copy();
        return new ColtTetradVector(mc.assign(mb.data, Functions.plus));
    }

    public ColtTetradVector scalarMult(double scalar) {
        DoubleMatrix1D mc = data.copy();
        mc.assign(Functions.mult(scalar));
        return new ColtTetradVector(mc);
    }

    public TetradMatrix diag() {
        return new TetradMatrix(DoubleFactory2D.dense.diagonal(data).toArray());
    }
}
