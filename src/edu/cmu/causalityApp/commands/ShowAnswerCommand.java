package edu.cmu.causalityApp.commands;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

import javax.swing.*;

/**
 * Command for when user clicks on show answer button
 *
 * @author mattheweasterday
 */
public class ShowAnswerCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "showAnswerCommand";

    private static final String GUESSES_SO_FAR = "guesseSoFar";
    private static final String ANSWER_HIDDEN = "answerCurrentlyHidden";
    private static final String YES = "yes";
    private static final String NO = "no";

    /**
     * Indicates if the answer is still hidden and locked.
     */
    private final boolean IS_HIDDEN;

    /**
     * Number of guesses the student has made so far.
     */
    private final int NUM_GUESSES;

    /**
     * The parent panel to manipulate.
     */
    private final ShowAnswerPanel PANEL;


    /**
     * Constructor
     *
     * @param labPanel   interface element to manipulate
     * @param isHidden   true if the answer is still locked
     * @param numGuesses number of guesses the student has made so far
     */
    public ShowAnswerCommand(ShowAnswerPanel labPanel, boolean isHidden, int numGuesses) {
        IS_HIDDEN = isHidden;
        NUM_GUESSES = numGuesses;
        PANEL = labPanel;
    }


    /**
     * Runs moves by making revealing the hidden editors, or giving an
     * error message if the student hasn't made enough attempts.
     */
    public void justDoIt() {
        CausalityLabModel model = CausalityLabModel.getModel();
        String lazy = "Don't be lazy. You need to make 3 guesses first before showing the answer." + "\n" + "Number of guesses so far:" + NUM_GUESSES;   //$NON-NLS-3$

        // show answer if user did at least 3 guesses already
        if (IS_HIDDEN) {
            if (model.getNumberOfGuesses() < 3) {
                JOptionPane pane = new JOptionPane(
                        lazy,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);
                JDialog d = pane.createDialog(PANEL.asJComponent(), "Cannot show answer yet");
                // todo: change all deprecated show() to setVisible(boolean)
                d.setVisible(true);
            } else {
                PANEL.showAnswer();
            }
        }
    }

    /**
     * RE-Runs ShowAnswerCommand without the dialog boxes that the user will
     * have to click "ok" to continue replaying.
     * todo: redo() function!!!!
     */
    public void redo() {

        System.out.println("WERTYUIOP");

        CausalityLabModel model = CausalityLabModel.getModel();
        String lazy = "Don't be lazy. You need to make 3 guesses first before showing the answer." + "\n" + "Number of guesses so far:" + NUM_GUESSES;   //$NON-NLS-3$

        // show answer if user did at least 3 guesses already
        if (IS_HIDDEN) {
            if (model.getNumberOfGuesses() < 3) {
                PANEL.setAnswerMessage(lazy);
            } else {
                PANEL.showAnswer();
            }
        }
    }


    /**
     * Since the action pops up a modal dialog, we don't do anything on the
     * rewind so as not to be annoying
     */
    public void undo() {
    }


    /**
     * String representation of the moves for display in the commmand history
     *
     * @return "Show answer"
     */
    public String toString() {
        return "Show answer";
    }


    /**
     * Name of the moves used for the xml representation
     *
     * @return "showAnswerCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * The attributes in the xml representation
     *
     * @return array of render Attributes
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[2];
        att[0] = new Attribute(GUESSES_SO_FAR, (new Integer(NUM_GUESSES)).toString());
        String ans = IS_HIDDEN ? YES : NO;
        att[1] = new Attribute(ANSWER_HIDDEN, ans);

        return att;
    }

}
