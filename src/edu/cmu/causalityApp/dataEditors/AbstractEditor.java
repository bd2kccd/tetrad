package edu.cmu.causalityApp.dataEditors;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * This is the superclass for all editors, such as the graph editors and the
 * experimental setup editor.
 *
 * @author mattheweasterday
 */
abstract public class AbstractEditor extends JInternalFrame implements InternalFrameListener {

    /**
     * Constructor.
     */
    protected AbstractEditor(String title) {
        super(title, true, true, false, false);
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
    }

    /**
     * @return the unique string id of this editor.
     */
    abstract public String getEditorName();


    /**
     * Use this to signal that this editor gains focus.
     */
    public void internalFrameActivated(InternalFrameEvent e) {
    }

    /**
     * Use this to signal that this editor is closed.
     */
    public void internalFrameClosed(InternalFrameEvent e) {
    }

    /**
     * Use this to signal that this editor is being closed.
     */
    public void internalFrameClosing(InternalFrameEvent e) {
        CloseWindowCommand cwc = new CloseWindowCommand(this);
        cwc.doIt();
    }

    /**
     * Use this to signal that this editor loses focus.
     */
    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    /**
     * Use this to signal that this editor is restored from its minimized state.
     */
    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    /**
     * Use this to signal that this editor is minimized.
     */
    public void internalFrameIconified(InternalFrameEvent e) {
    }

    /**
     * Use this to signal that this editor is opened.
     */
    public void internalFrameOpened(InternalFrameEvent e) {
    }
}
