package edu.cmu.causalityApp.commands;

import edu.cmu.causalityApp.dataEditors.AbstractEditor;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

/**
 * Command for focusing editors
 *
 * @author adrian
 */
public class FocusWindowCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "focusWindowCommand";
    private static final String EDITOR = "editor";

    private final AbstractEditor editor;

    /**
     * Constructor
     *
     * @param editor the editor to focus
     */
    public FocusWindowCommand(AbstractEditor editor) {
        this.editor = editor;
    }


    /**
     * Executes moves by setting editor visible
     */
    public void justDoIt() {
        try {
            editor.setSelected(true);
        } catch (Exception e) {
            // Empty
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
     * @return the toSTring
     */
    public String toString() {
        //todo: translation
        return editor.getEditorName() + " focused";
    }

    /**
     * Name of moves used in xml representation of moves
     *
     * @return "focusWindowCommand"
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
