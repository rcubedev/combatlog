package com.github.rcubedev.example.task.api.details.abstracts;

import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRunnableTaskDetails extends AbstractTaskDetails implements Runnable {

    public AbstractRunnableTaskDetails(@NotNull TaskInfo info) {
        super(info);
    }

    @Override
    public abstract void run();
}
