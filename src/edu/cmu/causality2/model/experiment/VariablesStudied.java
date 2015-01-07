package edu.cmu.causality2.model.experiment;

/**
 * @author mattheweasterday
 */
public interface VariablesStudied {

    /**
     * Returns true if the variable is studied in the experimental setup.
     *
     * @param variableName the name of the variable.
     * @return true if variableName is studied.
     * @throws IllegalArgumentException
     */
    public boolean isVariableStudied(String variableName) throws IllegalArgumentException;

    /**
     * Returns # variables studied.
     */
    public int getNumVariablesStudied();

    /**
     * Returns names of variables studied.
     */
    public String[] getNamesOfStudiedVariables();

}
