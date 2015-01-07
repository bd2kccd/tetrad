package edu.cmu.causality.sample;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.causality.population.BayesMarginalJoint;
import edu.cmu.causality.population.CorrectManipulatedGraphBayesIm;
import edu.cmu.tetrad.bayes.BayesIm;
import edu.cmu.tetrad.bayes.MlBayesEstimator;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * The BayesSample basically consists of two tables: sample cases, and sample
 * case frequencies. The first table has the same number of columns as studied
 * variables in the experimental setup. Each row is a sample case--an "measured"
 * instance observed from some population. Each cell in the sample case is a
 * possible value for the variable represented in the column. For example, if
 * the columns are "education" "income" and "happiness", then the first
 * row/sample case might be "high school" "50,000" "very happy".
 * <p/>
 * The second table, the sample case frequencies, is just the number of times a
 * given sample case occurs. This table has the same structure as the BayesPopulation
 * table, i.e. each row is a possible sample case, but in this table, the last
 * column is a double representing the frequency with which this possible sample
 * case occurs in the sample case tables. The rows in the sample case frequency
 * table are the same combinations of variable values as in the population
 * table.
 *
 * @author Matthew Easterday Date: Oct 20, 2003 Time:
 */
public class BayesSample implements Sample {

    private static final int VALUE_NOT_VIEWED = -1;
    private final BayesIm correctManipulatedGraphIM;

    private DataSet dataSet;
    private BayesMarginalJoint marginalJoint;

    /**
     * The BayesSample basically consists of two tables: sample cases, and
     * sample case frequencies. The first table has the same number of columns
     * as studied variables in the experimental setup. Each row is a sample
     * case--an "measured" instance observed from some population. Each cell in
     * the sample case is a possible value for the variable represented in the
     * column. For example, if the columns are "education" "income" and
     * "happiness", then the first row/sample case might be "high school"
     * "50,000" "very happy".
     * <p/>
     * The second table, the sample case frequencies, is just the number of
     * times a given sample case occurs. This table has the same structure as
     * the BayesPopulation table, i.e. each row is a possible sample case, but in
     * this table, the last column is a double representing the frequency with
     * which this possible sample case occurs in the sample case tables. The
     * rows in the sample case frequency table are the sampe combinations of
     * varaible values as in the population table.
     */
    public BayesSample(BayesIm bayesIm, ExperimentalSetup experiment, int sampleSize, long sampleSeed) {

        correctManipulatedGraphIM = CorrectManipulatedGraphBayesIm.createIm(
                bayesIm, experiment);
        DataSet dataSet = correctManipulatedGraphIM.simulateData(sampleSize, sampleSeed, false);
        this.dataSet = dataSet;

        MlBayesEstimator estimator = new MlBayesEstimator();
        BayesIm estimatedIm = estimator.estimate(correctManipulatedGraphIM.getBayesPm(), dataSet);
        marginalJoint = new BayesMarginalJoint(estimatedIm, experiment);
    }

    public BayesSample(BayesIm im, ExperimentalSetup experiment) {
        correctManipulatedGraphIM = CorrectManipulatedGraphBayesIm.createIm(im, experiment);
    }

    public void setVariablesStudied(VariablesStudied studiedVariables) {
        marginalJoint.setStudiedVariables(studiedVariables);
    }

    /**
     * @return the dataset associated with this sample.
     */
    public DataSet getDataSet() {
        return dataSet;
    }

    private int getVariableIndexInData(String varName)
            throws IllegalArgumentException {
        Iterator it;
        int index;
        for (index = 0, it = getDataSet().getVariableNames().iterator(); it
                .hasNext(); index++) {
            String name = (String) it.next();
            if (name.equals(varName)) {
                return index;
            }
        }

        throw new IllegalArgumentException(
                "That varName does not appear in the data");
    }

    private int getVariableValueIndexInData(String varName, String varValue,
                                            BayesIm IM) {
        return IM.getBayesPm().getCategoryIndex(
                IM.getBayesPm().getDag().getNode(varName), varValue);
    }

    /**
     * Given pair(s) of variable names and values, derive for the probability
     * for those values of the variables.
     *
     * @param nameValuePairs the constraints state that the variables should have
     * @return the probability, eg P(education="college")
     */
    public double getHistogramProbabilityOfVarValue(Properties nameValuePairs) {
        return getHistogramProbability(nameValuePairs,
                correctManipulatedGraphIM);
    }

    private double getHistogramProbability(Properties nameValuePairs, BayesIm IM) {
        int[] indexCombination = new int[getDataSet().getNumColumns()];

        // initialize the indexCombination to not viewed
        for (int i = 0; i < indexCombination.length; i++) {
            indexCombination[i] = VALUE_NOT_VIEWED;
        }

        for (Enumeration varNames = nameValuePairs.propertyNames(); varNames
                .hasMoreElements(); ) {
            String varName = (String) varNames.nextElement();
            String varValue = nameValuePairs.getProperty(varName);
            indexCombination[getVariableIndexInData(varName)] = getVariableValueIndexInData(
                    varName, varValue, IM);
        }

        return marginalJoint.getProbability(marginalJoint.getRowIndex(indexCombination));
    }

    // -------------- METHODS FOR SAMPLE CASES ------------------------------

    /**
     * Calculates how many rows there should be in the sample cases table given
     * the variables that are being studied
     *
     * @return the number of rows in the sample cases table.
     */
    public int getNumSampleCases() {
        return dataSet.getNumRows();// getMaxRowCount();
    }

    /**
     * @return a string array of the names of the sample case columns.
     */
    public String[] getSampleCaseColumnNames() {
        return marginalJoint.createHeaders();
    }

    /**
     * Returns a string representing a single sample case, i.e. if the variables
     * are Education, income, & happiness, then a sample case might be:
     * "High school, $50,000, very happy".
     *
     * @param row              which sample cases to generate.
     * @param studiedVariables the variables the user is examining, in the example above,
     *                         educaiton, income & happiness.
     * @return an array of strings where each string is the value of one
     *         variable.
     */
    public String[] getValueCombination(int row, VariablesStudied studiedVariables) {
        int i, j;
        Iterator it;

        String[] varNames = new String[dataSet
                .getVariableNames().size()];
        for (i = 0, it = dataSet.getVariableNames()
                .iterator(); it.hasNext(); i++) {
            varNames[i] = (String) it.next();
        }

        String[] sampleCase = new String[studiedVariables
                .getNumVariablesStudied() + 1];

        sampleCase[0] = Integer.toString(row + 1);
        for (i = 0, j = 1; i < dataSet.getNumColumns(); i++) {
            if (studiedVariables.isVariableStudied(varNames[i])) {
                sampleCase[j++] = dataSet.getObject(row, i).toString();
            }
        }

        return sampleCase;
    }

    // ---------------- METHODS FOR SAMPLE CASE FREQUENCIES
    // ----------------------------

    /**
     * Gets the number of rows in the second table, i.e. the sample case
     * frequencies table.
     *
     * @param studiedVariables the variables the user is examining.
     * @return the number of rows / possible sample cases.
     */
    public int getNumSampleValueCombinations(VariablesStudied studiedVariables) {
        int numSampleCases = 1;
        Node node;
        int numValues;
        int i;
        Iterator<String> it;

        String[] varNames = new String[dataSet
                .getVariableNames().size()];
        for (i = 0, it = dataSet.getVariableNames()
                .iterator(); it.hasNext(); i++) {
            varNames[i] = it.next();
        }

        if (studiedVariables.getNumVariablesStudied() == 0) {
            return 0;
        }

        for (i = 0; i < varNames.length; i++) {
            if (studiedVariables.isVariableStudied(varNames[i])) {
                node = correctManipulatedGraphIM.getNode(varNames[i]);
                numValues = correctManipulatedGraphIM.getBayesPm()
                        .getNumCategories(node);
                numSampleCases *= numValues;
            }
        }

        return numSampleCases;
    }

    /**
     * @return the string array of the names of the sample frequency columns.
     */
    public String[] getSampleFrequenciesColumnNames(
            VariablesStudied studiedVariables) {
        String[] columns = new String[studiedVariables.getNumVariablesStudied() + 1];

        String[] names = studiedVariables.getNamesOfStudiedVariables();
        System.arraycopy(names, 0, columns, 0, names.length);
        columns[columns.length - 1] = "%";

        return columns;
    }

    /**
     * Gets a string representation of a possible sample case in the sample case
     * frequency table, for example, if the studied variables are education,
     * income, and happiness, then the first possible sample case might be
     * "high school, $50,000, very happy"
     *
     * @param row the given sample case combination to get.
     * @return the sample combination.
     */
    public String[] getSampleCaseFrequencyCombination(int row) {
        return marginalJoint.getCase(row);
    }

    /**
     * Gets the frequency (number of occurances of a particular case / total
     * number of cases observed) of a given sample case from all sample cases
     * observed in the sample case table.
     *
     * @param row the sample whose frequency you want.
     * @return the frequency in which the sample case occurs in the data.
     */
    public double getSampleCaseFrequency(int row) {
        return marginalJoint.getProbability(row);
    }

    public BayesMarginalJoint getMarginalJoint() {
        return marginalJoint;
    }
}
