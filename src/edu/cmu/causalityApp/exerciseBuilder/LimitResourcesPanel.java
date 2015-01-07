package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This panel allows user to limit the resources for a student in an exercise,
 * and to specify the expected expenditure of each experiment.
 *
 * @author jangace Date: Oct 22, 2005 Time: 10:24:55 PM
 */
class LimitResourcesPanel extends JPanel implements ActionListener {
    final private static String LIMIT_RESOURCES = "Limit resources";
    private final Exercise exercise;
    private final JCheckBox checkbox;
    private final JTextField txt_total_money;
    private final JTextField txt_cost_per_obs;
    private final JTextField txt_cost_per_int;

    /**
     * Constructor. Creates the panel.
     */
    public LimitResourcesPanel(Exercise exercise) {
        super();
        this.exercise = exercise;

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // todo: translation
        JTextArea instructions = new JTextArea(
                "Please specify if you want to limit the resources that the student has to work with. You can indicate the total amount of money the student has in an exercise to collect sample data for experiments."); //$NON-NLS-3$
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setEditable(false);
        instructions.setBackground(getBackground());

        JLabel label_total_money = new JLabel("Total Money:");
        JLabel label_cost_per_obs = new JLabel(
                "Cost per subject (Observation):");
        JLabel label_cost_per_int = new JLabel(
                "Cost per subject (Intervention):");
        JPanel options = new JPanel();

        checkbox = new JCheckBox(LIMIT_RESOURCES);
        checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkbox.setActionCommand(LIMIT_RESOURCES);
        checkbox.addActionListener(this);

        txt_total_money = new JTextField(9);
        txt_cost_per_obs = new JTextField(9);
        txt_cost_per_int = new JTextField(9);

        options.setLayout(new GridLayout(3, 2));
        options.add(label_total_money);
        options.add(getTextFieldWithDollarSign(txt_total_money));
        options.add(label_cost_per_obs);
        options.add(getTextFieldWithDollarSign(txt_cost_per_obs));
        options.add(label_cost_per_int);
        options.add(getTextFieldWithDollarSign(txt_cost_per_int));

        add(instructions);
        add(Box.createVerticalStrut(10));
        add(checkbox);
        add(options);
        add(Box.createVerticalStrut(10));

        populateFields();

        setPreferredSize(new Dimension(400, 190));
    }

    private JPanel getTextFieldWithDollarSign(JTextField txtfield) {
        JPanel panel = new JPanel();

        panel.add(new JLabel("$"));
        panel.add(txtfield);

        return panel;
    }

    /**
     * Controls the action of the 'Limit Resources' checkbox.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(LIMIT_RESOURCES)) {
            toggleTextFields();
            applyChanges();
        }
    }

    private void toggleTextFields() {
        if (checkbox.isSelected()) {
            txt_total_money.setEnabled(true);
            txt_cost_per_obs.setEnabled(true);
            txt_cost_per_int.setEnabled(true);
            txt_total_money.setBackground(Color.white);
            txt_cost_per_obs.setBackground(Color.white);
            txt_cost_per_int.setBackground(Color.white);
        } else {
            txt_total_money.setEnabled(false);
            txt_cost_per_obs.setEnabled(false);
            txt_cost_per_int.setEnabled(false);
            txt_total_money.setBackground(Color.LIGHT_GRAY);
            txt_cost_per_obs.setBackground(Color.LIGHT_GRAY);
            txt_cost_per_int.setBackground(Color.LIGHT_GRAY);
        }
    }

    private void populateFields() {
        Integer total = exercise.getResourceTotal();
        Integer obsCost = exercise.getResourcePerObs();
        Integer intCost = exercise.getResourcePerInt();

        if (total == null) {
            total = Exercise.DEFAULT_RESOURCE_TOTAL;
        }
        if (obsCost == null) {
            obsCost = Exercise.DEFAULT_RESOURCE_OBS;
        }
        if (intCost == null) {
            intCost = Exercise.DEFAULT_RESOURCE_INT;
        }

        checkbox.setSelected(exercise.isLimitResource());

        txt_total_money.setText(total.toString());
        txt_cost_per_obs.setText(obsCost.toString());
        txt_cost_per_int.setText(intCost.toString());

        toggleTextFields();
    }

    /**
     * Checks that all the textfields contain valid numbers
     *
     * @return true if so, or if the checkbox is not checked
     */
    public boolean validateFields() {

        // todo: textfield number validation
        if (checkbox.isEnabled()) {
            return true;
        } else {
            return true;
        }
    }

    /**
     * Apply the values in this form into the exercise. This method is done
     * after the fields are validated.
     */
    public void applyChanges() {
        if (!checkbox.isSelected()) {
            exercise.setResourceTotal(null);
            exercise.setResourcePerObs(null);
            exercise.setResourcePerInt(null);
        } else {
            exercise.setResourceTotal(new Integer(txt_total_money.getText()));
            exercise.setResourcePerObs(new Integer(txt_cost_per_obs.getText()));
            exercise.setResourcePerInt(new Integer(txt_cost_per_int.getText()));
        }

    }

}
