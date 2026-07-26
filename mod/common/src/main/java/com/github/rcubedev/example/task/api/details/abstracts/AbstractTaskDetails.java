package com.github.rcubedev.example.task.api.details.abstracts;

import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public abstract class AbstractTaskDetails {

    private final TaskInfo info;

    public AbstractTaskDetails(@NotNull TaskInfo info) {
        this.info = info;
    }

    public @NotNull TaskInfo getInfo() {
        return this.info;
    }
}
