package edu.cmu.causality.chartBuilder;

import edu.cmu.causality.experimentalSetup.VariablesStudied;

/**
 * Minimal implementation of the VariablesStudied interface for classes that
 * need to compute stuff about variables studied, but don't want to use
 * a full experimental setup.
 *
 * @author mattheweasterday
 */
public class StudiedVariableNames implements VariablesStudied {

    private final String[] varNames;
    private final boolean[] varIsStudied;

    /**
     * Constructor.
     *
     * @param names of variables in experimental setup.
     */
    public StudiedVariableNames(String[] names) {
        varNames = names;

        varIsStudied = new boolean[names.length];
        for (int i = 0; i < names.length; i++) {
            varIsStudied[i] = true;
        }
    }

    /**
     * Set the variable with the given name to being studied or not in the
     * experiment.
     */
    public void setVariableStudied(String variableName, boolean isStudied) {
        for (int i = 0; i < varNames.length; i++) {
            if (varNames[i].equals(variableName)) {
                varIsStudied[i] = isStudied;
            }
        }
    }

    /**
     * @return whether or not a variable with variableName is studied in the
     *         experiment.
     */
    public boolean isVariableStudied(String variableName) {
        for (int i = 0; i < varNames.length; i++) {
            if (varNames[i].equals(variableName)) {
                return varIsStudied[i];
            }
        }
        IllegalArgumentException e = new IllegalArgumentException(variableName +
                " is not a valid variable name");
        e.printStackTrace();
        throw e;
    }

    /**
     * @return number of variables studied.
     */
    public int getNumVariablesStudied() {
        int numStudied = 0;
        for (int i = 0; i < varNames.length; i++) {
            if (varIsStudied[i]) {
                numStudied++;
            }
        }
        return numStudied;
    }

    /**
     * @return a string array containing the names of the variables that are
     *         studied.
     */
    public String[] getNamesOfStudiedVariables() {
        return varNames;
    }

    /**
     * Set all variables in the experiment as unstudied.
     */
    public void setAllVariablesUnstudied() {
        for (int i = 0; i < varNames.length; i++) {
            varIsStudied[i] = false;
        }

    }


}
