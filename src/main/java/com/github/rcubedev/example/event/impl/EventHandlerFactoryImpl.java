package com.github.rcubedev.example.event.impl;

import com.github.rcubedev.example.event.api.Event;
import com.github.rcubedev.example.event.api.EventHandler;
import com.github.rcubedev.example.event.api.EventProcessor;
import com.google.common.collect.MapMaker;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * Internal implementation for array-backed events.
 */
public final class EventHandlerFactoryImpl {

    private static final Set<ArrayBackedEventHandler<?>> EVENT_HANDLERS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private EventHandlerFactoryImpl() {}

    public static void invalidate() {
        EVENT_HANDLERS.forEach(ArrayBackedEventHandler::update);
    }

    /**
     * Create an array-backed event handler instance.
     */
    public static <E extends Event> EventHandler<E> createArrayBacked(Function<EventProcessor<E>[], EventProcessor<E>> invokerFactory) {
        @SuppressWarnings("unchecked")
        ArrayBackedEventHandler<E> handler = new ArrayBackedEventHandler<>((Class<EventProcessor<E>>) (Class<?>) EventProcessor.class, invokerFactory);
        EVENT_HANDLERS.add(handler);
        return handler;
    }
}