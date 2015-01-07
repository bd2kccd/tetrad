package edu.cmu.causality;

/**
 * This holds the guessed independencies for the hypothetical graph.
 */
public class GuessedIndependenciesForHypothesisHolder extends
        GuessedIndependenciesHolder {
    private final String hypotheticalGraphName;

    /**
     * Constructor.
     */
    public GuessedIndependenciesForHypothesisHolder(CausalityLabModel model, String expName,
                                                    String hypGraphName) {
        super(model,  expName);
        hypotheticalGraphName = hypGraphName;
    }

    /**
     * @return the associated hypothetical graph.
     */
    public String getHypotheticalGraphName() {
        return hypotheticalGraphName;
    }
}
