package edu.cmu.causality.chartBuilder;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.sample.SemSample;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.regression.RegressionResult;
import edu.cmu.tetrad.util.RandomUtil;
import edu.cmu.tetrad.util.StatUtils;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * This is the scatterplot model class holding the necessary information to
 * create a scatterplot. It uses Point2D to hold the pair of values need to
 * create the scatterplot.
 *
 * @author Adrian Tang
 */
public class ScatterPlot {
    private final String responseVar;
    private final String predictorVar;
    private final String sampleName;
    private final String experimentName;
    private final String truename;
    private final boolean includeLine;

    private RegressionInfo regression;
    private RegressionResult regressionResult;
    private Double correlation;
    private Vector<Point2D.Double> seivedValues;


    /**
     * Constructor.
     *
     * @param experimentName name of the experiment.
     * @param sampleName     name of the sample to create the scatterplot in.
     * @param includeLine    whether or not to include the regression line in the
     *                       plot.
     * @param responseVar    y-axis variable name.
     * @param predictorVar   x-axis variable name.
     */
    public ScatterPlot(
            String experimentName,
            String sampleName,
            String truename,
            boolean includeLine,
            String responseVar,
            String predictorVar
    ) {

        this.responseVar = responseVar;
        this.predictorVar = predictorVar;
        this.includeLine = includeLine;
        this.experimentName = experimentName;
        this.sampleName = sampleName;
        this.truename = truename;
    }

    private RegressionInfo getRegression() {
        if (regression == null) {
            String[] predictorVars = {predictorVar};
            regression = new RegressionInfo(
                    experimentName,
                    sampleName,
                    responseVar,
                    predictorVars);
        }
        return regression;
    }


    private RegressionResult getRegressionResult() {
        if (regressionResult == null) {
            regressionResult = getRegression().doRegression();
        }
        return regressionResult;
    }

    private SemSample getSample() {
        return (SemSample) CausalityLabModel.getModel()
                .getSample(experimentName, sampleName);
    }

    /**
     * @return the title for this scatterplot.
     */
    public String getTitle() {
        return "<" + responseVar + "> vs <" + predictorVar + ">";   //$NON-NLS-3$
    }

    public String getTrueName() {
        return truename;
    }

    /**
     * @return the correlation coefficient statistics as a double.
     */
    public double getCorrelationCoeff() {
        if (correlation == null) {
            SemSample sample = getSample();
            DataSet dataSet = sample.getDataSet();

            //double[] xData = (double[]) sample.getDataSet().getColumn(predictorVar).getRawData();
            //double[] yData = (double[]) sample.getDataSet().getColumn(responseVar).getRawData();

            List<Node> xDataNode = new LinkedList<Node>();
            List<Node> yDataNode = new LinkedList<Node>();

            xDataNode.add(dataSet.getVariable(predictorVar));
            yDataNode.add(dataSet.getVariable(responseVar));

            double[][] xDataSrc = dataSet.subsetColumns(xDataNode)
                    .getDoubleData().toArray();
            double[][] yDataSrc = dataSet.subsetColumns(yDataNode)
                    .getDoubleData().toArray();
            double[] xData = new double[xDataSrc.length];
            double[] yData = new double[yDataSrc.length];
            arrayCopy(xDataSrc, xData);
            arrayCopy(yDataSrc, yData);

            this.correlation = StatUtils.correlation(xData, yData);
        }
        return correlation;
    }

    /**
     * @return the p-value of the correlation coefficient statistics.
     */
    public double getCorrelationPvalue() {
        double correlation = getCorrelationCoeff();
        double fishersZ = 0.5 * Math.sqrt(getSampleSize() - 3.0) *
                Math.log(Math.abs(1.0 + correlation) / Math.abs(1.0 - correlation));
        return 2.0 * (1.0 - RandomUtil.getInstance().normalCdf(0, 1, Math.abs(fishersZ)));
    }

    /**
     * Copies the first column of the 2-d array into a 1-d array.
     */
    private void arrayCopy(double[][] fromD, double[] toD) {
        for (int i = 0; i < fromD.length; i++) {
            toD[i] = fromD[i][0];
        }
    }

    /**
     * @return the minimum x-axis value from the set of sample values.
     */
    public double getXmin() {
        double min = Double.POSITIVE_INFINITY;
        Vector<Point2D.Double> cleanedSampleValues = getSievedValues();
        for (Object cleanedSampleValue : cleanedSampleValues) {
            min = Math.min(min,
                    ((Point2D.Double) cleanedSampleValue).getX());
        }
        return min;
    }

    /**
     * @return the minimum y-axis value from the set of sample values.
     */
    public double getYmin() {
        double min = Double.POSITIVE_INFINITY;
        Vector<Point2D.Double> cleanedSampleValues = getSievedValues();
        for (Object cleanedSampleValue : cleanedSampleValues) {
            min = Math.min(min,
                    ((Point2D.Double) cleanedSampleValue).getY());
        }
        return min;
    }

    /**
     * @return the maximum x-axis value from the set of sample values.
     */
    public double getXmax() {
        double max = Double.NEGATIVE_INFINITY;
        Vector<Point2D.Double> cleanedSampleValues = getSievedValues();
        for (Object cleanedSampleValue : cleanedSampleValues) {
            max = Math.max(max,
                    ((Point2D.Double) cleanedSampleValue).getX());
        }
        return max;
    }

    /**
     * @return the maximum y-axis value from the set of sample values.
     */
    public double getYmax() {
        double max = Double.NEGATIVE_INFINITY;
        Vector<Point2D.Double> cleanedSampleValues = getSievedValues();
        for (Object cleanedSampleValue : cleanedSampleValues) {
            max = Math.max(max,
                    ((Point2D.Double) cleanedSampleValue).getY());
        }
        return max;
    }

    /**
     * Seives through the sample values and grabs only the values for the
     * response and predictor variables.
     *
     * @return a vector containing the filtered values.
     */
    public Vector<Point2D.Double> getSievedValues() {
        if (seivedValues == null) {
            SemSample sample = getSample();
            seivedValues = sieveOutValues(sample, responseVar, predictorVar);
        }
        return seivedValues;
    }

    /**
     * @return name of the experiment.
     */
    public String getExptName() {
        return experimentName;
    }

    /**
     * @return name of the sample.
     */
    public String getSampleName() {
        return sampleName;
    }

    /**
     * @return size of the sample.
     */
    public int getSampleSize() {
        return getSievedValues().size();
    }

    /**
     * @return the name of the predictor variable.
     */
    public String getXvar() {
        return predictorVar;
    }

    /**
     * @return the name of the response variable.
     */
    public String getYvar() {
        return responseVar;
    }

    /**
     * @return whether or not to include the regression line.
     */
    public boolean getIncludeLine() {
        return includeLine;
    }


    /*** Correlation coefficient statistics (Simple linear regression) ***/
    /**
     * Calculates the regression coefficient for the variables
     * return a correlation coeff
     */
    public double getRegressionCoeff() {
        return getRegressionResult().getCoef()[1];
    }

    /**
     * @return the zero intercept of the regression equation.
     */
    public double getRegressionZeroIntrpt() {
        return getRegressionResult().getCoef()[0];
    }

    /**
     * @return the regression equation.
     */
    public String getEquation() {
        return getRegression().getEquation();
    }

    private Vector<Point2D.Double> sieveOutValues(SemSample sample, String response, String predictor) {
        Point2D.Double pt;
        Vector<Point2D.Double> cleanedVals = new Vector<Point2D.Double>();

        for (int row = 0; row < sample.getRowCount(); row++) {
            pt = new Point2D.Double();
            pt.setLocation(((Double) sample.getValueAtRowGivenColumnName(row, predictor)).doubleValue(),
                    ((Double) sample.getValueAtRowGivenColumnName(row, response)).doubleValue());

            cleanedVals.add(pt);
        }
        return cleanedVals;
    }

}
