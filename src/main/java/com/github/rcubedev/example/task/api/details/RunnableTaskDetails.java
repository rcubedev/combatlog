package com.github.rcubedev.example.task.api.details;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.NotNull;

/**
 * A basic implementation of {@link RunnableTaskDetails}.
 * <p>
 * This implementation is non-repeating and uses and a {@link Runnable}.
 * <p>
 * For more control, extend {@link AbstractRunnableTaskDetails} and provide your own implementation.
 */
public final class RunnableTaskDetails extends AbstractRunnableTaskDetails {

    private final Runnable runnable;

    public RunnableTaskDetails(@NotNull TaskOwner owner, @NotNull TaskType type, long delay,
                               @NotNull Runnable runnable) {
        super(new TaskInfo(owner, type).setDelay(delay));
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }
}