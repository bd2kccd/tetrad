package edu.cmu.command;

/**
 * This interface should be implemented by classes that want to be notified
 * of changes to the Exercise history.
 *
 * @author mattheweasterday
 */
public interface ExerciseHistoryListener {

    /**
     * Callback for when history has changed.
     */
    public void historyChanged();
}
