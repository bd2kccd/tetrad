package edu.cmu.causalityApp.exerciseBuilder;

import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.causalityApp.exercise.WindowInclusionStatus;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the controller. This is separated out from the view
 * for better unit-testing.
 *
 * @author axshahab
 */
class NavigatorActionListener implements ActionListener {

    private NavigatorIconPanel navigatorIconPanel;

    private final Exercise exercise;
    private int selectedCheckboxes;

    public NavigatorActionListener(Exercise exercise) {
        this.exercise = exercise;
    }

    /**
     * @param navigatorIconPanel the navigatorIconPanel to set
     */
    public void setNavigatorIconPanel(NavigatorIconPanel navigatorIconPanel) {
        this.navigatorIconPanel = navigatorIconPanel;
        selectedCheckboxes = 0;
        //set up the model
        if (navigatorIconPanel.CORRECT_GRAPH_BOX.isSelected()) {
            selectedCheckboxes++;
        }
        if (navigatorIconPanel.EXPERIMENTAL_SETUP_BOX.isSelected()) {
            selectedCheckboxes++;
        }
        if (navigatorIconPanel.HYPOTHETICAL_GRAPH_BOX.isSelected()) {
            selectedCheckboxes++;
        }
    }

    /**
     * Defines the action performed when the navigator button checkboxes are
     * checked.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(NavigatorIconPanel.CORRECT_GRAPH)) {
            if (handleGraphSelection(navigatorIconPanel.CORRECT_GRAPH_BOX)) {

                navigatorIconPanel.correctGraphIcon.toggle();
                exercise.setCorrectGraphInclusion(getStatus(navigatorIconPanel.correctGraphIcon));
            }

        } else if (e.getActionCommand().equals(NavigatorIconPanel.EXPERIMENTAL_SETUP)) {
            if (handleGraphSelection(navigatorIconPanel.EXPERIMENTAL_SETUP_BOX)) {
                navigatorIconPanel.experimentalSetupIcon.toggle();
                exercise.setExperimentalSetupInclusion(getStatus(navigatorIconPanel.experimentalSetupIcon));
            }
        } else if (e.getActionCommand().equals(NavigatorIconPanel.HYPOTHETICAL_GRAPH)) {
            if (handleGraphSelection(navigatorIconPanel.HYPOTHETICAL_GRAPH_BOX)) {
                navigatorIconPanel.hypotheticalGraphIcon.toggle();
                exercise.setHypotheticalGraphInclusion(getStatus(navigatorIconPanel.hypotheticalGraphIcon));
            }
        } else if (e.getActionCommand().equals(NavigatorIconPanel.CORRECT_MANIPULATED_GRAPH)) {
            navigatorIconPanel.correctManipulatedGraphIcon.toggle();
            exercise.setCorrectManipulatedGraphInclusion(getStatus(navigatorIconPanel.correctManipulatedGraphIcon));

        } else if (e.getActionCommand().equals(NavigatorIconPanel.HYPOTHETICAL_MANIPULATED_GRAPH)) {
            navigatorIconPanel.hypotheticalManipulatedGraphIcon.toggle();
            exercise.setHypotheticalManipulatedGraphInclusion(getStatus(navigatorIconPanel.hypotheticalManipulatedGraphIcon));

        } else if (e.getActionCommand().equals(NavigatorIconPanel.POPULATION)) {
            navigatorIconPanel.populationIcon.toggle();
            exercise.setPopulationInclusion(getStatus(navigatorIconPanel.populationIcon));

        } else if (e.getActionCommand().equals(NavigatorIconPanel.SAMPLE)) {
            navigatorIconPanel.sampleIcon.toggle();
            exercise.setSampleInclusion(getStatus(navigatorIconPanel.sampleIcon));

        } else if (e.getActionCommand().equals(NavigatorIconPanel.INDEPENDENCIES)) {
            navigatorIconPanel.independenciesIcon.toggle();
            exercise.setIndependenciesInclusion(getStatus(navigatorIconPanel.independenciesIcon));

        } else if (e.getActionCommand().equals(NavigatorIconPanel.INCLUDE_STUDENT_GUESSES)) {
            exercise.setStudentGuessInclusion(navigatorIconPanel.STUDENT_GUESS_BOX.isSelected());
        }
    }

    private boolean handleGraphSelection(JCheckBox checkBox) {
        selectedCheckboxes = (checkBox.isSelected()) ? selectedCheckboxes + 1 : selectedCheckboxes - 1;
        if (selectedCheckboxes <= 0) {
            selectedCheckboxes++;
            checkBox.setSelected(true);
            JOptionPane.showMessageDialog(navigatorIconPanel,
                    "You must select at least one between the true graph, experimental graph, and hypothetical graph.");
            return false;
        }
        return true;
    }

    private WindowInclusionStatus getStatus(ToggleIcon icon) {
        return icon.isShown() ? WindowInclusionStatus.NOT_HIDABLE : WindowInclusionStatus.NOT_INCLUDED;
    }
}
