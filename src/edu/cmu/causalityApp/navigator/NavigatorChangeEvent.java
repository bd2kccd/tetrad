package edu.cmu.causalityApp.navigator;

import java.awt.event.ActionEvent;

/**
 * This class describes a navigator change event triggered when there is a change
 * to the navigator panel.
 *
 * @author mattheweasterday
 */
public class NavigatorChangeEvent extends ActionEvent {
    private static int uniqueId = 0;

    public NavigatorChangeEvent(Object source, String command) {
        super(source, uniqueId++, command);
    }
}
