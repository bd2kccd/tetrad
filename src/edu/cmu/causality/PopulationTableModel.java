package edu.cmu.causality;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.VariablesStudied;
import edu.cmu.causality.population.BayesMarginalJoint;
import edu.cmu.causality.population.SemPopulation;

import javax.swing.table.AbstractTableModel;

/**
 * A data structure that holds all the information needed by the view to
 * display population information.
 */
public class PopulationTableModel extends AbstractTableModel {
    private String experimentName;
    private BayesMarginalJoint bayesMarginalJoint;
    private CausalityLabModel causalityLabModel;

    /**
     * Constructor.
     *
     * @param experimentName the unique id of the experiment that determines the
     *                       population distribution from which the sample is taken.
     */
    public PopulationTableModel(CausalityLabModel causalityLabModel, String experimentName) {
        this.causalityLabModel = causalityLabModel;
        this.experimentName = experimentName;

        if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            this.bayesMarginalJoint = new BayesMarginalJoint(CausalityLabModel.getModel().getCorrectBayesIm(),
                    causalityLabModel.getExperiment(experimentName));
        } else {
            // SEM

        }
    }

    /**
     * Returns the names of the columns in the table model.
     *
     * @param col the index of the column to retrieve.
     * @return the name of the column.
     */
    public String getColumnName(int col) {
        if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
//                BayesPopulation bayesPopulation = new BayesPopulation(correctBayesIm, getExperiment(experimentName));
            return bayesMarginalJoint.createHeaders()[col];
        } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            return SemPopulation.getColumnHeaders(CausalityLabModel.getModel().getCorrectSemIm(), causalityLabModel.getExperiment(experimentName))[col];
        } else {
            System.err.println(this.getClass() + " Shouldn't get here");
            return null;
        }
    }

    /**
     * The number of different cases in the population distribution.
     *
     * @return the number of combinations.
     */
    public int getRowCount() {
        if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            return bayesMarginalJoint.getNumRows();
        } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            return SemPopulation.getNumRows(causalityLabModel.getExperiment(experimentName));
        } else {
            System.err.println(this.getClass() + " Shouldn't get here");
            return -1;
        }
    }

    /**
     * The number of columns in the table, basically the number of variables
     * + 1 (for the frequency).
     *
     * @return the number of columns.
     */
    public int getColumnCount() {

        ExperimentalSetup vars = causalityLabModel.getExperiment(experimentName);
        if (vars == null) {
            return 0;
        }
        if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            return vars.getNumVariablesStudied() + 1;
        } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            return vars.getNumVariablesStudied() + 1;
        } else {
            System.err.println(this.getClass() + " Shouldn't get here");
            return -1;
        }

        // return BayesPopulation.getNumColumns(CORRECT_GRAPH_BAYES_IM);
    }

    /**
     * gets The value at the the given row and column in the table, this is
     * either a variable value String, or (if its the rightmost column) a
     * Double representing the frequency with which the row combination of
     * variable values occurs in the population distribution.
     *
     * @param row    index of the row.
     * @param column index of the column.
     * @return the String or Double at the given row, column in the table.
     */
    public Object getValueAt(int row, int column) {
        ExperimentalSetup exp = causalityLabModel.getExperiment(experimentName);
        VariablesStudied studiedVariables = causalityLabModel.getExperiment(experimentName);

        if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            if (column < getColumnCount() - 1) {
                return (bayesMarginalJoint.getCase(row))[column];
            } else {
                return bayesMarginalJoint.getProbability(row);
            }
        } else if (causalityLabModel.getModelType().equals(CausalityLabModel.ModelType.SEM)) {
            return SemPopulation.getValueAt(row, column, CausalityLabModel.getModel().getCorrectSemIm(), exp, studiedVariables);
        } else {
            throw new RuntimeException(this.getClass() + " Shouldn't get here");
        }
    }

    /**
     * @return the covariance of the given two variables.
     */
    public double getCovariance(String var1, String var2) {
        return SemPopulation.getCovariance(
                SemPopulation.createCorrectManipulatedSemIm(
                        CausalityLabModel.getModel().getCorrectSemIm(),
                        causalityLabModel.getExperiment(experimentName)), var1, var2);
    }
}
