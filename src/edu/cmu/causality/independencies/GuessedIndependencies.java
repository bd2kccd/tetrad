package edu.cmu.causality.independencies;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.tetrad.graph.Graph;

/**
 * This class describes the functions needed to process the student guesses of
 * the independencies.
 *
 * @author mattheweasterday
 */
public class GuessedIndependencies {
    private final String[][] stringCombinations;
    private final Boolean[] isIndependentColumn;

    /**
     * Constructor.
     *
     * @param graph use the correct_graph for this parameter.
     */
    public GuessedIndependencies(Graph graph) {
        VariablesStudied studiedVariables = new ExperimentalSetup("temp", graph);
        int numRows = Independencies.getNumRows(studiedVariables);
        stringCombinations = new String[numRows][3];
        isIndependentColumn = new Boolean[numRows];

        for (int i = 0; i < numRows; i++) {
            stringCombinations[i] = Independencies.getStringCombination(i, studiedVariables);
        }
    }

    /**
     * @param stringCombination this corresponds to one of the rows of different
     *                          combination of the variables in the independencies table.
     * @return when a particular combination of variables is guessed independent.
     */
    public Boolean isIndependent(String[] stringCombination) {
        for (int i = 0; i < stringCombinations.length; i++) {
            if (matches(stringCombinations[i], stringCombination)) {
                return isIndependentColumn[i];
            }
        }
        return null;
    }

    /**
     * Set a particular combination of variables as being guessed independent or
     * dependent.
     *
     * @param isIndependent     Boolean wrapper class indicating whether the
     *                          combination is guessed independent or not.
     * @param stringCombination this corresponds to one of the rows of different
     *                          combination of the variables in the independencies table.
     */
    public void setIndependent(Boolean isIndependent, String[] stringCombination) {
        for (int i = 0; i < stringCombinations.length; i++) {
            if (matches(stringCombinations[i], stringCombination)) {
                isIndependentColumn[i] = isIndependent;
            }
        }
    }

    private boolean matches(String[] combo1, String[] combo2) {
        if (combo1.length == combo2.length) {
            if (combo1[0].equals(combo2[0])) {
                if (combo1[1].equals(combo2[1])) {
                    if (combo1.length == 3) {
                        if (combo1[2].equals(combo2[2])) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
