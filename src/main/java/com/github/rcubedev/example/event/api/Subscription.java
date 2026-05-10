package com.github.rcubedev.example.event.api;

/**
 * A handle to an active event registration. 
 * Calling {@link #unsubscribe()} or {@link #close()} removes the associated 
 * listeners from the bus and triggers a dispatch table rebuild.
 */
// todo
public interface Subscription extends AutoCloseable {
    
    void unsubscribe();

    @Override
    default void close() {
        unsubscribe();
    }
}