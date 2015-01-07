package edu.cmu.causality;

import edu.cmu.causality.independencies.GuessedIndependencies;

/**
 * This holds the guessed independencies.
 */
abstract public class GuessedIndependenciesHolder {
    private final String experimentalSetupName;
    private final GuessedIndependencies guess;

    /**
     * Constructor.
     */
    public GuessedIndependenciesHolder(CausalityLabModel causalityLabModel, String expName) {
        experimentalSetupName = expName;
        guess = new GuessedIndependencies(causalityLabModel.getCorrectGraph());
    }

    /**
     * @return the name of the experimental setup.
     */
    public String getExperimentalSetupName() {
        return experimentalSetupName;
    }

    /**
     * @return the guessed independencies.
     */
    public GuessedIndependencies getGuess() {
        return guess;
    }

}
