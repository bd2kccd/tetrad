package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This panel allows user to specify if a given variable in the experiment can
 * be intervened upon.
 *
 * @author jangace
 */
public class VariableIntervenablePanel extends JPanel {
    private final Exercise exercise;

    /**
     * Constructor. Creates the panel.
     */
    public VariableIntervenablePanel(Exercise exercise) {
        super();
        this.exercise = exercise;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //todo: translation
        JTextArea instructions = new JTextArea("Please specify if you want to limit the resources that the student has to work with. This resources will be the total amount of money the student has in an exercise to collect sample data for experiments.");   //$NON-NLS-3$
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        //instructions.setColumns(30);

        JPanel intervene_labels = new JPanel();
        intervene_labels.setLayout(new BoxLayout(intervene_labels, BoxLayout.PAGE_AXIS));
        intervene_labels.add(new JLabel("Student can"));
        intervene_labels.add(new JLabel("intervene upon"));

        JPanel options = new JPanel();
        options.setLayout(new GridLayout(0, 2));
        options.add(new JLabel(" "));
        options.add(intervene_labels);

        String[] varNames = exercise.getMeasuredVariableNames();
        JCheckBox[] checkboxes = createCheckboxes(varNames);
        for (int i = 0; i < varNames.length; i++) {
            boolean b = exercise.isVariableIntervenable(varNames[i]);
            checkboxes[i].setSelected(b);

            options.add(new JLabel(varNames[i]));
            options.add(checkboxes[i]);
        }

        add(instructions);
        add(Box.createVerticalStrut(10));
        add(options);
        add(Box.createVerticalStrut(10));

        setPreferredSize(new Dimension(400, 135 + varNames.length * 30));
    }

    /**
     * Given a string array of variable names in the exercise, create a set of
     * corresponding checkboxes to represent their intervenable status.
     *
     * @param varNames string array of variable names
     * @return an array of JCheckBoxes with index corresponding to the string
     *         array of variable names
     */
    private JCheckBox[] createCheckboxes(String[] varNames) {
        JCheckBox[] checkboxes = new JCheckBox[varNames.length];

        for (int i = 0; i < varNames.length; i++) {
            checkboxes[i] = new JCheckBox();
            checkboxes[i].setSelected(exercise.isVariableIntervenable(varNames[i]));
            checkboxes[i].addActionListener(new CheckBoxListener(varNames[i], checkboxes[i]));
        }

        return checkboxes;
    }


    /**
     * ***********************************************************************
     * Private Class action listener for the checkBoxes for conditional
     * variables, to control the enabling and disabling of the combo boxes
     */
    private class CheckBoxListener implements ActionListener {
        private final String varName;
        private final JCheckBox checkbox;

        public CheckBoxListener(String name, JCheckBox checkbox) {
            varName = name;
            this.checkbox = checkbox;
        }

        public void actionPerformed(ActionEvent e) {
            exercise.setVariableIntervenable(varName, checkbox.isSelected());
        }
    }
}
