package com.github.rcubedev.example.task.api;

import org.jetbrains.annotations.NotNull;

public interface Task {

    /**
     * Returns the id for the task.
     *
     * @return id number
     */
    int getId();

    /**
     * Returns the {@link TaskOwner} that owns this task.
     *
     * @return The {@link TaskOwner} that owns the task
     */
    @NotNull TaskOwner getOwner();

    /**
     * Returns true if this task has been cancelled.
     *
     * @return true if the task has been cancelled
     */
    boolean isCancelled();

    /**
     * Will attempt to cancel this task.
     */
    void cancel();
}
