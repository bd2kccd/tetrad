package edu.cmu.command;

/**
 * Dummy moves used at beginning of moves history
 *
 * @author mattheweasterday
 */
public class StartCommand extends AbstractCommand {

    /**
     * An xml tag representing this class.
     */
    private static final String MY_NAME = "startCommand";


    /**
     * Constructor.
     */
    public StartCommand() {
    }


    /**
     * Runs the moves by doing nothing.
     */
    public void justDoIt() {
    }


    /**
     * Undoes the moves by doing nothing.
     */
    public void undo() {
    }


    /**
     * String representation of moves for display in moves history.
     *
     * @return "start"
     */
    public String toString() {
        return "Start";
    }


    /**
     * Name of moves used in xml representation.
     *
     * @return "startCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }
}
