package edu.cmu.causality.event;

import java.awt.event.ActionEvent;

/**
 * This is the base class for the other model events.
 *
 * @author mattheweasterday
 */

class ModelChangedEvent extends ActionEvent {

    private static int uniqueId = 0;

    /**
     * Constructor.
     *
     * @param source  the object that created the event.
     * @param command ???
     */
    ModelChangedEvent(Object source, String command) {
        super(source, uniqueId++, command);
    }

}
