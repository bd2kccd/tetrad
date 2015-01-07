package edu.cmu.tetradapp.editor;

import edu.cmu.tetrad.util.NumberFormatUtil;
import edu.cmu.tetrad.util.TetradSerializable;

import java.text.NumberFormat;

public final class IndependenceResult implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public enum Type {
        INDEPENDENT, DEPENDENT, UNDETERMINED
    }

    private static NumberFormat nf = NumberFormatUtil.getInstance().getNumberFormat();

    private int index;
    private String fact;
    private Type indep;
    private double pValue;

    public IndependenceResult(int index, String fact, Type indep, double pValue) {
        this.index = index;
        this.fact = fact;
        this.indep = indep;
        this.pValue = pValue;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static IndependenceResult serializableInstance() {
        return new IndependenceResult(1, "X _||_ Y", Type.DEPENDENT, 0.0001);
    }

    public int getIndex() {
        return index;
    }

    public String getFact() {
        return fact;
    }

    public Type getType() {
        return indep;
    }

    public double getpValue() {
        return pValue;
    }

    public String toString() {
        return "Result: " + getFact() + "\t" + getType() + "\t" + nf.format(getpValue());
    }
}
