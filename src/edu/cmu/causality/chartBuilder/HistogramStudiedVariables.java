package edu.cmu.causality.chartBuilder;

import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * This is a wrapper class to hold information (number of states, variable name)
 * about the variables that are studied in the histogram.
 *
 * @author Adrian Tang
 */
public class HistogramStudiedVariables implements VariablesStudied {

    private Vector<String> varNames;
    private HashMap<String, List<String>> varValues;

    /**
     * Constructor
     *
     * @param correctPm parametric model needed to get the values of each
     *                  variable studied
     * @param vars      string array of the names of the variables studied in an experiment
     */
    public HistogramStudiedVariables(BayesPm correctPm, String[] vars) {
        varNames = new Vector<String>(vars.length);
        varValues = new HashMap<String, List<String>>();

        for (String var : vars) {
            /* Load variable names */
            varNames.add(var);

            /* Load variables values */
            Node node = correctPm.getDag().getNode(var);
            List<String> params = new ArrayList<String>(correctPm.getNumCategories(node));
            for (int j = 0; j < correctPm.getNumCategories(node); j++) {
                params.add(correctPm.getCategory(node, j));
            }
            varValues.put(var, params);
        }
    }

    public HistogramStudiedVariables(List<DiscreteVariable> variables) {
        varNames = new Vector<String>();
        varValues = new HashMap<String, List<String>>();

        for (DiscreteVariable var : variables) {
            varNames.add(var.getName());
            varValues.put(var.getName(), var.getCategories());
        }
    }

    /**
     * @return all the values that the variable with the specified name has
     */
    public List getVarValues(String varName) {
        return (List) varValues.get(varName);
    }

    /**
     * @return number of studied variables
     */
    public int getNumVars() {
        return varNames.size();
    }

    /**
     * @return the name of the studied variable with the index i
     */
    public String getVarName(int i) {
        return varNames.get(i);
    }


    public boolean isVariableStudied(String variableName) throws IllegalArgumentException {
        return varNames.contains(variableName);
    }

    public int getNumVariablesStudied() {
        return varNames.size();
    }

    public String[] getNamesOfStudiedVariables() {
        String[] names = new String[varNames.size()];

        for (int i = 0; i < varNames.size(); i++) {
            names[i] = varNames.get(i);
        }

        return names;
    }
}
