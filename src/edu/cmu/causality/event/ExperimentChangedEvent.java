package edu.cmu.causality.event;

/**
 * This event should be thrown whenever the exerimental setup is changed so
 * that other windows/editors affected by the experimental setup know to update
 * their state.
 *
 * @author mattheweasterday
 */
public class ExperimentChangedEvent extends ModelChangedEvent {

    /**
     * Concstructor.
     *
     * @param source         the object that stated the event.
     *
     */
    public ExperimentChangedEvent(Object source) {
        super(source, "");
    }

}
