package edu.cmu.causality.independencies;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.causality.manipulatedGraph.AbstractManipulatedGraph;
import edu.cmu.causality.sample.BayesSample;
import edu.cmu.causality.sample.Sample;
import edu.cmu.causality.sample.SemSample;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.search.IndTestChiSquare;
import edu.cmu.tetrad.search.IndTestFisherZ;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class describes all the functions related to the independencies of the
 * variables.
 *
 * @author mattheweasterday
 */
public class Independencies {

    /**
     * Gets the number of possible independence combinations given the variables
     * the user is looking at.
     *
     * @param studiedVars the variables the user is looking at.
     * @return number of possible combinations.
     */
    public static int getNumRows(VariablesStudied studiedVars) {
        int p = studiedVars.getNumVariablesStudied();
        if (p < 2) {
            return 0;
        } else if (p == 2) {
            return 1;
        } else {
            return pChoose2(p) * ((int) (Math.pow(2, (p - 2))));
        }
    }

    /**
     * The table of independencies presented to the user has 3 columns:
     * the first variable in the independence, e.g. A
     * the second variable in the independence, e.g. B
     * 0 or more variables in the conditioning set, e.g. C, D
     * e.g. A _||_ B | C, D
     * <p/>
     * Given a row index of the independencies table, and a set of variables
     * that are observed for a given experiment, this method returns a String[]
     * specifying
     * the names of the first variable e.g. "A", (in combination[0])
     * the name of the second variable e.g. "B", (in combination[1])
     * the names of any variables in the conditioning set, e.g. "C", "D"
     * (in combination[2] and on)
     *
     * @param row         the index in the independencies table (from 0 to
     *                    getNumberRows(studiedVars).
     * @param studiedVars the variables that the user has set observed.
     * @return a String[] of the names of the variables for the given row in the
     *         independecies table.
     */
    public static String[] getStringCombination(int row,
                                                VariablesStudied studiedVars) {
        int j;
        Iterator<Integer> i;

        int numVariables = studiedVars.getNumVariablesStudied();
        String[] variableNames = studiedVars.getNamesOfStudiedVariables();
        int firstVariableIndex = firstVariableIndex(row, numVariables);
        int secondVariableIndex = secondVariableIndex(row, numVariables);
        List<Integer> conditionSet = conditionSetIndicies(row, numVariables);
        String[] combination = new String[2 + conditionSet.size()];


        combination[0] = variableNames[firstVariableIndex];
        combination[1] = variableNames[secondVariableIndex];
        for (j = 2, i = conditionSet.iterator(); i.hasNext(); ) {
            combination[j++] = variableNames[(i.next())];
        }

//        System.out.print("Row " + row + " " + combination[0] + ", " + combination[1] + " | ");
//        for (int k = 2; k < combination.length; k++) {
//            System.out.print(combination[k] + " " );
//        }
//
//        System.out.println();

        return combination;
    }

    /**
     * This method calculates the independence relation for a given row in the
     * independencies table.
     *
     * @param row         the index of the table you want the independence relation for.
     * @param graph       the manipulated graph, e.g. the graph that results from the
     *                    experiment setup being viewed in the independencies table and the correct
     *                    graph.
     * @param studiedVars the variables the user has chosen to observe in teh
     *                    experimental setup.
     * @return true if the variables in the given row are independnet or false
     *         if dependent.
     */

    public static boolean isIndependent(int row,
                                        AbstractManipulatedGraph graph,
                                        VariablesStudied studiedVars) {
        String[] variableNames = getStringCombination(row, studiedVars);
        List<Node> conditionSetNodes = new ArrayList<Node>();

        Node firstNode = graph.getNode(variableNames[0]);
        Node secondNode = graph.getNode(variableNames[1]);

        for (int i = 2; i < variableNames.length; i++) {
            conditionSetNodes.add(graph.getNode(variableNames[i]));
        }

        return graph.isDSeparatedFrom(firstNode, secondNode, conditionSetNodes);
    }

    /**
     * Takes two variables and conditioning set (specified by row) and tells
     * if they are independent in the sample. created: 14-Oct-06
     */
    public static IndependenceResult isSampleIndependent(int row,
                                             Sample sample,
                                             VariablesStudied studiedVars) {
        String[] variableNames = getStringCombination(row, studiedVars);
        List<Node> conditionSetVariables = new ArrayList<Node>();

        //Variable firstVariable  = sample.getDataSet().getVariable(variableNames[0]);
        //Variable secondVariable = sample.getDataSet().getVariable(variableNames[1]);//get(variableNames[1]).getVariable();
        //Variable var;

        Node firstVariable = sample.getDataSet().getVariable(variableNames[0]);
        Node secondVariable = sample.getDataSet().getVariable(variableNames[1]);//get(variableNames[1]).getVariable();

        Node var;

        for (int i = 2; i < variableNames.length; i++) {
            var = sample.getDataSet().getVariable(variableNames[i]); //getVariable(i);//get(variableNames[i]).getVariable();
            conditionSetVariables.add(var);
        }

        boolean isIndependent;
        IndependenceResult independenceResult;

        if (sample instanceof BayesSample) {
            //2nd argument is "alpha" if alpha is bigger, it's more likely to be dependent
            //IndTestGSquare test = new IndTestGSquare(new DiscreteDataSet(sample.getDataSet()), 0.05);
//            IndTestGSquare test = new IndTestGSquare(sample.getDataSet(), 0.05);
            IndTestChiSquare test = new IndTestChiSquare(sample.getDataSet(), 0.05);
            isIndependent = test.isIndependent(firstVariable, secondVariable, conditionSetVariables);

            independenceResult = new IndependenceResult(test.getPValue(), isIndependent);


        } else if (sample instanceof SemSample) {
            IndTestFisherZ test = new IndTestFisherZ(sample.getDataSet(), 0.05);
            isIndependent = test.isIndependent(firstVariable, secondVariable, conditionSetVariables);
            independenceResult = new IndependenceResult(test.getPValue(), isIndependent);

        } else {
            throw new IllegalArgumentException("Unrecognized sample type!");
        }

        return independenceResult;
    }

    /**
     * If the lab is set to display and keep track of student guesses, (i.e. if
     * the student thinks the variables in a given row of the table are
     * independent) this method tells you the value the student has guessed for
     * those variables.
     *
     * @param row         the given index of the independencies table.
     * @param guess       the set of independencies values the student has set.
     * @param studiedVars the variables studied in the experimental setup for
     *                    this independnecies table.
     * @return true if the student thinks the variables in the given row are
     *         independent, or false if dependent.
     */
    public static Boolean isIndependent(int row,
                                        GuessedIndependencies guess,
                                        VariablesStudied studiedVars) {

        String[] stringCombination = getStringCombination(row, studiedVars);

        return guess.isIndependent(stringCombination);
    }


    /**
     * If the lab is set to display and keep track of student guesses, (i.e. if
     * the student thinks the variables in a given row of the table are
     * independent) this method sets the value the student guess for
     * those variables.
     *
     * @param isIndependent the student guess, true if the student thinks the
     *                      variables in the given row are independent, or false if dependent.
     * @param row           the given index of the independencies table.
     * @param guess         the set of independencies values the student has set.
     * @param studiedVars   the variables studied in the experimental setup for
     *                      this independnecies table.
     */
    public static void setIndependent(Boolean isIndependent,
                                      int row,
                                      GuessedIndependencies guess,
                                      ExperimentalSetup studiedVars) {
        String[] stringCombination = getStringCombination(row, studiedVars);
        guess.setIndependent(isIndependent, stringCombination);
    }

    //======================== PRIVATE METHODS ===========================


    private static int firstVariableIndex(int rowIndex, int numVars) throws IllegalArgumentException {
        if (numVars < 2) {
            throw new IllegalArgumentException("There must be more than two variables to calculate independencies");
        }
        int pairIndex = pairIndex(rowIndex, numVars);
        int firstVariableIndex;
        for (firstVariableIndex = 0; firstVariableIndex < numVars; firstVariableIndex++) {
            int numPairsWithGivenFirstVariableAndBefore = numVariablePairsBeforeAndIncludingPairWithGivenFirstVariableIndex(firstVariableIndex, numVars);
            if (pairIndex < numPairsWithGivenFirstVariableAndBefore) {
                break;
            }
        }
        return firstVariableIndex;
    }

    private static int secondVariableIndex(int rowIndex, int numVars) {
        int firstVariableIndex = firstVariableIndex(rowIndex, numVars);
        int numPairsBeforePairWithGivenFirstVariableIndex = numVariablePairsBeforePairWithGivenFirstVariableIndex(firstVariableIndex, numVars);
        int pairIndex = pairIndex(rowIndex, numVars);
        return (firstVariableIndex + 1) + (pairIndex - numPairsBeforePairWithGivenFirstVariableIndex);
    }


    //todo change to set of nodes
    private static List<Integer> conditionSetIndicies(int rowIndex, int numVars) {
        List<Integer> conditionSetIndicies = new ArrayList<Integer>();
        boolean[] conditionSetIndexArray = conditionSet(rowIndex, numVars);

        for (int i = 0; i < conditionSetIndexArray.length; i++) {
            if (conditionSetIndexArray[i]) {
                conditionSetIndicies.add(i);
            }
        }
        return conditionSetIndicies;
    }

    private static boolean[] conditionSet(int rowIndex, int numVars) {
        boolean[] conditionSet = new boolean[numVars];
        int numConditionSets = rowIndex % (int) (Math.pow(2, numVars - 2));
        int firstVarIndex = firstVariableIndex(rowIndex, numVars);
        int secondVarIndex = secondVariableIndex(rowIndex, numVars);

        int conditionVarIndex = 0;
        int varIndex;
        int bit;
        for (varIndex = 0; varIndex < numVars; varIndex++) {
            if (varIndex == firstVarIndex) {
                conditionSet[varIndex] = false;
            } else if (varIndex == secondVarIndex) {
                conditionSet[varIndex] = false;
            } else {
                bit = (int) (numConditionSets / (Math.pow(2, conditionVarIndex++)) % 2);
                conditionSet[varIndex] = bit != 0;
            }

        }
        return conditionSet;
    }

    private static int numVariablePairsBeforePairWithGivenFirstVariableIndex(int firstVariableIndex, int numVariables) {
        return numVariablePairsBeforeAndIncludingPairWithGivenFirstVariableIndex(firstVariableIndex - 1, numVariables);
    }

    private static int numVariablePairsBeforeAndIncludingPairWithGivenFirstVariableIndex(int firstVariableIndex, int numVariables) {
        int numPairs = 0;
        for (int i = 0; i <= firstVariableIndex; i++) {
            numPairs += (numVariables - (i + 1));
        }
        return numPairs;
    }

    private static int pairIndex(int rowIndex, int numVars) throws IllegalArgumentException {
        if (numVars < 2) {
            throw new IllegalArgumentException("There must be more than two variables to calculate independencies");
        }
        return (rowIndex / (int) (Math.pow(2, numVars - 2)));
    }


    //error if p <= 2
    private static int pChoose2(int p) {
        return (factorial(p) / (factorial(2) * factorial(p - 2)));
    }


    private static int factorial(int i) {
        if (i == 1) return 1;
        else return (i * factorial(i - 1));
    }

}
