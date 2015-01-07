package edu.cmu.causalityApp.dataEditors;

import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

/**
 * Command for closing windows in the interface.  Every window can be closed
 * with this moves by using the name of the editor as a parameter.
 *
 * @author mattheweasterday
 */
public class CloseWindowCommand extends AbstractCommand {

    /**
     * unique string id of this moves
     */
    private static final String MY_NAME = "closeWindowCommand";
    private static final String EDITOR = "editor";

    private final AbstractEditor editor;


    /**
     * Constructor
     *
     * @param editor to close
     */
    public CloseWindowCommand(AbstractEditor editor) {
        this.editor = editor;
    }

    /**
     * Executes the moves.  Just sets the window to not visible.
     */
    public void justDoIt() {
        if (editor.isVisible()) {
            editor.setVisible(false);
        }
    }

    /**
     * Undoes the moves by making the window visible.
     */
    public void undo() {
        try {
            editor.setVisible(true);
            editor.setSelected(true);
        } catch (Exception e) {
            // empty
        }
    }

    /**
     * A string representation of the moves for the moves history window
     *
     * @return the name of the editor + closed
     */
    public String toString() {
        return editor.getEditorName() + " closed";
    }

    /**
     * The name of the moves, used in the xml representation of the moves
     *
     * @return "closeWindowCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * The attributes for the xml representation of the moves
     *
     * @return
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[1];
        att[0] = new Attribute(EDITOR, editor.getEditorName());
        return att;
    }
}
