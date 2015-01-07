package edu.cmu.causalityApp.EssayEditor;

import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.causalityApp.exercise.Exercise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class describes the window containing the instructions for the exercise
 * when an exercise is first loaded.
 *
 * @author Ming Yang Koh
 */
public class EssayEditor extends AbstractEditor {
    private final Exercise exercise;

    /**
     * Constructor.
     */
    public EssayEditor(Exercise exercise) {
        super(MY_NAME);
        this.exercise = exercise;
        getContentPane().add(createMainPane());
    }

    /**
     * Unique ID name for this window.
     */

    public static final String MY_NAME = "Essay";

    /**
     * @return unique id name for this window
     */
    public String getEditorName() {
        return MY_NAME;
    }


    private JPanel createMainPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Question"));
        b.add(Box.createHorizontalGlue());

        panel.add(b);

        JTextArea essayQuestion = new JTextArea(exercise.getEssayQuestion().replaceAll("0x0a", "\n"));

        essayQuestion.setEditable(false);
        essayQuestion.setLineWrap(true);
        essayQuestion.setWrapStyleWord(true);
        essayQuestion.setRows(3);


        JScrollPane scrollPane =
                new JScrollPane(essayQuestion,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setPreferredSize(new Dimension(
                (int) essayQuestion.getPreferredSize().getWidth(),
                (int) essayQuestion.getPreferredSize().getHeight() + 18));

        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(20));


        // building the essay answer section
        JTextArea textArea = new JTextArea(exercise.getEssayAnswer().replaceAll("0x0a", "\n"));

        textArea.setEditable(true);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.setRows(10);
        textArea.setColumns(30);

        JScrollPane scrollPane2 =
                new JScrollPane(textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane2);

        JPanel doneButtonLayer = new JPanel();
        doneButtonLayer.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 2));

        doneButtonLayer.setLayout(new BorderLayout());
        JButton okButton = new JButton("ok");
        okButton.setToolTipText("Only the most recent submission is taken");
        okButton.addActionListener(new myActionListener(textArea, this));

        doneButtonLayer.add(okButton, BorderLayout.EAST);

        panel.add(doneButtonLayer);
        return panel;
    }

    private class myActionListener implements ActionListener {
        private final JTextArea essayAnswerTextArea;
        private final EssayEditor currentEditor;

        public myActionListener(JTextArea essayAnswerTextArea, EssayEditor currentEditor) {
            this.essayAnswerTextArea = essayAnswerTextArea;
            this.currentEditor = currentEditor;
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("getting here");
            exercise.setAnswer(essayAnswerTextArea.getText().replaceAll("\n", "0x0a"));

            currentEditor.setVisible(false);

        }
    }
}
