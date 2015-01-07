package edu.cmu.causality.chartBuilder;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.manipulation.ManipulationType;
import edu.cmu.causality.population.BayesMarginalJoint;
import edu.cmu.causality.sample.BayesSample;

import java.util.Properties;


/**
 * This is the histogram model class to hold all the necessary information to
 * create a histogram.
 *
 * @author Adrian Tang
 */
public class Histogram {

    // This is the number of divisions on the y-axis
    private static final int GET_NUM_RANGE_GROUP = 4;

    private final String[] varsToChart;
    private final String[] condVars;
    private final String[] condState;
    private final String sampleName;
    private final String experimentName;
    private final String truemodelName;
    private BayesSample sample;

    /**
     * Constructor.
     * For eg, when creating a histogram of Fr(X,Y | Za=0 & Zb=1),
     * varsToChart = {X, Y}
     * condVars = {Za, Zb}
     * condState = {0, 1}
     *
     * @param exptName    name of experiment.
     * @param sampleName  name of sample.
     * @param varsToChart the string array of variable names to be charted.
     * @param condVars    the string array of variables name to be used as conditioning
     *                    variables.
     * @param condState   the string array of the corresponding conditioning variable
     *                    values.
     */
    public Histogram(String exptName,
                     String sampleName,
                     String tmodelname,
                     String[] varsToChart,
                     String[] condVars,
                     String[] condState) {

        this.varsToChart = varsToChart;
        this.condVars = condVars;
        this.condState = condState;
        this.sampleName = sampleName;
        this.experimentName = exptName;
        this.truemodelName = tmodelname;

        this.setSample((BayesSample) CausalityLabModel.getModel().getSample(exptName, sampleName));
        if (this.getSample() == null) return; // A problem with the XML parser
        HistogramStudiedVariables studiedVariables = new HistogramStudiedVariables(getSample().getMarginalJoint().getIm().getBayesPm(),
                varsToChart);
        getSample().setVariablesStudied(studiedVariables);
    }

    /**
     * Given certain constraint variables, calculate the size of n (sample size).
     * For example, for P(education | income = high), 'n' will be the number of sample
     * cases whose income is high. If no constraint variables, n = total sample size
     */
    public int getSampleSize() {
        return (int) (getCondProb() * (double) getSample().getNumSampleCases());
    }

    /**
     * @return string array of the variable names to be charted in the histogram.
     */
    public String[] getChartedVarNames() {
        return getVarsToChart();
    }

    /**
     * @return string array of the conditioning variable names.
     */
    public String[] getConditionedVarNames() {
        return getCondVars();
    }

    /**
     * @return a bayesian sample with corresponding experiment name and sample
     *         name.
     */
    private BayesSample getSample() {
        return sample;

    }

    /**
     * Given certain constraint variables and their values, calculate their independent
     * probability for use to calculate the general conditional probability
     *
     * @return the probability of the variables of the specified values
     */
    private double getCondProb() {


        Properties varNameValue = new Properties();
        if (getCondVars().length == 0) return 1.0;

        for (int i = 0; i < getCondVars().length; i++) {
            varNameValue.put(getCondVars()[i], getCondState()[i]);
        }

//        BayesSample sample = getSample();
        return getSample().getHistogramProbabilityOfVarValue(varNameValue);
    }

    /**
     * @return total number of states there are given the constraint variables
     *         conditional variables
     */
    public int getNumStates() {
        BayesMarginalJoint joint = getSample().getMarginalJoint();
        return joint.getNumRows();
    }

    /**
     * Eg. If the number of divisions on the y-axis is 4, the tick marks on the
     * y-axis will be 0.25, 0.50, 0.75 and 1.
     *
     * @return number of divisions the y-axis has.
     */
    public int getNumRangeGroups() {
        return GET_NUM_RANGE_GROUP;
    }

    /**
     * Given the number i, returns the corresponding relative frequency of the
     * histogram bar for that particular state. Ranges from 0 to 1
     *
     * @return relative frequency for that particular state i
     */
    public double getStateValue(int i) {
        Properties varNameValue = new Properties();

        String[] str = getSample().getSampleCaseFrequencyCombination(i);
        String value;

        for (int j = 0; j < str.length; j++) {
            varNameValue.setProperty(getVarsToChart()[j], str[j]);
        }

        /* Check for conditional constraints */
        if (getCondVars().length != 0) {
            for (int j = 0; j < getCondVars().length; j++) {
                value = varNameValue.getProperty(getCondVars()[j]);
                if (value == null) {
                    varNameValue.setProperty(getCondVars()[j], (getCondState()[j]));
                } else if (!value.equals(getCondState()[j])) {
                    return 0;       // no intersection at all
                }
            }
        }

        return (getSample().getHistogramProbabilityOfVarValue(varNameValue) / getCondProb());
    }

    /**
     * Given the number i, returns the corresponding name of the state to chart.
     * For example, if variables to chart is income and education, one state name
     * may be "high, college"
     *
     * @return state name
     */
    public String getStateName(int i) {
        BayesSample sample = getSample();
        String[] str = sample.getSampleCaseFrequencyCombination(i);

        String str2 = "";
        for (int j = 0; j < str.length; j++) {
            if (j == 0) {
                str2 += str[j];
            } else {
                str2 += (", " + str[j]);
            }
        }
        return str2;
    }

    /**
     * @return the string representation of the names of the variables that are
     *         charted
     */
    public String getIndependentsAsString() {
        String str = "";
        for (String aVarsToChart : getVarsToChart())
            if (str.equals("")) {
                str += aVarsToChart;
            } else {
                str += (" x " + aVarsToChart);
            }
        return str;
    }

    /**
     * For a sample in an observational experiment, the title will be in black.
     * For a sample in an experiment with manipulated variable(s), if the
     * manipulated variable is charted, the title will add a red word "set" to
     * the manipulated value to indicate the manipulation.
     * <p/>
     * For eg, in an experiment where Y is locked at value 0, the title of the
     * histogram charting variable X, conditioning at Y=0 will be "Fr(X | Y=set_0)"
     *
     * @return String html representation of the histogram title.
     */
    public String getTitle() {
        CausalityLabModel model = CausalityLabModel.getModel();

        String str = "<html><body>Fr(";
        for (String aVarsToChart : getVarsToChart()) {
            if (str.equals("<html><body>Fr(")) {
                str += aVarsToChart;
            } else {
                str += (", " + aVarsToChart);
            }
        }
        String str2 = "";
        String varName;
        for (String condVar : getCondVars()) {
            varName = condVar;

            if (!str2.equals("")) {
                str2 += " & ";
            }

            str2 += (varName + "=");

            if (model.getExperimentalVariableManipulation(getExperimentName(), varName).getType()
                    != ManipulationType.NONE) {
                str2 += "<sub><font color=\"red\">set </font></sub>";
            }
            str2 += getCondVarState(varName);
        }

        if (str2.equals("")) {
            return (str + ")</body></html>");
        } else {
            return (str + " | " + str2 + ")</body></html>");
        }
    }

    /**
     * @return name of the associated experiment.
     */
    public String getExptName() {
        return getExperimentName();
    }

    /**
     * @return name of the associated sample.
     */
    public String getSampleName() {
        return sampleName;
    }

    public String getTrueName() {
        return getTruemodelName();
    }

    /**
     * @param varName the conditioning variable name.
     * @return value of the corresponding conditioning variable name.
     */
    public String getCondVarState(String varName) {
        for (int i = 0; i < getCondVars().length; i++) {
            if (getCondVars()[i].equals(varName)) {
                return getCondState()[i];
            }
        }
        return null;
    }

    public String[] getVarsToChart() {
        return varsToChart;
    }

    public String[] getCondVars() {
        return condVars;
    }

    public String[] getCondState() {
        return condState;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getTruemodelName() {
        return truemodelName;
    }

    public void setSample(BayesSample sample) {
        this.sample = sample;
    }
}
