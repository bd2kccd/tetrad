package edu.cmu.causalityApp.FeedbackEditor;

import edu.cmu.causalityApp.CausalityLab;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.exercise.Exercise;
import edu.cmu.oli.superactivity.model.ActivityMode;
import edu.cmu.tetradapp.util.DoubleTextField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * This class describes the window containing the instructions for the exercise
 * when an exercise is first loaded.
 *
 * @author ming yang koh, luis fernando bermudez
 */
public class FeedbackEditor extends AbstractEditor {

    private final Exercise exercise;
    private JTextArea textArea;

    private DoubleTextField scoreField;
    public static final String MY_NAME = "Feedback";

    /**
     * @return unique id name for this window
     */
    public String getEditorName() {
        return MY_NAME;
    }

    public FeedbackEditor(Exercise exercise) {
        super(MY_NAME);
        this.exercise = exercise;
        getContentPane().add(createMainPane());
    }

    /**
     * Unique ID name for this window.
     */


    private JPanel createMainPane() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Instructor feedback"));
        b.add(Box.createHorizontalGlue());

        panel.add(b);

        panel.add(Box.createVerticalStrut(20));

        //JTextArea textArea = new JTextArea(exercise.get Answer());
        textArea = new JTextArea(exercise.getInstructorFeedback());

        // by default no-one can edit this textfield other than the grader
        textArea.setEditable(false);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.setRows(10);
        textArea.setColumns(30);

        JScrollPane scrollPane2 =
                new JScrollPane(textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane2);


        // setting up the score Label and the score textfield
        JLabel scoreLabel = new JLabel("Grade:");
        try {
            scoreField = new DoubleTextField(Double.valueOf((exercise.getGrade()).trim()), 5, new DecimalFormat("##.##"));
        } catch (Exception e) {
//            System.out.println("error in parsing student's grade..");
            scoreField = new DoubleTextField(0.0, 5, new DecimalFormat("##.##"));
        }


        scoreField.getDocument().addDocumentListener(new scoreListener(scoreField, exercise));

        // filter set-up to prevent percent scores from going below 0 or above 100
        scoreField.setFilter(new DoubleTextField.Filter() {
            public double filter(double value, double oldValue) {
                if (value < 0.0 || value > 100.0) {
                    JOptionPane.showMessageDialog(null, "Please enter a value between 0 and 100", "Input error", JOptionPane.ERROR_MESSAGE);

                    return oldValue;
                }

                return value;
            }
        });

        // by default set as disabled first.
        scoreField.setEnabled(false);

        JPanel scoreSection = new JPanel();
        scoreSection.setLayout(new BoxLayout(scoreSection, BoxLayout.X_AXIS));
        scoreSection.add(scoreLabel);
        scoreSection.add(Box.createHorizontalStrut(5));
        scoreSection.add(scoreField);
        scoreSection.add(new JLabel("%"));


        // setting up the bottom layer
        JPanel doneButtonLayer = new JPanel();
        doneButtonLayer.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 2));
        doneButtonLayer.setLayout(new BorderLayout());

        JButton okButton = new JButton("ok");
        okButton.addActionListener(new myActionListener(textArea, this, exercise));

        doneButtonLayer.add(okButton, BorderLayout.EAST);
        doneButtonLayer.add(scoreSection, BorderLayout.WEST);

        panel.add(doneButtonLayer);

        return panel;
    }

    // the scoreListener class implements DocumentListener (not ActionListener! or TextListener!
    // TextListener is for Textfield not JTextField. something about difference between .awt package
    // and swing package.  And documentListener seems more versatile also....
    // simply updates the score variable whenever it detects a change
    private class scoreListener implements DocumentListener {
        private final JTextField currentScoreField;
        private final Exercise currentExercise;

        public scoreListener(JTextField currentScoreField, Exercise currentExercise) {
            this.currentScoreField = currentScoreField;
            this.currentExercise = currentExercise;
        }

        public void insertUpdate(DocumentEvent e) {
            currentExercise.setScore(currentScoreField.getText());
        }

        public void removeUpdate(DocumentEvent e) {
            currentExercise.setScore(currentScoreField.getText());
        }

        public void changedUpdate(DocumentEvent e) {
            currentExercise.setScore(currentScoreField.getText());
        }
    }


    private class myActionListener implements ActionListener {
        private final JTextArea feedbackTextArea;
        private final FeedbackEditor currentEditor;
        private final Exercise currentExercise;

//        public myActionListener(JTextArea feedbackTextArea, CausalityLabModel model, FeedbackEditor currentEditor, Exercise currentExercise, CausalityLab currentCausalityLab){
//            this.feedbackTextArea = feedbackTextArea;
//            this.model = model;
//            this.currentEditor = currentEditor;
//            this.currentExercise = currentExercise;
//            this.currentCausalityLab = currentCausalityLab;
//        }

        public myActionListener(JTextArea feedbackTextArea, FeedbackEditor currentEditor, Exercise currentExercise) {
            this.feedbackTextArea = feedbackTextArea;
            this.currentEditor = currentEditor;
            this.currentExercise = currentExercise;
        }

        public void actionPerformed(ActionEvent e) {
            currentExercise.setInstructorFeedback(feedbackTextArea.getText());
//
//
//            // calling the CausalityLab.saveInstructorFeedback() method to save the feedback
//            // only will need to call saveInstructorFeedback if its an applet
//            // otherwise, it'll crash if its an application
//            if(currentCausalityLab.getMode() != null && currentCausalityLab.getMode().equals(OLIAppletParams.gradeExerciseMode)) {
//                currentCausalityLab.saveInstructorFeedback();
//            }
            if (CausalityLab.getMode() != null && CausalityLab.getMode().equals(ActivityMode.REVIEW)) {
                // on top of the instructor feedback, saves the score as well.
                // need to pass in the score value as an argument?!?!?

                CausalityLab.saveInstructorFeedback();
            }
            currentEditor.setVisible(false);
        }
    }


    /**
     * mutator function to enable the scorefield for grader to enter grades
     */
    public void enableScoreField() {
        scoreField.setEnabled(true);
    }


    /**
     * setTextFieldEnabled() method is used to enable the single textfield inside this panel.
     * this function is being called from the CausalityLabPanel which is called by
     * CausalityLab when the check of user mode is determined.
     * by default this function is not called. if user is a grader, this function will be called
     */
    public void setTextFieldEnabled(boolean isEnabled) {
        textArea.setEditable(isEnabled);
    }
}
