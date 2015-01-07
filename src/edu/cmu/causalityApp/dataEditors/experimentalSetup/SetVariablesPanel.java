package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.Randomized;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * To change this template use File | Settings | File Templates.
 *
 * @author mykoh
 */
class SetVariablesPanel extends JPanel {

    private final ExperimentalSetup studiedVariables;
    private BayesPm correctGraphPm;
    private final ExperimentalSetupWizardVariableView varViewP2;
    private final HashMap<String, JComboBox> comboBoxes;

    public SetVariablesPanel(ExperimentalSetup studiedVariables, CausalityLabModel model) {
        super();
        this.studiedVariables = studiedVariables;

        if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
//            this.correctGraphPm = model.getCorrectGraphPmCopy();
//            this.correctGraphPm = model.getCorrectBayesIm().getBayesPm();

            // todo should a pm be set here? Otherwise it's null and none of the code below kicks in...
            // todo but what does this code do? jdramsey 6/14/2013
        } else {
            this.correctGraphPm = null;
        }


        varViewP2 = new ExperimentalSetupWizardVariableView(studiedVariables);
        comboBoxes = new HashMap<String, JComboBox>();
        String[] variables = studiedVariables.getVariableNames();

        setLayout(new BorderLayout());
        add(varViewP2, BorderLayout.WEST);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(variables.length + 1, 4, 30, 20));
        titlePanel.setPreferredSize(new Dimension(300, 50 * (variables.length + 1)));

        // in the meanwhile, perhaps just put a string inside the label that doesn't have the language options
        titlePanel.add(new JLabel("Mean"));
        titlePanel.add(new JLabel("Standard Deviation"));
        // temporary measure above...

        if (correctGraphPm != null) {
            titlePanel.add(new JLabel("Lock"));
            titlePanel.add(new JLabel("Lock Value"));
        }
        for (String varName : variables) {
            ButtonGroup group = new ButtonGroup();

            DoubleTextField meanField = new DoubleTextField(0.0, 15, new DecimalFormat("0.0###"));
            DoubleTextField sdField = new DoubleTextField(1.0, 15, new DecimalFormat("0.0###"));

            sdField.setFilter(new DoubleTextField.Filter() {
                public double filter(double value, double oldValue) {
                    if (value >= 0.0) {
                        /*setBinaryCutoff(value);*/
                        return value;
                    }

                    return oldValue;
                }
            });

            // checks if the respective x (eg x1, x2 rows) are randomized
            // if randomized, let them enter a mean and s.d, else,
            // do not allow input from the user
            if (!(studiedVariables.getVariable(varName).getManipulation() instanceof Randomized)) {
                meanField.setVisible(false);
                sdField.setVisible(false);
            } else {
                meanField.setVisible(true);
                sdField.setVisible(true);
            }

            // **** end of changes ***

            titlePanel.add(meanField);
            titlePanel.add(sdField);

            meanField.addFocusListener(new DoubleTextFieldMeanListener(varName, meanField));
            sdField.addFocusListener(new DoubleTextFieldStandardDeviationListener(varName, sdField));

            meanField.setPreferredSize(new Dimension(15, 15));

            // Disables randomizing if variable cannot be intervened upon
            if (!model.getVariableIntervenable(varName)) {
                sdField.setEnabled(false);
                sdField.setToolTipText("For either practical or ethical reasons, you can't intervene on this variable.");
            }

            //======locking
            if (correctGraphPm != null) {
                JRadioButton lock = new JRadioButton();
                JComboBox lockVal = new JComboBox();

                lockVal.setEnabled(false);
                comboBoxes.put(varName, lockVal);

                List<String> params = new ArrayList<String>();
                Node node = correctGraphPm.getDag().getNode(varName);

                for (int j = 0; j < correctGraphPm.getNumCategories(node); j++) {
                    params.add(correctGraphPm.getCategory(node, j));
                }

                for (String val : params) {
                    lockVal.addItem(val);
                    lockVal.addItemListener(new ComboListener(varName));
                }

                if (!studiedVariables.isVariableStudied(varName)) {
                    lock.setEnabled(false);
                }

                group.add(lock);
                titlePanel.add(lock);
                titlePanel.add(lockVal);
                lock.addActionListener(new RadioListenerP2(varName, "Locked"));

                // Disables locking if variable cannot be intervened upon
                if (!model.getVariableIntervenable(varName)) {
                    lock.setEnabled(false);
                    lock.setToolTipText("For either practical or ethical reasons, you can't intervene on this variable.");
                }
            }
        }

        add(titlePanel, BorderLayout.CENTER);
        add(Box.createHorizontalStrut(7), BorderLayout.EAST);
    }


    private class DoubleTextFieldMeanListener implements FocusListener {
        private final String name;
        private final DoubleTextField mean;

        public DoubleTextFieldMeanListener(String name, DoubleTextField mean) {
            this.name = name;
            this.mean = mean;
        }

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            studiedVariables.getVariable(name).setMean(this.mean.getValue());
        }
    }

    private class DoubleTextFieldStandardDeviationListener implements FocusListener {
        private final String name;
        private final DoubleTextField stdDev;

        public DoubleTextFieldStandardDeviationListener(String name, DoubleTextField stdDev) {
            this.name = name;
            this.stdDev = stdDev;
        }

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            studiedVariables.getVariable(name).setStandardDeviation(this.stdDev.getValue());
        }
    }


    //investigate later...

    private class RadioListenerP2 implements ActionListener {
        private final String name;
        private final String manip;

        public RadioListenerP2(String name, String manip) {
            this.name = name;
            this.manip = manip;
        }

        public void actionPerformed(ActionEvent e) {
            if (manip.equals("None")) {
                studiedVariables.getVariable(name).setUnmanipulated();
                if (correctGraphPm != null) {
                    (comboBoxes.get(name)).setEnabled(false);
                }
            } else if (manip.equals("Randomized")) {
                studiedVariables.getVariable(name).setRandomized();    // HERE
                if (correctGraphPm != null) {
                    (comboBoxes.get(name)).setEnabled(false);
                }
            } else if (manip.equals("Locked")) {
                (comboBoxes.get(name)).setEnabled(true);
                studiedVariables.getVariable(name).setLocked(
                        (comboBoxes.get(name)).getSelectedItem().toString());
            }
            varViewP2.refreshViews();
        }
    }


    private class ComboListener implements ItemListener {
        private final String name;

        public ComboListener(String name) {
            this.name = name;
        }

        public void itemStateChanged(ItemEvent e) {
            studiedVariables.getVariable(name).setLocked(
                    (comboBoxes.get(name)).getSelectedItem().toString());
            varViewP2.refreshViews();
        }
    }

}
