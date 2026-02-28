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
    private final Object lock = new Object();
    private EventProcessor<E>[] listeners;
    private final Map<Priority, EventPhaseData<E>> phases = new EnumMap<>(Priority.class);
    private final List<EventPhaseData<E>> sortedPhases = new ArrayList<>();

    private volatile EventProcessor<E> invoker;

    @SuppressWarnings("unchecked")
    public ArrayBackedEventHandler(Class<EventProcessor<E>> type, Function<EventProcessor<E>[], EventProcessor<E>> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.listeners = (EventProcessor<E>[]) Array.newInstance(type, 0);
        update();
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
                // NodeSorting.sort(sortedPhases, "event phases", Comparator.comparing(data -> data.priority));
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
}