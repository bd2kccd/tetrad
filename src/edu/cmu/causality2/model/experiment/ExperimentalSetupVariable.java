package edu.cmu.causality2.model.experiment;

//import edu.cmu.causality.experimentalSetup.manipulation.Locked;
//import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;
//import edu.cmu.causality.experimentalSetup.manipulation.None;
//import edu.cmu.causality.experimentalSetup.manipulation.Randomized;

/**
 * This class describes each variable in an experimental setup. It keeps track
 * of its individual manipulation status and whether it's studied (or ignored)
 * in the experiment.
 *
 * @author mattheweasterday
 */
public class ExperimentalSetupVariable {

    private final String name;
    private Manipulation manipulation;
    private boolean isStudied;
    private double mean;
    private double stdDev;

    /**
     * Constructor.
     *
     * @param name the name of the variable.
     */
    public ExperimentalSetupVariable(String name, double mean, double stdDev) {
        // Swapping out U(lower, upper) for N(mean, stdDev) jdramsey 6/12/2013 todo

        this.name = name;
        manipulation = new NoManipulation();
        isStudied = true;
        this.mean = mean;
        this.stdDev = stdDev;

    }

    /**
     * Copy Constructor.
     *
     * @param esv the variable to copy.
     */
    public ExperimentalSetupVariable(ExperimentalSetupVariable esv, double mean, double stdDev) {
        // Swapping out U(lower, upper) for N(mean, stdDev) jdramsey 6/12/2013 todo

        name = esv.getName();
        manipulation = new NoManipulation();
        isStudied = esv.isStudied;
        this.mean = mean;
        this.stdDev = stdDev;

    }

    //======================================================================
    //
    //  GETTERS and SETTERS
    //
    //======================================================================

    /**
     * @return the variable name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the variable manipulation status.
     */
    public Manipulation getManipulation() {
        return manipulation;
    }

    /**
     * @return if the variable is studied in the experimental setup.
     */
    public boolean isStudied() throws IllegalArgumentException {
        return isStudied;
    }

    /**
     * Set this variable to be randomized.
     */
    public void setRandomized() {
//        manipulation = new Randomized();
    }

    public double getMean() {
        return this.mean;
    }

    public double getStandardDeviation() {
        return this.stdDev;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setStandardDeviation(double stdDev) {
        this.stdDev = stdDev;
    }


    /**
     * Set this variable to be locked.
     */
    public void setLocked(String lockedValue) throws IllegalArgumentException {
        Locked l = new Locked();
        l.setLockedAt(lockedValue);
        manipulation = l;
    }

    /**
     * Remove all manipulation on this variable.
     */
    public void setUnmanipulated() throws IllegalArgumentException {
        manipulation = new NoManipulation();
    }

    /**
     * Set this variable to be studied or ignored in the experiment.
     */
    public void setStudied(boolean isStudied) throws IllegalArgumentException {
        this.isStudied = isStudied;
    }
}
