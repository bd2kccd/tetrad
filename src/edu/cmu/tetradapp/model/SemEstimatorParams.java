package edu.cmu.tetradapp.model;

import edu.cmu.tetrad.util.Params;
import edu.cmu.tetrad.util.TetradSerializableExcluded;
import edu.cmu.tetrad.util.Unmarshallable;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by josephramsey on 1/3/14.
 */
public class SemEstimatorParams implements Params {
    static final long serialVersionUID = 23L;

    private String semOptimizerType;

    /**
     * Constructs a new parameters object. Must be a blank constructor.
     */
    public SemEstimatorParams() {
//        this.semOptimizerType = "Regression";
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static SemEstimatorParams serializableInstance() {
        return new SemEstimatorParams();
    }

    public String getSemOptimizerType() {
        Object o = this;
        System.out.println(o.toString());
        return this.semOptimizerType;
    }

    public void setSemOptimizerType(String semOptimizerType) {
//        if (semOptimizerType == null) throw new NullPointerException();
        this.semOptimizerType = semOptimizerType;
    }
}
