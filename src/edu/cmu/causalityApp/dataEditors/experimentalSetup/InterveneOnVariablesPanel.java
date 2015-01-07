package edu.cmu.causalityApp.dataEditors.experimentalSetup;


import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causality.experimentalSetup.ExperimentalSetup;
import edu.cmu.causality.experimentalSetup.manipulation.Randomized;
import edu.cmu.tetrad.bayes.BayesPm;
import edu.cmu.tetrad.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * To change this template use File | Settings | File Templates.
 *
 * @author mattheweasterday
 */
class InterveneOnVariablesPanel extends JPanel {
    private final ExperimentalSetup studiedVariables;
    private final BayesPm correctGraphPm;
    private final ExperimentalSetupWizardVariableView varViewP2;
    private final HashMap<String, JComboBox> comboBoxes;
    private final JButton nextButton;
    private final JButton finishButton;

    public InterveneOnVariablesPanel(ExperimentalSetup studiedVariables, CausalityLabModel model, JButton next, JButton finish) {
        super();
        this.studiedVariables = studiedVariables;
        nextButton = next;
        finishButton = finish;

        if (model.getModelType().equals(CausalityLabModel.ModelType.BAYES)) {
            this.correctGraphPm = model.getCorrectGraphBayesPmCopy();
        } else {
            this.correctGraphPm = null;
        }


        varViewP2 = new ExperimentalSetupWizardVariableView(studiedVariables);
        comboBoxes = new HashMap<String, JComboBox>();
        String[] variables = studiedVariables.getVariableNames();

        setLayout(new BorderLayout());
        add(varViewP2, BorderLayout.WEST);

        JPanel p2Radio = new JPanel();
        p2Radio.setLayout(new GridLayout(variables.length + 1, 4));
        p2Radio.setPreferredSize(new Dimension(300, 50 * (variables.length + 1)));
        p2Radio.add(new JLabel("<html>Passively<br>Observe</html>"));
        p2Radio.add(new JLabel("Randomize"));
        if (correctGraphPm != null) {
            p2Radio.add(new JLabel("Lock"));
            p2Radio.add(new JLabel("Lock Value"));
        }
        for (String varName : variables) {
            ButtonGroup group = new ButtonGroup();
            JRadioButton observe = new JRadioButton();
            JRadioButton randomize = new JRadioButton();

            group.add(observe);
            group.add(randomize);

            p2Radio.add(observe);
            p2Radio.add(randomize);

            observe.addActionListener(new RadioListenerP2(varName, "None"));
            randomize.addActionListener(new RadioListenerP2(varName, "Randomized"));

            if (studiedVariables.isVariableStudied(varName)) {
                observe.setSelected(true);
            } else {
                observe.setEnabled(false);
                randomize.setEnabled(false);
            }

            // Disables randomizing if variable cannot be intervened upon
            if (!model.getVariableIntervenable(varName)) {
                randomize.setEnabled(false);
                randomize.setToolTipText("For either practical or ethical reasons, you can't intervene on this variable.");
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
                p2Radio.add(lock);
                p2Radio.add(lockVal);
                lock.addActionListener(new RadioListenerP2(varName, "Locked"));

                // Disables locking if variable cannot be intervened upon
                if (!model.getVariableIntervenable(varName)) {
                    lock.setEnabled(false);
                    lock.setToolTipText("For either practical or ethical reasons, you can't intervene on this variable.");
                }
            }
        }
        add(p2Radio, BorderLayout.EAST);
    }

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
                } else {
                    String nameArray[] = studiedVariables.getVariableNames();
                    boolean nextFlag = false;
                    for (String aNameArray : nameArray) {
                        if (studiedVariables.getVariable(aNameArray).getManipulation() instanceof Randomized) {
                            nextFlag = true;
                            break;
                        }
                    }

                    if (!nextFlag) {
                        finishButton.setEnabled(true);
                        nextButton.setEnabled(false);
                    }

                }

            } else if (manip.equals("Randomized")) {
                studiedVariables.getVariable(name).setRandomized();


                if (correctGraphPm != null) {
                    (comboBoxes.get(name)).setEnabled(false);
                } else {
                    finishButton.setEnabled(false);
                    nextButton.setEnabled(true);
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
