package edu.cmu.tetrad.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Vector wrapping matrix library.
 */
public class TetradVector implements TetradSerializable {
    static final long serialVersionUID = 23L;

    private RealVector data;

    public TetradVector(double[] data) {
        this.data = new ArrayRealVector(data);
    }

    public TetradVector(int size) {
        this.data = new ArrayRealVector(size);
    }

    private TetradVector(RealVector data) {
        this.data = data;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static TetradVector serializableInstance() {
        return new TetradVector(0);
    }

    public double[] toArray() {
        return data.toArray();
    }

    public int size() {
        return data.getDimension();
    }

    public void set(int j, double v) {
        data.setEntry(j, v);
    }

    public double dotProduct(TetradVector v2) {
        return data.dotProduct(v2.data);
    }

    public TetradVector like() {
        return new TetradVector(size());
    }

    public double get(int i) {
        return data.getEntry(i);
    }

    public TetradVector copy() {
        return new TetradVector(data.copy());
    }

    public TetradVector viewSelection(int[] selection) {
        double[] _selection = new double[selection.length];
        
        for (int i = 0; i < selection.length; i++) {
            _selection[i] = data.getEntry(selection[i]);
        }
        
        return new TetradVector(_selection);
    }

    public TetradVector minus(TetradVector mb) {
        return new TetradVector(data.subtract(mb.data));
    }

    public TetradVector plus(TetradVector mb) {
        return new TetradVector(data.add(mb.data));
    }

    public TetradVector scalarMult(double scalar) {
        return new TetradVector(data.mapDivideToSelf(scalar));
    }

    public TetradMatrix diag() {
        TetradMatrix m = new TetradMatrix(data.getDimension(), data.getDimension());

        for (int i = 0; i < data.getDimension(); i++) {
            m.set(i, i, data.getEntry(i));
        }

        return m;
    }
}
