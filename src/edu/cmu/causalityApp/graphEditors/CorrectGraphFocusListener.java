package edu.cmu.causalityApp.graphEditors;

import javax.swing.*;

/**
 * This class describes the listener for any change in the hypothetical graph
 * currently in focus.
 *
 * @author mattheweasterday
 */
public interface CorrectGraphFocusListener {

    /**
     * Fires a change event when a change in the hypothetical graph is detected.
     */
    public void fireCgViewChangedEvent(JInternalFrame source, String correctGraphName);

}
