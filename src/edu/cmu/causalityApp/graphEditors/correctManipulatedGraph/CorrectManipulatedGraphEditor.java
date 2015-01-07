package edu.cmu.causalityApp.graphEditors.correctManipulatedGraph;


import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.graphEditors.GraphEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;

/**
 * This class describes the Correct Manipulated Graph Editor frame which shows
 * the correct manipulated graph, given an experimental setup in the exercise.
 *
 * @author greg
 */
public class CorrectManipulatedGraphEditor extends GraphEditor {

    /**
     * Unique string id of this editor.
     */
    public static final String MY_NAME = "Correct Manipulated Graph";

    private String expName = null;

    /**
     * Constructor.
     */
    public CorrectManipulatedGraphEditor(CausalityLabModel model,
                                         InternalFrameListener listener,
                                         JDesktopPane desktop) {
        super(model, listener, desktop, MY_NAME, true);
        setMainView(new CorrectManipulatedGraphView(null, this, model));
        setContentPane(getMainView());
    }

    /**
     * Sets the getModel experimental setup in focus andd refreshes the graph view.
     */
    public void setExperimentalSetupFocus(String experimentalSetupName) {
        this.expName = experimentalSetupName;
        ((CorrectManipulatedGraphView) getMainView()).setExpName(experimentalSetupName);
        ((CorrectManipulatedGraphView) getMainView()).refreshViews();
    }

    /**
     * Sets up the toolbar. This editor does not have any toolbar.
     */
    protected void fillToolbar() {
    }

    /**
     * Hides or unhides the graph.
     */
    public void setHidden(boolean hidden) {
        if (hidden) {
            setMainView(new CorrectManipulatedGraphView.HiddenView(expName, this, getModel()));
            setContentPane(getMainView());
        } else {
            setMainView(new CorrectManipulatedGraphView(expName, this, getModel()));
            setContentPane(getMainView());
        }
    }


    /**
     * Adds edge between two nodes. The graph in this editor is not
     * editable.
     */
    public void addEdge(String fromName, String toName) {
    }

    /**
     * @return unique string id of this editor.
     */
    public String getEditorName() {
        return MY_NAME;
    }
}
