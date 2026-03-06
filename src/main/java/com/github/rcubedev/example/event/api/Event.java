package com.github.rcubedev.example.event.api;

/**
 * Base class for all events.
 *
 */
public abstract class Event {

    /**
     * Returns the handler instance.
     *
     * @return The handler instance.
     */
    public abstract EventHandler<? extends Event> handler();

    /**
     * Returns the runtime class of this event.
     *
     * @return The event class.
     */
    public final Class<? extends Event> eventType() {
        return this.getClass();
    }
}