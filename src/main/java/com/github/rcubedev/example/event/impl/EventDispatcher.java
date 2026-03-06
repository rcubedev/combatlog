package com.github.rcubedev.example.event.impl;

import com.github.rcubedev.example.event.api.Event;

/**
 * Helper class to dispatch events polymorphically to all relevant handlers.
 * When you dispatch a child event, it will be processed by:
 * 1. Its own handler's listeners
 * 2. All parent type handlers' listeners (in order from most specific to most general)
 */
public final class EventDispatcher {

    private EventDispatcher() {}

    /**
     * Dispatch an event to its handler and all parent handlers.
     * 
     * Example:
     * PlayerLoginEvent extends PlayerEvent extends CancellableEvent extends Event
     * 
     * EventDispatcher.dispatch(loginEvent) will call:
     * 1. PlayerLoginEvent handler
     * 2. PlayerEvent handler  
     * 3. CancellableEvent handler
     *
     * @param event The event to dispatch
     */
    public static void dispatch(Event event) {
        Class<? extends Event> eventType = event.eventType();
        dispatchToType(event, eventType);
    }

    /**
     * Internal method to dispatch an event starting from a specific type in the hierarchy.
     */
    @SuppressWarnings("unchecked")
    private static <E extends Event> void dispatchToType(Event event, Class<E> targetType) {
        ArrayBackedEventHandler<E> handler = EventHandlerFactoryImpl.getHandler(targetType);
        
        if (handler != null) {
            // Dispatch to this handler's invoker
            handler.invoker().process((E) event);
        }
    }
}