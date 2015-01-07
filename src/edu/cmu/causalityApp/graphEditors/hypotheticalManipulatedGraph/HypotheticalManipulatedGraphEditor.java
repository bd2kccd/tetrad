package edu.cmu.causalityApp.graphEditors.hypotheticalManipulatedGraph;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.causalityApp.graphEditors.GraphEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import java.awt.*;

/**
 * This class describes the Hypothetical Manipulated Editor frame which shows
 * the correct hypothetical manipulated graph, given an experimental setup in
 * the exercise.
 *
 * @author greg
 */
public class HypotheticalManipulatedGraphEditor extends GraphEditor {

    /**
     * Unique string id of this editor.
     */
    public static final String MY_NAME = "Hypothetical Manipulated Graph";

    /**
     * Constructor.
     */
    public HypotheticalManipulatedGraphEditor(CausalityLabModel model,
                                              InternalFrameListener listener,
                                              JDesktopPane desktop) {
        super(model, listener, desktop, MY_NAME, true);
        setMainView(new HypotheticalManipulatedGraphView(null, null, this, model));
        getContentPane().add(getMainView(), BorderLayout.CENTER);
    }

    /**
     * @return a thumbnail of the hypothetical manipulated graph view.
     */
    public ImageIcon getThumbnail(String expName, String hypName, double ratio) {
        HypotheticalManipulatedGraphView hmgb = new HypotheticalManipulatedGraphView(expName, hypName, this, getModel());
        return hmgb.getThumbnailImage(ratio);
    }


    /**
     * Set getModel experimental setup in focus.
     */
    public void setExperimentalSetupFocus(String experimentalSetupName) {
        ((HypotheticalManipulatedGraphView) getMainView()).setExpName(experimentalSetupName);
    }

    /**
     * Set getModel hypothetical graph in focus.
     */
    public void setHypotheticalGraphFocus(String hypotheticalGraphName) {
        ((HypotheticalManipulatedGraphView) getMainView()).setHypName(hypotheticalGraphName);
    }

    /**
     * Sets up toolbar. This editor does not have a toolbar.
     */
    protected void fillToolbar() {
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
