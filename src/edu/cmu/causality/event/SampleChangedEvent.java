package edu.cmu.causality.event;

/**
 * This event should be thrown whenever the samples are changed so
 * that other windows/editors affected by the sample know to update
 * their state.
 *
 * @author mattheweasterday
 */
public class SampleChangedEvent extends ModelChangedEvent {

    /**
     * Constructor.
     *
     * @param source the object that created the event.
     */
    public SampleChangedEvent(Object source) {
        super(source, "");
    }
}
