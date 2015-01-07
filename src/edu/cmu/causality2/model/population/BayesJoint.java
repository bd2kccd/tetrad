package edu.cmu.causality2.model.population;

import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.BayesImProbs;
import edu.cmu.tetrad.bayes.Proposition;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.data.VariableSource;
import edu.cmu.tetrad.graph.Node;

import java.util.ArrayList;


/**
 * This class produces a table of combinations of values for discrete variables in a
 * Bayes IM to the probabilities of those combinations in the Bayes IM. The table
 * is organized by row, which ranges over all combinations of values over the studied
 * variables. (If the studied variables is null,
 * it is taken to be the list of variables in the Bayes IM. The last column of the
 * table is the probability of the combination of variables in that row. In this way,
 * the table represents the probability distribution of the joint distribution of
 * the IM and not the probabilities for particular variables given their parents
 * in the model.
 *
 * Note that this serves as the Bayes population. The SEM population is a correlation
 * matrix.
 *
 * @author Matt Easterday
 * @author Joseph Ramsey
 */
public class BayesJoint {
    private static final int VALUE_NOT_VIEWED = -1;

    private BayesIm bayesIm;
    private BayesImProbs probs;
    private ArrayList<DiscreteVariable> discreteVariables;

    public BayesJoint(BayesIm bayesIm) {
        this.bayesIm = bayesIm;
        this.probs = new BayesImProbs(bayesIm);

        this.discreteVariables = new ArrayList<DiscreteVariable>();

        for (Node node : bayesIm.getVariables()) {
            this.discreteVariables.add((DiscreteVariable) node);
        }
    }

    public String[] getCase(int row) {
        int[] combination = getCombination(row);
        return createStringCombination(combination);
    }

    public double getProbability(int row) {
        int[] combination = getCombination(row);
        Proposition proposition = createProposition(bayesIm, combination);
        return probs.getProb(proposition);
    }

    public int getNumRows() {
        int rows = 1;

        for (DiscreteVariable variable : discreteVariables) {
            rows *= variable.getNumCategories();
        }

        return rows;

    }

    /**
     * Returns the number of columns for the combinations.
     *
     * @return the number of columns.
     */
    public int numColumns() {
        return discreteVariables.size();
    }


    /**
     * Returns the headers (names of visibile variables) for the joint distribution
     * depending on what variables are viewed.
     */
    public String[] createHeaders() {

        String[] headers;

        headers = new String[getNumColumns() + 1];

        for (int i = 0; i < discreteVariables.size(); i++) {
            headers[i] = discreteVariables.get(i).getName();
        }

        headers[getNumColumns()] = "%";
        return headers;
    }

    /**
     * This class produces a table of combinations of discrete values to
     *
     * @return an array of ints where the index i represents the ith node in the IM.
     *         The value n (stored at the ith index) is the index of the ith node's nth value,
     *         i.e. array[i] = n where i is the node, n is the node's value
     */
    public int[] getCombination(int row) {

        int product = 1;

        int[] combination = new int[numColumns()];

        for (int i = discreteVariables.size() - 1; i >= 0; i--) {
            DiscreteVariable node = discreteVariables.get(i);
            int numValues = bayesIm.getBayesPm().getNumCategories(node);
            int value = (row / product) % numValues;
            combination[i] = value;
            product = product * numValues;
        }

        return combination;
    }

    public int getRowIndex(int[] values) {
        int rowIndex = 0;

        for (int i = 0; i < discreteVariables.size(); i++) {
            rowIndex *= discreteVariables.get(i).getNumCategories();
            rowIndex += values[i];
        }

        return rowIndex;
    }

    public DiscreteVariable getVariable(int varIndex) {
        return discreteVariables.get(varIndex);
    }

    //------------------PROTECTED AND PRIVATE METHODS ------------------------------------------

    /**
     * ffff
     * This is a helper method for getProbability() that converts
     *
     * @param varSource   the instantiated model with the variables and their
     *                    probabilites of occuring.
     * @param combination an array of ints where if combination[i] = n, then
     *                    i is the ith node in the varSource, and n is a value of that
     *                    node.  if n = VALUE_NOT_VIEWED, then that node is not being observed in
     *                    the experimental setup.
     * @return a data structure that translates the parameters into something
     *         tetrad can understand.
     */
    private static Proposition createProposition(VariableSource varSource,
                                                 int[] combination) {
        Proposition proposition;
        int value;

        proposition = Proposition.tautology(varSource);
        for (int nodeIndex = 0; nodeIndex < combination.length; nodeIndex++) {
            value = combination[nodeIndex];
            if (value == VALUE_NOT_VIEWED) {
                proposition.setVariable(nodeIndex, true);
            } else {
                proposition.setVariable(nodeIndex, false);
                proposition.setCategory(nodeIndex, value); //, true);
            }
        }
        return proposition;
    }

    /**
     * Returns the number of columns that will be in the final joint distribution - 1.
     * In otherwords, latent variables will be included if showLatents == true
     * and viewed variables will be included.
     *
     * @return the number of columns that will be in the final joint distribution - 1.
     */
    private int getNumColumns() {
        return discreteVariables.size();
    }

    /**
     * Takes a combination of variable values as an array of ints and converts it
     * to the names of the values.  Variables that are not viewed will not be
     * converted.
     *
     * @param combination and array of variable value indicies.
     * @return an array of variable value names.
     */
    private String[] createStringCombination(int[] combination) {

        Node node;
        String[] stringCombination = new String[getNumColumns()];
        int i, j;

        for (j = 0, i = 0; i < discreteVariables.size(); i++) {
            node = discreteVariables.get(i);
            stringCombination[j++] = bayesIm.getBayesPm().getCategory(node, combination[i]);
        }

        return stringCombination;
    }
}
