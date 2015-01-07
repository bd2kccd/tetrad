package edu.cmu.causalityApp.dataEditors;

import javax.swing.*;

/**
 * This listener watches out if the focus on the getModel experimental setup has
 * changed or not.
 *
 * @author mattheweasterday
 */
public interface ExperimentalSetupFocusListener {
    public void fireESViewChangedEvent(JInternalFrame source, String expName);
}
