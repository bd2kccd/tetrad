package edu.cmu.causality;

import edu.cmu.causality.manipulatedGraph.GuessedManipulatedGraph;

/**
 * This serves as a holder class for a guessed manipulated graph.
 */
public class GuessedManipulatedGraphHolder {
    private final String experimentalSetupName;
    private final String hypotheticalGraphName;
    private final GuessedManipulatedGraph guess;

    /**
     * Constructor.
     */
    public GuessedManipulatedGraphHolder(String expName,
                                         String hypGraphName, GuessedManipulatedGraph guess) {
        experimentalSetupName = expName;
        hypotheticalGraphName = hypGraphName;
        this.guess = guess;
    }

    /**
     * @return the experimental setup name.
     */
    public String getExperimentalSetupName() {
        return experimentalSetupName;
    }

    /**
     * @return the name of the hypothetical graph.
     */
    public String getHypotheticalGraphName() {
        return hypotheticalGraphName;
    }

    /**
     * @return the guessed manipulated graph.
     */
    public GuessedManipulatedGraph getGuess() {
        return guess;
    }
}
