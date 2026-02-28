package com.github.rcubedev.example.event.api;

/**
 * Base class for all events.
 *
 * @param <E> the event type itself
 */
public abstract class Event<E extends Event<E>> {

    // /**
    //  * The handler field. This should be updated always refer to an instance containing
    //  * all code that should be executed upon event emission. <--- fixme
    //  */
    // private volatile EventHandler<E> handler;
    //
    // public Event(EventHandler<E> handler) {
    //     this.handler = handler;
    // }
    //
    // /**
    //  * Returns the handler instance.
    //  *
    //  * @return The handler instance.
    //  */
    // public final EventHandler<E> handler() {
    //     return handler;
    // }
}