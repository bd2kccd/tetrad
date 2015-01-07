package edu.cmu.causalityApp.commands;

import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

/**
 * Command for opening editors
 *
 * @author mattheweasterday
 */
public class OpenWindowCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "openWindowCommand";

    private static final String EDITOR = "editor";
    private AbstractEditor editor;

    /**
     * Constructor
     *
     * @param editor the editor to open
     */
    public OpenWindowCommand(AbstractEditor editor) {
        if (editor == null) {
            throw new IllegalArgumentException("No editor specified.");
        }

        this.editor = editor;
    }


    /**
     * Executes moves by setting editor visible
     */
    public void justDoIt() {
        try {
            editor.setVisible(true);
            editor.setSelected(true);
        } catch (Exception e) {
            // empty
        }
    }


    /**
     * Undoes moves by setting the editor invisible
     */
    public void undo() {
        editor.setVisible(false);
    }


    /**
     * String representation of the moves used for display in moves history
     *
     * @return toString of moves used for display in moves history
     */
    public String toString() {
        return editor.getEditorName() + " opened";
    }

    /**
     * Name of moves used in xml representation of moves
     *
     * @return "openWindowCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }

    /**
     * Attributes used in xml representation moves
     *
     * @return array of attributes
     */
    protected Attribute[] renderAttributes() {
        Attribute[] att = new Attribute[1];
        att[0] = new Attribute(EDITOR, editor.getEditorName());
        return att;
    }

}
