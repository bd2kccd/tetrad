package edu.cmu.causality.event;


/**
 * This event should be thrown whenever the hypotheses are changed so
 * that other windows/editors affected by the hypotheses know to update
 * their state.
 *
 * @author mattheweasterday
 */
public class HypothesisChangedEvent extends ModelChangedEvent {

    /**
     * Constructor.
     *
     * @param source the object that created the event
     */
    public HypothesisChangedEvent(Object source) {
        super(source, "");
    }
}
