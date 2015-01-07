package edu.cmu.causalityApp.commands;

import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * This class is a moves for clicking the "check answer" button at the top
 * of the navigator.
 *
 * @author mattheweasterday
 */
public class CheckAnswerCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "checkAnswerCommand";

    /**
     * hypotheticalGraph xml attribute name.
     */
    private static final String HYPOTHETICAL_GRAPH = "hypotheticalGraph";

    /**
     * isAnswerCorrect xml attribute name.
     */
    private static final String IS_ANSWER_CORRECT = "isAnswerCorrect";

    /**
     * numberOfGuesses xml attribute name.
     */
    private static final String NUMBER_OF_GUESSES = "numberOfGuesses";

    private final Component labPanel;
    private JDialog dialog;

    private final boolean isCorrect;
    private final String hypGraphName;
    private final int numGuesses;


    /**
     * Constructor
     *
     * @param parent       the main component of the inteface
     * @param isCorrect    true if the students answer was correct
     * @param hypGraphName the name of the hypothesis the student is declearing as the answer
     * @param numGuesses   the number of times the student has tried to check their answer previously
     */
    public CheckAnswerCommand(Component parent, boolean isCorrect, String hypGraphName, int numGuesses) {
        labPanel = parent;
        this.isCorrect = isCorrect;
        this.hypGraphName = hypGraphName;
        this.numGuesses = numGuesses;
    }

    /**
     * Executes the moves.  if the answer is correct, a "you're right" message
     * pops up.  If the answer is incorrect a "try again message" pops up. if
     * no hypothesis has been created, a "make a graph" message pops up.
     */
    public void justDoIt() {
        JOptionPane pane;
        if (hypGraphName != null) {
            Object[] args = {hypGraphName};
            if (isCorrect) {
                pane = new JOptionPane(this.hypGraphName + " " + " matches the correct graph. You're right! ",
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);
                dialog = pane.createDialog(labPanel, "Checking Answer");
            } else {
                pane = new JOptionPane(MessageFormat.format("Nope, try again! {0} does not match the correct graph.", args) +
                        " " + "Number of guesses so far:" + numGuesses,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION);
                dialog = pane.createDialog(labPanel, "Checking Answer");
            }
        } else {
            pane = new JOptionPane(
                    "Hey, you didn't make a graph yet. Please make a graph to check answer.",
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.DEFAULT_OPTION);
            dialog = pane.createDialog(labPanel, "Checking Answer");
        }
        dialog.setVisible(true);
    }

    public void redo() {
        // empty.
    }

    /**
     * "Undoes" the moves by removing the the window
     */
    public void undo() {
        try {
            dialog.dispose();
        } catch (Exception e) {
            System.out.println("In replay mode - no dialog to undo()");
        }
    }

    /**
     * A string representation of the moves for the history window
     *
     * @return String
     */
    public String toString() {
        return "Check answer";
    }

    /**
     * The name of the moves, also used for the xml representation
     *
     * @return name of moves
     */
    public String getCommandName() {
        return MY_NAME;
    }

    /**
     * The attributes in the xml representation
     *
     * @return array of attributes
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[4];

        if (hypGraphName != null) {
            att[0] = new Attribute(HYPOTHETICAL_GRAPH, hypGraphName);
        } else {
            att[0] = new Attribute(HYPOTHETICAL_GRAPH, "");
        }
        String ans = isCorrect ? "yes" : "no";
        att[2] = new Attribute(IS_ANSWER_CORRECT, ans);
        att[3] = new Attribute(NUMBER_OF_GUESSES, (new Integer(numGuesses)).toString());

        return att;
    }

}
