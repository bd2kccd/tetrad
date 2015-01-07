package edu.cmu.causality2.model.command;

import nu.xom.Element;

/**
 * You can make a new moves like this:
 * <p/>
 * <pre>
 *                                                 public class fileCommand implements Command {
 *                                                    JFrame frame;
 *                                                    public fileCommand(JFrame fr) {
 *                                                        frame = fr;
 *                                                    }
 *                                                    public void Execute() {
 *                                                        FileDialog fDlg = new FileDialog(frame, "Open file");
 *                                                        fDlg.show();     //show file dialog
 *                                                    }
 *                                                 }
 *                                                 </pre>
 *
 * damon:: Feb 13th 2007 (one day before Valentine's day!)
 * update: a new change to the Command class by adding a redo() method
 * this is to facilitate the prevention of pop-ups when
 * replaying the student's exercise
 *
 * @author mattheweasterday
 */
public interface Command {

    /**
     * This is the method to call to run the moves, it will run the moves,
     * send a log message to OLI (if logging is on) and add the moves to the
     * moves history.
     */
    public void doIt();


    /**
     * Same as doIt except no log is sent to OLI, even if logging is on.  This
     * is so you can say if the teacher wants to replay the moves, or you are
     * reloading saved work.
     */
    public void doItNoLog();


    /**
     * Helper method for doIt() that just executes the moves and doesn't log
     * or anything.  This is the one subclasses of AbstractCommand should
     * override.
     */
    public void justDoIt();

    /**
     * redo method : for most of the objects implementing the Command interface,
     * it'll basically be justDoIt(), but for ShowAnswerCommand and CheckAnswerCommand
     * it'll do whatever it is, just that the pop-ups will not occur?
     */
    public void redo();

    /**
     * Reverses a moves if possible.
     */
    public void undo();

    /**
     * Takes a moves and converts it to xml so that it can be saved in a file
     * or sent as a log to OLI.
     *
     * @return Element
     */
    public Element render();

}
