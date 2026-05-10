package com.github.rcubedev.example.task.impl;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractTaskDetails;
import org.jetbrains.annotations.NotNull;

public final class ScheduledEntry<T extends AbstractTaskDetails> {

    private final TaskType type;
    private final T details;
    private final TaskImpl task;
    private long ticksUntilRun;
    private final long period; // 0 = run once

    public ScheduledEntry(TaskType type, T details, TaskImpl task, long delay, long period) {
        this.type = type;
        this.details = details;
        this.task = task;
        this.ticksUntilRun = Math.max(0L, delay);
        this.period = period > 0 ? period : 0; // todo maybe throw illegal arg ex?
    }

    public @NotNull TaskType getType() {
        return this.type;
    }

    public @NotNull T getDetails() {
        return this.details;
    }

    public @NotNull TaskImpl getTask() {
        return this.task;
    }

    public @NotNull TaskOwner getOwner() {
        return getTask().getOwner();
    }

    public boolean isCancelled() {
        return getTask().isCancelled();
    }

    /**
     * Tick down the countdown.
     *
     * @return {@code true} if the task should fire this tick.
     */
    public boolean tick() {
        if (ticksUntilRun > 0) {
            ticksUntilRun--;
            return false;
        }
        return true;
    }

    /**
     * Reset the countdown for a repeating task.
     *
     * @return {@code true} if this task should be kept (repeating), {@code false} if it is run once.
     */
    boolean reset() {
        if (period > 0) {
            ticksUntilRun = period - 1; // -1 because we tick() before checking
            return true;
        }
        return false;
    }
}
