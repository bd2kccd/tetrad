package edu.cmu.causality.chartBuilder;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.sample.SemSample;
//import edu.cmu.tetrad.regression.RegressionOld;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.Regression;
import edu.cmu.tetrad.regression.RegressionDataset;
import edu.cmu.tetrad.regression.RegressionResult;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a wrapper class containing the information needed to run regression
 * for a given sample.
 *
 * @author mattheweasterday
 */
public class RegressionInfo {
    static private final NumberFormat nf;

    static {
        nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
    }


    private final String EXPERIMENT_NAME;
    private final String SAMPLE_NAME;
    private final String RESPONSE_VAR;
    private final String[] PREDICTOR_VARS;

    private RegressionResult REGRESSION_PLANE;

    /**
     * Constructor.
     */
    public RegressionInfo(
            //SemSample sample,
            String experimentName,
            String sampleName,
            String responseVar,
            String[] predictorVars
    ) {

        EXPERIMENT_NAME = experimentName;
        SAMPLE_NAME = sampleName;
        RESPONSE_VAR = responseVar;
        PREDICTOR_VARS = predictorVars;
    }

    /**
     * @return an equation expressing the response variable as a function of
     *         the predictor variable(s) with its coefficient.
     */
    public String getEquation() {
        CausalityLabModel model = CausalityLabModel.getModel();
        RegressionResult results = doRegression();

        String equation = "<html><body>" + RESPONSE_VAR;

        if (model.getExperimentalVariableManipulation(EXPERIMENT_NAME,
                RESPONSE_VAR).getType() != ManipulationType.NONE) {

            equation += "<sub><font color=\"red\">set </font></sub>";
        }

        equation += (" = " + nf.format(results.getCoef()[0]));

        for (int i = 0; i < PREDICTOR_VARS.length; i++) {
            equation = equation + " + " +
                    nf.format(results.getCoef()[i + 1]) + "*" + PREDICTOR_VARS[i];

            if (model.getExperimentalVariableManipulation(EXPERIMENT_NAME,
                    PREDICTOR_VARS[i]).getType() != ManipulationType.NONE) {

                equation += "<sub><font color=\"red\">set </font></sub>";
            }
        }

        equation = equation + "</body></html>";
        return equation;
    }

    /**
     * @return a table model containing all the required regression info to create
     *         a table.
     */
    public RegressionTableModel getRegressionTableModel() {
        return new RegressionTableModel(doRegression(), PREDICTOR_VARS.length);
    }

    /**
     * @return experiment name.
     */
    public String getExptName() {
        return EXPERIMENT_NAME;
    }

    /**
     * @return sample name.
     */
    public String getSampleName() {
        return SAMPLE_NAME;
    }

    /**
     * @return sample size.
     */
    public int getSampleSize() {
        SemSample sample = getSample();
        return sample.getRowCount();
    }

    /**
     * Runs the regression with the response and predictor variables.
     *
     * @return regression results in the form of the RegressionPlane class.
     */
    public RegressionResult doRegression() {
        SemSample sample = getSample();

        if (REGRESSION_PLANE == null) {
            List<Node> regressors = new ArrayList<Node>();

            for (String r : PREDICTOR_VARS) {
                regressors.add(sample.getDataSet().getVariable(r));
            }

            Node target = sample.getDataSet().getVariable("foo");

            Regression regression = new RegressionDataset(sample.getDataSet());

            REGRESSION_PLANE = regression.regress(target, regressors);
        }
        return REGRESSION_PLANE;
    }

    public RegressionResult getRegressionResult() {
        return REGRESSION_PLANE;
    }

    /**
     * @return the SEM sample on which the regression was run.
     */
    private SemSample getSample() {
        return (SemSample) CausalityLabModel.getModel().getSample(EXPERIMENT_NAME, SAMPLE_NAME);
    }

    /**
     * Use this to contain the sample as a wrapper to run the regression.
     *
     * @return an array of doubles representing the sample values.
     */
    private double[] getResponseVarData() {
        SemSample SAMPLE = getSample();

        double[] responseVar = new double[SAMPLE.getRowCount()];

        for (int row = 0; row < SAMPLE.getRowCount(); row++) {
            responseVar[row] = (Double) SAMPLE.getValueAtRowGivenColumnName(row, RESPONSE_VAR);
        }
        return responseVar;
    }


    /**
     * @return the response variable.
     */
    public String getResponseVar() {
        return RESPONSE_VAR;
    }

    /**
     * @return a string array of the predictor variables.
     */
    public String[] getPredictorVars() {
        return PREDICTOR_VARS;
    }

    /* Tablemodel class for creating a table with all the regression statistics */
    public class RegressionTableModel extends AbstractTableModel {

        private final RegressionResult RESULTS;
        private final int NUM_PREDICTOR_VARS;

        public RegressionTableModel(RegressionResult results, int numPredictorVars) {
            RESULTS = results;
            NUM_PREDICTOR_VARS = numPredictorVars;
        }

        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return " ";
                case 1:
                    return "coeff";
                case 2:
                    return "SE coeff";
                case 3:
                    return "t-stats";
                case 4:
                    return "p-value";
            }
            return null;
        }

        public int getColumnCount() {
            return 5;       /* Var name, coefficient, SE coeff, t-stats, p-value */
        }

        public int getRowCount() {
            return NUM_PREDICTOR_VARS + 1;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    String[] regressorNames = RESULTS.getRegressorNames();
                    if (rowIndex == 0) {
                        return "Intercept";
                    } else if (rowIndex <= regressorNames.length) {
                        return regressorNames[rowIndex - 1];
                    }
                case 1:
                    return nf.format(RESULTS.getCoef()[rowIndex]);
                case 2:
                    return nf.format(getSEcoeff(rowIndex));
                case 3:
                    return nf.format(getTstats(rowIndex));
                case 4:
                    return nf.format(RESULTS.getP()[rowIndex]);
            }
            return null;
        }


        private double getSEcoeff(int row) {
            return RESULTS.getSe()[row];
        }


        private double getTstats(int row) {
            return RESULTS.getT()[row];
        }
    }
}
