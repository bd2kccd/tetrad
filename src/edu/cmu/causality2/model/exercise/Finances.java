package edu.cmu.causality2.model.exercise;

import edu.cmu.tetrad.util.TetradSerializable;

/**
 * Calculates the running balance of dollars left to spend on experiments.
 *
 * @author Joseph Ramsey
 */
public class Finances implements TetradSerializable {
    static final long serialVersionUID = 23L;

    public static final int DEFAULT_RESOURCE_TOTAL = 50000;
    public static final int DEFAULT_RESOURCE_OBS = 10;
    public static final int DEFAULT_RESOURCE_INT = 100;

    private int balance;

    private int resourceTotal;

    private int resourcePerObs;

    private int resourcePerInt;

    public Finances(int resourceTotal, int resourcePerObs, int resourcePerInt) {
        this.resourceTotal = resourceTotal;
        this.resourcePerObs = resourcePerObs;
        this.resourcePerInt = resourcePerInt;

        this.balance = resourceTotal;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static Finances serializableInstance() {
        return new Finances(10, 10, 10);
    }

    public void observeRecords(int numObservations) {
        balance = getBalance() - getResourcePerObs() * numObservations;
    }

    public void incureInterventionCost(int numIterventions) {
        balance = getBalance() - getResourcePerInt() * numIterventions;
    }

    /**
     * Fields to implement dollar resources allowed for the user. Current dollar balance.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Initial balance allotted.
     */
    public int getResourceTotal() {
        return resourceTotal;
    }

    /**
     * Dollar cost per sample unit if everything is passively observed.
     */
    public int getResourcePerObs() {
        return resourcePerObs;
    }

    /**
     * Dollar cost per sample unit if some variable is intervened upon.
     */
    public int getResourcePerInt() {
        return resourcePerInt;
    }
}
