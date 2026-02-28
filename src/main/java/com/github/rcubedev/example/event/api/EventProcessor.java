package com.github.rcubedev.example.event.api;

/**
 * Functional interface for dispatching or listening to an event.
 */
@FunctionalInterface
public interface EventProcessor<E extends Event<E>> {
    void process(Event event);
}
