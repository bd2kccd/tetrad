package edu.cmu.causalityApp.navigator;

/**
 * This listener keep tracks of any changes to the navigator panel.
 *
 * @author mattheweasterday
 */
public interface NavigatorChangeListener {

    /**
     * Detects a change and triggers a response.
     */
    public void navigatorChanged(NavigatorChangeEvent event);
}
