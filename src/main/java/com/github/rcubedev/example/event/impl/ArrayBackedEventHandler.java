package com.github.rcubedev.example.event.impl;

import com.github.rcubedev.example.event.api.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ArrayBackedEventHandler<E extends Event> extends EventHandler<E> {

    private final Function<EventProcessor<E>[], EventProcessor<E>> invokerFactory;
    private final Class<E> eventType;
    private final Object lock = new Object();
    private EventProcessor<E>[] listeners;
    private final Map<Priority, EventPhaseData<E>> phases = new EnumMap<>(Priority.class);
    private final List<EventPhaseData<E>> sortedPhases = new ArrayList<>();

    private volatile EventProcessor<E> invoker;
    // Child handlers that should receive this handler's events
    private final List<ArrayBackedEventHandler<? extends E>> childHandlers = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public ArrayBackedEventHandler(Class<E> eventType, Class<EventProcessor<E>> processorType, Function<EventProcessor<E>[], EventProcessor<E>> invokerFactory) {
        this.eventType = eventType;
        this.invokerFactory = invokerFactory;
        this.listeners = (EventProcessor<E>[]) Array.newInstance(processorType, 0);
        update();
    }

    /**
     * Register a child handler. Called when a subtype handler is created.
     */
    void registerChildHandler(ArrayBackedEventHandler<? extends E> childHandler) {
        synchronized (lock) {
            childHandlers.add(childHandler);
        }
    }

    public void update() {
        this.invoker = invokerFactory.apply(listeners);
    }

    @Override
    public void register(@NotNull EventProcessor<E> listener) {
        register(Priority.NORMAL, listener);
    }

    @Override
    public void register(@NotNull Priority priority, @NotNull EventProcessor<E> listener) {
        Objects.requireNonNull(priority, "Tried to register a listener for a null priority!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");

        synchronized (lock) {
            getOrCreatePhase(priority, true).addListener(listener);
            rebuildInvoker(listeners.length + 1);
        }
    }

    @SuppressWarnings("unchecked")
    private EventPhaseData<E> getOrCreatePhase(Priority id, boolean sortIfCreate) {
        EventPhaseData<E> phase = phases.get(id);

        if (phase == null) {
            phase = new EventPhaseData<>(id, (Class<EventProcessor<E>>) listeners.getClass().getComponentType());
            phases.put(id, phase);
            sortedPhases.add(phase);

            if (sortIfCreate) {
                sortedPhases.sort(Comparator.comparing(data -> data.priority));
            }
        }

        return phase;
    }

    private void rebuildInvoker(int newLength) {
        // Rebuild handlers.
        if (sortedPhases.size() == 1) {
            // Special case with a single phase: use the array of the phase directly.
            listeners = sortedPhases.getFirst().listeners;
        } else {
            @SuppressWarnings("unchecked")
            EventProcessor<E>[] newHandlers = (EventProcessor<E>[]) Array.newInstance(listeners.getClass().getComponentType(), newLength);
            int newHandlersIndex = 0;

            for (EventPhaseData<E> existingPhase : sortedPhases) {
                int length = existingPhase.listeners.length;
                System.arraycopy(existingPhase.listeners, 0, newHandlers, newHandlersIndex, length);
                newHandlersIndex += length;
            }

            listeners = newHandlers;
        }

        // Rebuild invoker.
        update();
    }

    @Override
    public EventProcessor<E> invoker() {
        return invoker;
    }

    /**
     * Get the event type this handler manages.
     */
    public Class<E> getEventType() {
        return eventType;
    }

    /**
     * Get all child handlers registered with this handler.
     */
    public List<ArrayBackedEventHandler<? extends E>> getChildHandlers() {
        synchronized (lock) {
            return new ArrayList<>(childHandlers);
        }
    }
}