package edu.cmu.causality.independencies;

/**
 * @author Ming Yang Koh, Luis
 */
public class IndependenceResult {

    private final double pValue;
    private final boolean isIndependent;

    public IndependenceResult(double pValue, boolean isIndependent) {
        this.pValue = pValue;
        this.isIndependent = isIndependent;
    }

    public double getPValue() {
        return pValue;
    }

    public boolean isIndependent() {
        return isIndependent;
    }


}
