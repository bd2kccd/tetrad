package edu.cmu.causalityApp.exerciseBuilder;


import edu.cmu.causalityApp.exercise.Exercise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class describes the panel for users to enter instructions into the exercise
 *
 * @author mattheweasterday
 */
public class ExerciseInfoPanel extends JPanel implements KeyListener {

    private final JTextArea promptArea;
    private final Exercise exercise;

    /**
     * Constructor. Creates the panel.
     */
    public ExerciseInfoPanel(Exercise exercise) {
        super();

        this.exercise = exercise;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel promptLabel1 = new JLabel("Please specify the instructions in this exercise.");
        JLabel promptLabel2 = new JLabel("This is what the user will see when they click on the instructions button.");
        JLabel promptLabel3 = new JLabel("Instructions:");
        Font regularFont = promptLabel1.getFont().deriveFont(Font.PLAIN, 12.0f);
        Font boldFont = promptLabel3.getFont().deriveFont(Font.BOLD, 12.0f);

        promptLabel1.setFont(regularFont);
        promptLabel2.setFont(regularFont);
        promptLabel3.setFont(boldFont);

        promptArea = new JTextArea(10, 30);         // was 5, 30
        if (exercise.getPrompt() != null) {
            promptArea.setText(exercise.getPrompt().replaceAll("0x0a", "\n"));
        }

        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setMinimumSize(promptArea.getPreferredSize());
        promptArea.setMaximumSize(promptArea.getPreferredSize());


        promptArea.addKeyListener(this);
        JScrollPane scrollPane =
                new JScrollPane(promptArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);


        add(promptLabel1);
        add(promptLabel2);
        add(Box.createVerticalStrut(10));
        add(promptLabel3);
        add(scrollPane);

    }

    /**
     * Method for KeyListener. Not used by this panel.
     */
    public void keyTyped(KeyEvent ke) {
    }

    /**
     * Method for KeyListener. Not used by this panel.
     */
    public void keyPressed(KeyEvent ke) {
    }

    /**
     * This action is needed to convert the linefeeds in the instructions to
     * readable and storable representation.
     */
    public void keyReleased(KeyEvent ke) {
        // convert all linefeeds to "0x0a" chars
        exercise.setPrompt(promptArea.getText().replaceAll("\n", "0x0a"));
    }


}
