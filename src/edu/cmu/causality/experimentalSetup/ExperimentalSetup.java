package edu.cmu.causality.experimentalSetup;

import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of (qualitative) manipulations for each variable
 *
 * @author Matthew Easterday
 */
public class ExperimentalSetup implements VariablesStudied, Cloneable {

    private final List<ExperimentalSetupVariable> variables;     // A list of Nodes which are the variables of the graph
    private String name;        // The name of this experimental setup


    /**
     * Constructor.
     *
     * @param name  the name of the experimental setup
     * @param graph the graph on which this experimental setup is supposed to manipulated
     */
    public ExperimentalSetup(String name, Graph graph) {
        this(name);
        for (Node var : graph.getNodes()) {
            if (var.getNodeType() == NodeType.MEASURED) {
                variables.add(new ExperimentalSetupVariable(var.getName(), 0, 1));
            } else if (var.getNodeType() == NodeType.LATENT) {
            } else if (var.getNodeType() == NodeType.ERROR) {
            }
        }
    }


    /**
     * This constructor is used to create a setup from an xml file
     * <p/>
     * NOTE: antiquated, new version below which stores the mean/sd of
     * interventions.  --Freenor
     */
    public ExperimentalSetup(String name, String[] varNames) {
        this(name);
        for (String varName : varNames) {
            variables.add(new ExperimentalSetupVariable(varName, 0, 1));
        }
    }

    /**
     * This constructor is used to create a setup from an xml file
     */
    public ExperimentalSetup(String name, String[] varNames, double[] means, double[] sds) {

        // Swapping out U(lower, upper) for N(mean, sd) jdramsey todo 9/12/2013

        this(name);
        for (int i = 0; i < varNames.length; i++) {
            variables.add(new ExperimentalSetupVariable(varNames[i], means[i], sds[i]));
        }
    }

    /**
     * Copy constructor.
     */
    public ExperimentalSetup(ExperimentalSetup es) {
        this.name = es.getName();
        variables = new ArrayList<ExperimentalSetupVariable>();

        ExperimentalSetupVariable var;
        String[] varNames = es.getNamesOfStudiedVariables();
        for (String varName : varNames) {
            var = es.getVariable(varName);
            variables.add(new ExperimentalSetupVariable(var, 0, 1));
        }
    }


    /**
     * Gets the name of the experimental setup
     *
     * @return variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the experimental setup
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Gets the number of variables in the experimental setup.
     *
     * @return num variables.
     */
    public int getNumVariables() {
        return variables.size();
    }

    /**
     * Gets the number of variables that are observed in this experimental setup.
     *
     * @return # vars observed.
     */
    public int getNumVariablesStudied() {
        int j;
        Iterator<ExperimentalSetupVariable> vars;
        for (j = 0, vars = variables.iterator(); vars.hasNext(); ) {
            if ((vars.next()).isStudied()) {
                j++;
            }
        }
        return j;
    }


    /**
     * Gets the names of all the variables in this experimental setup.
     *
     * @return array of names
     */
    public String[] getVariableNames() {
        String[] names = new String[variables.size()];
        int i;
        Iterator<ExperimentalSetupVariable> vars;
        for (i = 0, vars = variables.iterator(); vars.hasNext(); i++) {
            names[i] = (vars.next()).getName();
        }
        return names;
    }


    /**
     * Returns the names of all the variables that are studied in this
     * experimental setup.
     *
     * @return a list of variable names
     */
    public String[] getNamesOfStudiedVariables() {
        String[] names = new String[getNumVariablesStudied()];
        ExperimentalSetupVariable var;
        int i;
        int j;
        for (j = 0, i = 0; i < variables.size(); i++) {
            var = variables.get(i);
            if (var.isStudied()) {
                names[j++] = var.getName();
            }
        }
        return names;
    }


    /**
     * Use this to check that variable is in the experimental setup.
     *
     * @param variableName name of the variable to check.
     * @return true if this variable is in the experimental setup.
     */
    public boolean isValidVariableName(String variableName) {
        ExperimentalSetupVariable var;
        for (ExperimentalSetupVariable variable : variables) {
            var = variable;
            if (var.getName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Tells you if variable is explicitly observed (default) in the experimental
     * setup.
     *
     * @param variableName the variable under question.
     * @return true if studied, false if not.
     * @throws IllegalArgumentException thrown if variable doesn't exist.
     */
    public boolean isVariableStudied(String variableName)
            throws IllegalArgumentException {
        canManipulateVariable(variableName);
        return getVariable(variableName).isStudied();
    }


    /**
     * Get the given variable.
     *
     * @param name of the variable.
     * @return the variable.
     */
    public ExperimentalSetupVariable getVariable(String name) {
        ExperimentalSetupVariable var;
        for (ExperimentalSetupVariable variable : variables) {
            var = (variable);
            if (var.getName().equals(name)) {
                return var;
            }
        }
        throw new IllegalArgumentException("variable " + name +
                " not in experimental setup");
    }


    //=========================================================================
    //
    //  PRIVATE METHODS
    //
    //=========================================================================

    /*
     * Constructor.
     * @param name
     */
    private ExperimentalSetup(String name) {
        this.name = name;
        variables = new ArrayList<ExperimentalSetupVariable>();
    }

    /*
     *  Makes sure the variable isn't latent, or an error variable or null
     * @param variableName
     * @throws IllegalArgumentException
     */
    private void canManipulateVariable(String variableName) throws IllegalArgumentException {
        ExperimentalSetupVariable var = getVariable(variableName);
        if (var.getManipulation().getType() == ManipulationType.LATENT) {
            throw new IllegalArgumentException("Cannot set manipulation for Latent variable " + variableName);
        }
        if (var.getManipulation().getType() == ManipulationType.ERROR) {
            throw new IllegalArgumentException("Cannot set manipulation for Error variable " + variableName);
        }
    }
}
