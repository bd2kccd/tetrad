package edu.cmu.causalityApp.dataEditors;

/**
 * This class allows us to display the combo box items as the name of the experiment, but
 * still hold on to the experiment Id for use in accessing the model.
 */
class ComboBoxName {
    private final String experimentName;

    /**
     * Constructor.
     *
     * @param name
     */
    public ComboBoxName(String name) {
        experimentName = name;
    }

    /**
     * @return the getModel experimental setup name.
     */
    public String toString() {
        return getExperimentName();
    }

    /**
     * @return the getModel experimental setup name.
     */
    public String getExperimentName() {
        return experimentName;
    }

}