package com.github.rcubedev.example.event.impl;

import java.lang.ref.WeakReference;

import com.github.rcubedev.example.event.api.Event;
import com.github.rcubedev.example.event.api.EventProcessor;

// todo
//  this might be moved to api pkg.
public final class WeakEventProcessor<T, E extends Event> implements EventProcessor<E> {
    private final WeakReference<T> targetRef;
    private final UnboundProcessor<T, E> invoker; // this should be implemented via metafactory if using EventSubscriberHandler

    public WeakEventProcessor(T target, UnboundProcessor<T, E> invoker) {
        this.targetRef = new WeakReference<>(target);
        this.invoker = invoker;
    }

    @Override
    public void process(E event) {
        T target = targetRef.get();
        if (target != null) {
            invoker.process(target, event);
        }
    }
}
