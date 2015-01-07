package edu.cmu.causalityApp.chartBuilder;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.chartBuilder.HistogramStudiedVariables;
import edu.cmu.causality.experimentalSetup.manipulation.Locked;
import edu.cmu.causality.experimentalSetup.manipulation.Manipulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


/**
 * This class describes the wizard to select the type of variables to include
 * in the histogram, as well as the conditioning states. It will then launch
 * at least one HistogramChartFrame which contains one HistogramChartView each.
 *
 * @author Adrian Tang
 */
public class HistogramBuilder extends ChartBuilder {

    private final HashMap<String, JComboBox> comboBoxes;
    private final HashMap<String, JCheckBox> condVars;
    private final HashMap<String, JCheckBox> varsToInclude;
    private final HistogramStudiedVariables histogramStudiedVariables;
    private final int sampleId;

    /**
     * Constructor
     *
     * @param model    the Lab model needed to get the parametric model
     * @param sampleId the unique id of the sample in question
     */
    public HistogramBuilder(CausalityLabModel model, int sampleId) {
        super(model.getExperimentNameForSampleId(sampleId),
                sampleId, "Create Histogram (Bayes Net Model)");

        varsToInclude = new HashMap<String, JCheckBox>();
        condVars = new HashMap<String, JCheckBox>();
        comboBoxes = new HashMap<String, JComboBox>();
        String expName = model.getExperimentNameForSampleId(sampleId);
        histogramStudiedVariables = new HistogramStudiedVariables(
                model.getCorrectGraphBayesPmCopy(),
                model.getExperimentalSetupStudiedVariablesNames(expName));
        //expName);

        this.sampleId = sampleId;

        setContentPane(makePage());
        pack();
    }

    /**
     * Creates the histogram based on the selections the user chooses
     */
    protected void createChart() {
        JCheckBox jcheckbox;
        Vector<String> varsToChart = new Vector<String>();  // variables to be charted
        Vector<String> condVars = new Vector<String>();     // conditioning variables
        Vector<String> condState = new Vector<String>();    // conditioning states of the conditional variables
        String varName;

        /* Gets data from all the selections */
        for (int i = 0; i < histogramStudiedVariables.getNumVars(); i++) {
            varName = histogramStudiedVariables.getVarName(i);
            jcheckbox = varsToInclude.get(varName);
            if (jcheckbox.isSelected()) {
                varsToChart.add(varName);
            }

            jcheckbox = this.condVars.get(varName);
            if (jcheckbox.isSelected()) {
                String selectedVal = (String) (comboBoxes.get(varName)).getSelectedItem();

                if (isCondVarValueValid(varName, selectedVal)) {
                    condVars.add(varName);
                    condState.add(selectedVal);
                } else {
                    return;
                }
            }
        }


        String[] vtc = new String[varsToChart.size()];
        for (int i = 0; i < varsToChart.size(); i++) {
            vtc[i] = varsToChart.get(i);
        }

        String[] cv = new String[condVars.size()];
        for (int i = 0; i < condVars.size(); i++) {
            cv[i] = condVars.get(i);
        }

        String[] cs = new String[condState.size()];
        for (int i = 0; i < condState.size(); i++) {
            cs[i] = condState.get(i);
        }

        CausalityLabModel model = CausalityLabModel.getModel();

        CreateHistogramCommand chc = new CreateHistogramCommand(
                getDesktopPane(),
                model.getExperimentNameForSampleId(sampleId),
                model.getSampleName(sampleId),
                vtc,
                cv,
                cs,
                (int) getLocation().getX() + 15,
                (int) getLocation().getY() + 15);
        chc.doIt();

        dispose();
    }

    /**
     * Creates the selection options for the user to select from the various
     * variables to include in the chart as well as the conditioning states
     *
     * @return JPanel chooser pane
     */
    protected JPanel createChooser() {
        JPanel chooser = new JPanel();
        JPanel varsP = new JPanel();
        JPanel condVarsP = new JPanel();
        String varName;

        chooser.add(varsP, BorderLayout.WEST);
        chooser.add(Box.createHorizontalStrut(30), BorderLayout.CENTER);
        chooser.add(condVarsP, BorderLayout.EAST);
        varsP.setLayout(new BoxLayout(varsP, BoxLayout.Y_AXIS));
        condVarsP.setLayout(new BorderLayout());

        /* List variables that are studied in the experiment */
        varsP.add(new JLabel("Select variables to chart"));
        for (int i = 0; i < histogramStudiedVariables.getNumVars(); i++) {
            varName = histogramStudiedVariables.getVarName(i);
            JCheckBox jcheckbox = new JCheckBox(varName);
            varsToInclude.put(varName, jcheckbox);
            varsP.add(jcheckbox);
        }

        /* List conditional variables and their corresponding values combo boxes */
        condVarsP.add(new JLabel("Select values of conditional variables"), BorderLayout.NORTH);
        JPanel condVarsP2 = new JPanel();
        condVarsP2.setLayout(new GridLayout(histogramStudiedVariables.getNumVars(), 2));
        for (int i = 0; i < histogramStudiedVariables.getNumVars(); i++) {
            varName = histogramStudiedVariables.getVarName(i);

            JCheckBox jcheckbox = new JCheckBox(varName);
            condVars.put(varName, jcheckbox);
            jcheckbox.addActionListener(new CheckBoxListener(varName));

            JComboBox valuesBox = new JComboBox();
            comboBoxes.put(varName, valuesBox);
            valuesBox.setEnabled(false);

            condVarsP2.add(jcheckbox);
            condVarsP2.add(valuesBox);

            ArrayList params = (ArrayList) histogramStudiedVariables.getVarValues(varName);

            for (Iterator j = params.iterator(); j.hasNext(); ) {
                String val = (String) j.next();
                valuesBox.addItem(val);
            }

            valuesBox.addActionListener(new ComboBoxListener(varName, valuesBox));
        }

        condVarsP.add(condVarsP2, BorderLayout.SOUTH);

        return chooser;
    }

    /**
     * Checks if all the required variables are selected before creating the
     * histogram.
     *
     * @return true if so
     */
    protected boolean isReadyToCreateChart() {
        String varName;
        JCheckBox jcheckbox;

        for (int i = 0; i < histogramStudiedVariables.getNumVars(); i++) {
            varName = histogramStudiedVariables.getVarName(i);
            jcheckbox = varsToInclude.get(varName);
            if (jcheckbox.isSelected()) return true;
        }
        return false;
    }

    /**
     * Checks if a particular variable of name varName has its values locked to the
     * given value in an experiment.
     *
     * @param varName
     * @param value
     * @return true if so
     */
    private boolean isCondVarValueValid(String varName, String value) {
        CausalityLabModel model = CausalityLabModel.getModel();
        String expName = model.getExperimentNameForSampleId(sampleId);

        Manipulation manip = model.getExperimentalVariableManipulation(expName, varName);

        //todo:translation
        if ((manip instanceof Locked)
                && !(((Locked) manip).getLockedAtValue().equals(value))) {

            JOptionPane.showMessageDialog(this,
                    "Cannot set this conditional variable (" + varName + ") at this value \nas it is locked at the value \"" + ((Locked) manip).getLockedAtValue() + "\" in the experiment.");
            return false;
        }
        return true;

    }

    /**
     * Private Class action listener for the checkBoxes for conditional
     * variables, to control the enabling and disabling of the combo boxes
     */
    private class CheckBoxListener implements ActionListener {
        private final String varName;

        public CheckBoxListener(String name) {
            varName = name;
        }

        public void actionPerformed(ActionEvent e) {
            boolean enabled = (condVars.get(varName)).isSelected();
            (comboBoxes.get(varName)).setEnabled(enabled);
        }
    }


    /**
     * Private Class action listener for the comboboxes. To restrict the selection
     * of values where that value is not set in an intervened variable for the expt
     */
    private class ComboBoxListener implements ActionListener {
        private final String varName;
        private final JComboBox combo;

        public ComboBoxListener(String varName, JComboBox combo) {
            this.varName = varName;
            this.combo = combo;
        }

        public void actionPerformed(ActionEvent e) {
            isCondVarValueValid(varName, (String) combo.getSelectedItem());
        }
    }
}
