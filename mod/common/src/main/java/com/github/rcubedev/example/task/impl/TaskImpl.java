package com.github.rcubedev.example.task.impl;

import com.github.rcubedev.example.task.api.Task;
import com.github.rcubedev.example.task.api.TaskOwner;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric implementation of {@link Task}.
 * <p>
 * Cancellation is a simple volatile flag; the scheduler checks it before executing
 * and prunes cancelled entries between ticks.
 * </p>
 */
public final class TaskImpl implements Task {

    private final TaskOwner owner;
    private final int id;
    private volatile boolean cancelled = false;

    public TaskImpl(TaskOwner owner, int id) {
        this.owner = owner;
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public @NotNull TaskOwner getOwner() {
        return this.owner;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}