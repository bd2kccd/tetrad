package edu.cmu.causality;

/**
 * This holds the guessed independencies for the sample.
 */
public class GuessedIndependenciesForSampleHolder extends
        GuessedIndependenciesHolder {
    private final String sampleName;

    /**
     * Constructor.
     */
    public GuessedIndependenciesForSampleHolder(CausalityLabModel model, String expName,
                                                String sampleName) {
        super(model, expName);
        this.sampleName = sampleName;
    }

    /**
     * @return the sample name.
     */
    public String getSampleName() {
        return sampleName;
    }
}
