///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.regression;

import edu.cmu.tetrad.data.ColtDataSet;
import edu.cmu.tetrad.data.ContinuousVariable;
import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.*;
import edu.cmu.tetrad.util.TetradSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a logistic regression algorithm based on a Javascript
 * implementation by John Pezzullo.  That implementation together with a
 * description of logistic regression and some examples appear on his web page
 * http://members.aol.com/johnp71/logistic.html
 * <p/>
 * See also  Applied Logistic Regression, by D.W. Hosmer and S. Lemeshow. 1989,
 * John Wiley & Sons, New York which Pezzullo references.  In particular see
 * pages 27-29.
 *
 * @author Frank Wimberly
 */
public class LogisticRegression implements TetradSerializable {
    static final long serialVersionUID = 23L;

    /**
     * The data set that was supplied.
     */
    private DataSet dataSet;

    /**
     * The number formatter used for printing reports.
     */
    private NumberFormat nf;

    /**
     * The default alpha level which may be specified otherwise in the GUI
     */
    private double alpha = 0.05;

    /**
     * The raw data for the regressors is stored in this array.
     */
    private double[][] regressors;

    /**
     * Sample size of the data set.
     */
    private int sampleSize;

    /**
     * Names of the regressors.
     */
    private String[] regressorNames;

    /**
     * A graph in which the target is the child of each significant regressor
     */
    private Graph outGraph;

    /**
     * Contains values computed by the regress method, including regression
     * coefficients, p values, etc.
     */
    private Result result;

    /**
     * Coefficients associated with regressors.  The ratio of a coefficient to
     * its standard error has a normal distribution.
     */
    private double[] coefficients;  //coefficients associated with regressors.

    /**
     * P values of the regressors.
     */
    private double[] pValues;

    /**
     * Z scores of the regressors.
     */
    private double[] zScores;

    /**
     * A printable report for the regression.
     */
    private String report;

    /**
     * The data converted into column major, to avoid unnecessary copying.
     */
    private double[][] dataCols;

    private int[] rows;

    /**
     * A mixed data set. The targets of regresson must be binary. Regressors must be continuous or binary.
     * Other variables don't matter.
     */
    public LogisticRegression(DataSet dataSet) {
        this.dataSet = dataSet;
        nf = new DecimalFormat("0.0000");
        dataCols = dataSet.getDoubleData().transpose().toArray();
        setRows(new int[dataSet.getNumRows()]);
        for (int i = 0; i < getRows().length; i++) getRows()[i] = i;
    }

    /**
     * Generates a simple exemplar of this class to test serialization.
     *
     * @see edu.cmu.TestSerialization
     * @see edu.cmu.tetradapp.util.TetradSerializableUtils
     */
    public static LogisticRegression serializableInstance() {
        return new LogisticRegression(ColtDataSet.serializableInstance());
    }

    /**
     * x must be binary; regressors must be continuous or binary.
     */
    public LogisticRegression.Result regress(DiscreteVariable x, List<Node> regressors) {
        if (!binary(x)) {
            throw new IllegalArgumentException("Target must be binary.");
        }

        for (Node node : regressors) {
            if (!(node instanceof ContinuousVariable || binary(node))) {
                throw new IllegalArgumentException("Regressors must be continuous or binary.");
            }
        }

        double[][] _regressors = new double[regressors.size()][getRows().length];

        for (int j = 0; j < regressors.size(); j++) {
            int col = dataSet.getColumn(regressors.get(j));
            double[] dataCol = dataCols[col];

            for (int i = 0; i < getRows().length; i++) {
                _regressors[j][i] = dataCol[getRows()[i]];
            }
        }

        setRegressors(_regressors);

        int[] target = new int[getRows().length];
        int col = dataSet.getColumn(dataSet.getVariable(x.getName()));

        for (int i = 0; i < getRows().length; i++) {
            target[i] = dataSet.getInt(getRows()[i], col);
        }

        List<String> regressorNames = new ArrayList<String>();

        for (Node node : regressors) {
            regressorNames.add(node.getName());
        }

        this.regressorNames = new String[regressorNames.size()];

        for (int i = 0; i < regressorNames.size(); i++) {
            this.regressorNames[i] = regressorNames.get(i);
        }

        regress(target, x.getName());
        return getResult();
    }

    private boolean binary(Node x) {
        return x instanceof DiscreteVariable && ((DiscreteVariable) x).getNumCategories() == 2;
    }


    /**
     * Sets the regressor data to the given array. The array should be column
     * major, and the columns should all be the same length. Variable names are
     * set to null each time a new regressor data array is set.
     *
     * @param regressors the regressor data.
     */
    private void setRegressors(double[][] regressors) {

        if (regressors == null) {
            throw new NullPointerException("Regressor data must not be null.");
        }

        this.sampleSize = regressors.length == 0 ? 0 : regressors[0].length;

        for (double[] regressor : regressors) {
            if (regressor == null) {
                throw new NullPointerException(
                        "All regressor columns must be non-null.");
            }

            if (regressor.length != sampleSize) {
                throw new IllegalArgumentException(
                        "Regressor data must all be the same length.");
            }
        }

        this.regressors = regressors;
        this.regressorNames = null;
    }

    /**
     * Returns the array of variable names. This is a String[] array, the length
     * of which is equal to the number of columns in the regressor data array.
     */
    public String[] getVariableNames() {
        return regressorNames;
    }

    /**
     * Regresses the single-column target onto the regressors which have been
     * previously set, generating a regression result.
     * <p/>
     * The target must be a two-valued variable with values 0 and 1.
     * <p/>
     * This implements an iterative search.
     */
    public void regress(int[] target, String targetName) {
        String report = "";

        double[][] x;
        double[] c1;

        //int k = regressors.length + 1;   //One more than the number of regressors?

        if (sampleSize != 0 && target.length != sampleSize) {
            //System.out.println("sampleSize = " + sampleSize + "target length = " + target.length);
            throw new IllegalArgumentException(
                    "Target sample size must match regressor sample size.");
        }

        //The output graph will contain the target node and nodes corresponding to variables
        //with significant correlations with the target together with edges from them to the
        //target.
        outGraph = new EdgeListGraph();
        Node targetNode = new GraphNode(targetName);
        outGraph.addNode(targetNode);

        //try {

        int numRegressors = regressors.length;
        int numCases = target.length;

        // make a new matrix x with all the columns of regressors
        // but with first column all 1.0's.
        x = new double[numRegressors + 1][];
        c1 = new double[numCases];

        x[0] = c1;

        System.arraycopy(regressors, 0, x, 1, numRegressors);

        for (int i = 0; i < numCases; i++) {
            x[0][i] = 1.0;
            c1[i] = 1.0;
        }

        double[] xMeans = new double[numRegressors + 1];
        double[] xStdDevs = new double[numRegressors + 1];

        double[] y0 = new double[numCases];
        double[] y1 = new double[numCases];
        for (int i = 0; i < numCases; i++) {
            y0[i] = 0;
            y1[i] = 0;
        }

        int ny0 = 0;
        int ny1 = 0;
        int nc = 0;

        for (int i = 0; i < numCases; i++) {
            if (target[i] == 0.0) {
                y0[i] = 1;
                ny0++;
            } else {
                y1[i] = 1;
                ny1++;
            }
            nc += y0[i] + y1[i];
            for (int j = 1; j <= numRegressors; j++) {
                //System.out.println("case " + i + " ar " + j + " = " + x[j][i]);
                xMeans[j] += (y0[i] + y1[i]) * x[j][i];
                xStdDevs[j] += (y0[i] + y1[i]) * x[j][i] * x[j][i];
            }
        }

        report = report + (ny0 + " cases have " + targetName + " = 0; " + ny1 +
                " cases have " + targetName + " = 1.\n");
        //if(nc != numCases) System.out.println("nc NOT numCases");
        report = report + ("\tVariable\tAvg\tSD\n");

        for (int j = 1; j <= numRegressors; j++) {
            xMeans[j] /= nc;
            xStdDevs[j] /= nc;
            xStdDevs[j] = Math.sqrt(Math.abs(xStdDevs[j] - xMeans[j] * xMeans[j]));
            report = report + ("\t" + this.regressorNames[j - 1] + "\t" +
                    nf.format(xMeans[j]) + "\t" + nf.format(xStdDevs[j]) +
                    "\n");
        }
        xMeans[0] = 0.0;
        xStdDevs[0] = 1.0;

        for (int i = 0; i < nc; i++) {
            for (int j = 1; j <= numRegressors; j++) {
                x[j][i] = (x[j][i] - xMeans[j]) / xStdDevs[j];
            }
        }

        //report = report + ("Iteration history...\n");

        double[] par = new double[numRegressors + 1];
        double[] parStdErr = new double[numRegressors + 1];
        coefficients = new double[numRegressors + 1];

        par[0] = Math.log((double) ny1 / (double) ny0);
        for (int j = 1; j <= numRegressors; j++) {
            par[j] = 0.0;
        }

        double[][] arr = new double[numRegressors + 1][numRegressors + 2];

        double lnV;
        double ln1mV;

        double llP = 2e+10;
        double ll = 1e+10;
        double llN = 0.0;

        while (Math.abs(llP - ll) > 1e-7) {   /// 1e-7

            llP = ll;
            ll = 0.0;

            for (int j = 0; j <= numRegressors; j++) {
                for (int k = j; k <= numRegressors + 1; k++) {
                    arr[j][k] = 0.0;
                }
            }

            for (int i = 0; i < nc; i++) {
                double q;
                double v = par[0];

                for (int j = 1; j <= numRegressors; j++) {
                    v += par[j] * x[j][i];
                }

                if (v > 15.0) {
                    lnV = -Math.exp(-v);
                    ln1mV = -v;
                    q = Math.exp(-v);
                    v = Math.exp(lnV);
                } else {
                    if (v < -15.0) {
                        lnV = v;
                        ln1mV = -Math.exp(v);
                        q = Math.exp(v);
                        v = Math.exp(lnV);
                    } else {
                        v = 1.0 / (1 + Math.exp(-v));
                        lnV = Math.log(v);
                        ln1mV = Math.log(1.0 - v);
                        q = v * (1.0 - v);
                    }
                }

                ll = ll - 2.0 * y1[i] * lnV - 2.0 * y0[i] * ln1mV;

                for (int j = 0; j <= numRegressors; j++) {
                    double xij = x[j][i];
                    arr[j][numRegressors + 1] +=
                            xij * (y1[i] * (1.0 - v) + y0[i] * (-v));

                    for (int k = j; k <= numRegressors; k++) {
                        arr[j][k] += xij * x[k][i] * q * (y0[i] + y1[i]);
                    }
                }
            }

            //report = report + ("-2 Log Likelihood = " + ll + "\n");

            if (llP == 1e+10) {
                llN = ll;
                //report = report + (" (Null Model)\n");
                //break;
            }

            for (int j = 1; j <= numRegressors; j++) {
                for (int k = 0; k < j; k++) {
                    arr[j][k] = arr[k][j];
                }
            }

            for (int i = 0; i <= numRegressors; i++) {
                double s = arr[i][i];
                arr[i][i] = 1.0;
                for (int k = 0; k <= numRegressors + 1; k++) {
                    arr[i][k] = arr[i][k] / s;
                }

                for (int j = 0; j <= numRegressors; j++) {
                    if (i != j) {
                        s = arr[j][i];
                        arr[j][i] = 0.0;
                        for (int k = 0; k <= numRegressors + 1; k++) {
                            arr[j][k] = arr[j][k] - s * arr[i][k];
                        }
                    }
                }
            }

            for (int j = 0; j <= numRegressors; j++) {
                par[j] += arr[j][numRegressors + 1];
            }
        }

        //report = report + (" (Converged) \n");

        EdgeListGraph outgraph = new EdgeListGraph();
        Node targNode = new GraphNode(targetName);
        outgraph.addNode(targNode);

        double chiSq = llN - ll;
        report = report + ("Overall Model Fit...\n");
        report = report + ("  Chi Square = " + nf.format(chiSq) + "; df = " +
                numRegressors + "; " + "p = " +
                nf.format(chiSquare(chiSq, numRegressors)) + "\n");
        report = report + ("\nCoefficients and Standard Errors...\n");
        report = report + (" Variable\tCoeff.\tStdErr\tprob.\tsig.\n");

        //Indicates whether each coefficient is significant at the alpha level.
        String[] sigMarker = new String[numRegressors];
        pValues = new double[numRegressors + 1];
        zScores = new double[numRegressors + 1];

        for (int j = 1; j <= numRegressors; j++) {
            par[j] = par[j] / xStdDevs[j];
            parStdErr[j] = Math.sqrt(arr[j][j]) / xStdDevs[j];
            par[0] = par[0] - par[j] * xMeans[j];
            double zScore = par[j] / parStdErr[j];
            double prob = norm(Math.abs(zScore));


            getpValues()[j] = prob;
            getZScores()[j] = zScore;

            if (prob < alpha) {
                sigMarker[j - 1] = "*";
                Node predNode = new GraphNode(this.regressorNames[j - 1]);
                outgraph.addNode(predNode);
                Edge newEdge = new Edge(predNode, targNode, Endpoint.TAIL,
                        Endpoint.ARROW);
                outgraph.addEdge(newEdge);
            } else {
                sigMarker[j - 1] = "";
            }

            report = report + (this.regressorNames[j - 1] + "\t" + nf.format(par[j]) +
                    "\t" + nf.format(parStdErr[j]) + "\t" + nf.format(prob) +
                    "\t" + sigMarker[j - 1] + "\n");
        }

        parStdErr[0] = Math.sqrt(arr[0][0]);
        double zScore = par[0] / parStdErr[0];
        getpValues()[0] = norm(Math.abs(zScore));
        getZScores()[0] = zScore;

        double intercept = par[0];
        report = report + ("\nIntercept = " + nf.format(intercept) + "\n");

        //return new RegressionResult(false, vNames, regressors.length, n, b[0],
        //        t, p, r2, coefSE, summary);
        setOutGraph(outgraph);
        setCoefficients(par);

        this.result = new Result(targetName,
                this.regressorNames, xMeans, xStdDevs, numRegressors, ny0, ny1, coefficients,
                parStdErr, getpValues(), intercept, report, ll
        );

        this.report = report;
    }

    private double chiSquare(double x, int n) {

        if (x > 1000.0 || n > 1000) {
            double q = norm((Math.pow(x / n, 1.0 / 3.0) + 2.0 / (9.0 * n) -
                    1.0) / Math.sqrt(2.0 / (9.0 * n))) / 2.0;
            if (x > n) {
                return q;
            } else {
                return 1.0 - q;
            }
        }

        double p = Math.exp(-0.5 * x);
        if (n % 2 == 1) {
            p = p * Math.sqrt(2.0 * x / Math.PI);
        }

        int k = n;
        while (k >= 2) {
            p = p * x / k;
            k = k - 2;
        }

        double t = p;
        int a = n;
        while (t > p * 1e-15) {
            a = a + 2;
            t = t * x / a;
            p = p + t;
        }

        return 1.0 - p;
    }

    private double norm(double z) {
        double q = z * z;
        double piOver2 = Math.PI / 2.0;

        if (Math.abs(z) > 7.0) {
            return (1.0 - 1.0 / q + 3.0 / (q * q)) * Math.exp(-q / 2.0) /
                    (Math.abs(z) * Math.sqrt(piOver2));
        } else {
            return chiSquare(q, 1);
        }

    }

    private void setCoefficients(double[] c) {
        System.arraycopy(c, 0, coefficients, 0, c.length);
    }

    private void setOutGraph(Graph g) {
        outGraph = g;
    }

    /**
     * Returns the results computed by the regress method.  These are the values
     * which appear in the printed report.
     *
     * @return result
     */
    public Result getResult() {
        return result;
    }

    public String getReport() {
        return report;
    }

    /**
     * @return The graph with a node for the target variable and edges from
     *         significant regressors (if any) to the target.
     */
    public Graph getOutGraph() {
        return outGraph;
    }

    /**
     * Returns the alpha level.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha level.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Returns the sample size.
     */
    public int getSampleSize() {
        return sampleSize;
    }

    public double[] getpValues() {
        return pValues;
    }

    public double[] getZScores() {
        return zScores;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    /**
     * The rows in the data used for regression.
     */
    public int[] getRows() {
        return rows;
    }

    public void setRows(int[] rows) {
        this.rows = rows;
    }

    public class Result implements TetradSerializable {
        static final long serialVersionUID = 23L;

        /**
         * String representation of the result
         */
        private String result;


        /**
         * The variables.
         */
        private String[] variableNames;


        /**
         * The target.
         */
        private String target;

        /**
         * The number of data points with target = 0.
         */
        private int ny0;

        /**
         * The number of data points with target = 1.
         */
        private int ny1;


        /**
         * The number of regressors.
         */
        private int numRegressors;

        /**
         * The array of regression coefficients.
         */
        private double[] coefs;

        /**
         * The array of standard errors for the regression coefficients.
         */
        private double[] stdErrs;

        /**
         * The array of coefP-values for the regression coefficients.
         */
        private double[] probs;


        /**
         * THe array of means.
         */
        private double[] xMeans;


        /**
         * The array of standard devs.
         */
        private double[] xStdDevs;


        private double intercept;

        /**
         * The log likelihood of the regression
         */
        private double logLikelihood;


        /**
         * Constructs a new LinRegrResult.
         *
         * @param ny0           the number of cases with target = 0.
         * @param ny1           the number of cases with target = 1.
         * @param numRegressors the number of regressors
         * @param coefs         the array of regression coefficients.
         * @param stdErrs       the array of std errors of the coefficients.
         * @param probs         the array of P-values for the regression
         *                      coefficients.
         */
        public Result(String target, String[] variableNames, double[] xMeans, double[] xStdDevs,
                      int numRegressors, int ny0, int ny1, double[] coefs,
                      double[] stdErrs, double[] probs, double intercept, String result, double logLikelihood) {


            if (variableNames.length != numRegressors) {
                throw new IllegalArgumentException();
            }

            if (coefs.length != numRegressors + 1) {
                throw new IllegalArgumentException();
            }

            if (stdErrs.length != numRegressors + 1) {
                throw new IllegalArgumentException();
            }

            if (probs.length != numRegressors + 1) {
                throw new IllegalArgumentException();
            }

            if (xMeans.length != numRegressors + 1) {
                throw new IllegalArgumentException();
            }

            if (xStdDevs.length != numRegressors + 1) {
                throw new IllegalArgumentException();
            }
            if (target == null) {
                throw new NullPointerException();
            }

            this.intercept = intercept;
            this.target = target;
            this.xMeans = xMeans;
            this.xStdDevs = xStdDevs;
            this.variableNames = variableNames;
            this.numRegressors = numRegressors;
            this.ny0 = ny0;
            this.ny1 = ny1;
            this.coefs = coefs;
            this.stdErrs = stdErrs;
            this.probs = probs;
            this.result = result;
            this.logLikelihood = logLikelihood;
        }

        /**
         * Generates a simple exemplar of this class to test serialization.
         *
         * @see edu.cmu.TestSerialization
         * @see edu.cmu.tetradapp.util.TetradSerializableUtils
         */
        public Result serializableInstance() {
            double[] values = new double[]{1.0, 2.0};
            return new Result("X1", new String[]{"X2"}, values, values, 1, 2, 3,
                    values, values, values, 1.5, "", 0.0);
        }

        //================================== Public Methods =======================================//

        public String getTarget() {
            return this.target;
        }

        public double getIntercept() {
            return this.intercept;
        }

        /**
         * Returns the number of regressors.
         */
        public int getNumRegressors() {
            return numRegressors;
        }

        /**
         * Returns the number of cases with target = 0.
         */
        public int getNy0() {
            return ny0;
        }

        /**
         * Returns the number of cases with target = 1.
         */
        public int getNy1() {
            return ny1;
        }

        /**
         * Returns the array of strings containing the variable names.
         */
        public String[] getVariableNames() {
            return variableNames;
        }

        /**
         * Returns the array of regression coeffients.
         */
        public double[] getCoefs() {
            return coefs;
        }

        /**
         * Returns the array of coefT-statistics for the regression coefficients.
         */
        public double[] getStdErrs() {
            return stdErrs;
        }

        /**
         * Returns the array of coefP-values for the regression coefficients.
         */
        public double[] getProbs() {
            return probs;
        }

        public String[] getVarNames() {
            return variableNames;
        }

        public String toString() {
            return result;
        }

        public double[] getxMeans() {
            return xMeans;
        }

        public double[] getxStdDevs() {
            return xStdDevs;
        }

        /**
         * Returns -2LogLiklihood
         */
        public double getLogLikelihood() {
            return logLikelihood;
        }

        /**
         * Adds semantic checks to the default deserialization method. This method
         * must have the standard signature for a readObject method, and the body of
         * the method must begin with "s.defaultReadObject();". Other than that, any
         * semantic checks can be specified and do not need to stay the same from
         * version to version. A readObject method of this form may be added to any
         * class, even if Tetrad sessions were previously saved out using a version
         * of the class that didn't include it. (That's what the
         * "s.defaultReadObject();" is for. See J. Bloch, Effective Java, for help.
         *
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            if (this.xMeans == null) {
                throw new NullPointerException();
            }
            if (this.xStdDevs == null) {
                throw new NullPointerException();
            }
            if (this.variableNames == null) {
                throw new NullPointerException();
            }
            if (this.coefs == null) {
                throw new NullPointerException();
            }
            if (this.stdErrs == null) {
                throw new NullPointerException();
            }
            if (this.probs == null) {
                throw new NullPointerException();
            }
            if (this.result == null) {
                throw new NullPointerException();
            }
            if (target == null) {
                throw new NullPointerException();
            }

        }
    }

}





