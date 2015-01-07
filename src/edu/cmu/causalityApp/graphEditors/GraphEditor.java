package edu.cmu.causalityApp.graphEditors;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.dataEditors.AbstractEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import java.awt.*;

/**
 * This is the superclass to the graph editors such as Correct Graph editor, and
 * Hypothetical Graph editor.
 *
 * @author greg
 */
public abstract class GraphEditor extends AbstractEditor {
    private static final String EDIT_NONE = "none";
    public static final String EDIT_MOVE = "move";
    public static final String EDIT_DRAW = "draw";

    // todo: encapsulate these variables
    private final CausalityLabModel model;
    private JToolBar toolbar;
    private String editMode;
    private JComponent mainView;
    private final JDesktopPane desktop;
    private final InternalFrameListener frameListener;

    /**
     * Constructor.
     */
    protected GraphEditor(CausalityLabModel model,
                          InternalFrameListener parent,
                          JDesktopPane desktop,
                          String name,
                          boolean editable) {
        super(name);

        this.model = model;
        editMode = EDIT_NONE;
        frameListener = parent;

        if (editable) {
            toolbar = new JToolBar();
            getToolbar().setFloatable(false);
            fillToolbar();
            getContentPane().add(getToolbar(), BorderLayout.NORTH);
        }

        this.desktop = desktop;
        addInternalFrameListener(getFrameListener());

    }

    /**
     * Sets up the toolbar.
     */
    protected abstract void fillToolbar();

    /**
     * @return the edit mode of this graph editor.
     */
    public String getEditMode() {
        return editMode;
    }

    /**
     * Sets the edit mode of this graph editor. Can the graph be edited?
     */
    public void setEditMode(String editMode) {
        this.editMode = editMode;
    }

    /**
     * Adds edge between two nodes.
     */
    public abstract void addEdge(String fromName, String toName);

    public CausalityLabModel getModel() {
        return model;
    }

    public JToolBar getToolbar() {
        return toolbar;
    }

    public JComponent getMainView() {
        return mainView;
    }

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public InternalFrameListener getFrameListener() {
        return frameListener;
    }

    public void setMainView(JComponent mainView) {
        this.mainView = mainView;
    }
}
