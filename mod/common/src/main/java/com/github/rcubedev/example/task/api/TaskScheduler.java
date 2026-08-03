package com.github.rcubedev.example.task.api;

import com.github.rcubedev.example.task.api.details.RunnableTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractPlayerTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractServerTaskDetails;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public interface TaskScheduler {

    /**
     * Schedule a once off runnable task at the given tick phase.
     *
     * @param details the task to schedule
     * @return the {@link Task} handle that can be used to cancel the task
     * @throws IllegalArgumentException if {@code type} requires a server or player context
     */
    @NotNull Task schedule(@NotNull AbstractRunnableTaskDetails details);

    /**
     * Schedule a {@link net.minecraft.server.MinecraftServer MinecraftServer} context task at the given tick phase.
     *
     * @param details the task to schedule
     * @return the {@link Task} handle
     */
    @NotNull Task scheduleServer(@NotNull AbstractServerTaskDetails details);

    /**
     * Schedule a {@link net.minecraft.server.level.ServerPlayer ServerPlayer} context task at the given tick phase.
     *
     * @param details the task to schedule
     * @return the {@link Task} handle
     */
    @NotNull Task schedulePlayer(@NotNull AbstractPlayerTaskDetails details);

    /**
     * Removes a task by id. Silently does nothing if the id does not exist.
     *
     * @param owner the owner that scheduled the task
     * @param id the task id
     */
    void cancelTask(@NotNull TaskOwner owner, int id);

    /**
     * Removes all tasks associated with the owner in the given bucket.
     *
     * @param owner the owner
     * @param type the bucket to search
     */
    void cancelTasks(@NotNull TaskOwner owner, @NotNull TaskType type);

    /**
     * Removes all tasks associated with a particular owner from the
     * scheduler.
     *
     * @param owner owner of tasks to be removed
     */
    void cancelTasks(@NotNull TaskOwner owner);

    /**
     * Returns an executor that will run tasks on the next server tick.
     *
     * @param owner the reference to the task scheduler
     * @return an executor associated with the given plugin
     */
    default @NotNull Executor getMainThreadExecutor(@NotNull TaskOwner owner) {
        return runnable -> schedule(new RunnableTaskDetails(owner, TaskType.START_TICK, 0, runnable));
    }
}