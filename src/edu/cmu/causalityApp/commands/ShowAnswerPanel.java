package edu.cmu.causalityApp.commands;

import javax.swing.*;

/**
 * To change this template use File | Settings | File Templates.
 *
 * @author mykoh
 */
public interface ShowAnswerPanel {

    /**
     * Show the answer.
     */
    public void showAnswer();

    /**
     * Tells the answer panel what feedback message to display.
     *
     * @param message the feedback.
     */
    public void setAnswerMessage(String message);

    /**
     * Casts the answer panel to a JComponent.
     *
     * @return the answer panel.
     */
    public JComponent asJComponent();
}
