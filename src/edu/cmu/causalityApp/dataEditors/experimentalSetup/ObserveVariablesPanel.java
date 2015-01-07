package edu.cmu.causalityApp.dataEditors.experimentalSetup;

import edu.cmu.causality.experimentalSetup.ExperimentalSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * To change this template use File | Settings | File Templates.
 *
 * @author mattheweasterday
 */
class ObserveVariablesPanel extends JPanel {

    private final ExperimentalSetupWizardVariableView varViewP1;
    private final ExperimentalSetup studiedVariables;

    public ObserveVariablesPanel(ExperimentalSetup studiedVariables) {
        super();
        this.studiedVariables = studiedVariables;

        String[] variables = studiedVariables.getVariableNames();

        varViewP1 = new ExperimentalSetupWizardVariableView(studiedVariables);
        setLayout(new BorderLayout());
        add(varViewP1, BorderLayout.WEST);

        JPanel p1Radio = new JPanel();
        p1Radio.setLayout(new GridLayout(variables.length + 1, 2));
        p1Radio.setPreferredSize(new Dimension(150, 50 * (variables.length + 1)));
        p1Radio.add(new JLabel("Measure"));
        p1Radio.add(new JLabel("Ignore"));
        for (int i = 0; i < variables.length; i++) {
            String varName = variables[i];
            ButtonGroup group = new ButtonGroup();
            JRadioButton observe = new JRadioButton();
            observe.setSelected(true);
            JRadioButton ignore = new JRadioButton();
            group.add(observe);
            group.add(ignore);

            p1Radio.add(observe);
            p1Radio.add(ignore);

            observe.addActionListener(new RadioListenerP1(varName, true));
            ignore.addActionListener(new RadioListenerP1(varName, false));
        }

        add(p1Radio, BorderLayout.EAST);

    }


    private class RadioListenerP1 implements ActionListener {
        private final String name;
        private final boolean studied;

        public RadioListenerP1(String name, boolean studied) {
            this.name = name;
            this.studied = studied;
        }

        public void actionPerformed(ActionEvent e) {
            studiedVariables.getVariable(name).setStudied(studied);
            if (!studied) {
                studiedVariables.getVariable(name).setUnmanipulated();
            }
            varViewP1.refreshViews();
            //varViewP2.refreshViews();
        }
    }
}
