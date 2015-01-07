package edu.cmu.causality.event;


/**
 * Classes that want to listen to changes in the model can receive notification
 * of changes in the hypothesis, experimental setups, samples, or finances records.
 *
 * @author mattheweasterday
 */
public interface ModelChangeListener {

    /**
     * Implement this method to be notified of changes in the hypothesis.
     *
     * @param hcEvent contains the name of the hypothesis changed
     */
    public void hypothesisChanged(HypothesisChangedEvent hcEvent);

    /**
     * Implement this method to be notified of changes in the experimental setup.
     *
     * @param ecEvent contains the name of the experimental setup changed.
     */
    public void experimentChanged(ExperimentChangedEvent ecEvent);

    /**
     * Implement this method to be notified of changes in the sample
     *
     * @param scEvent contains the name of the sample changed.
     */
    public void sampleChanged(SampleChangedEvent scEvent);

    /**
     * Implement this method to be notified of changes in the finance records
     */
    public void financeChanged();

}
