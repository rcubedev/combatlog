package com.github.rcubedev.example.task.api.info;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TaskInfo {
    private final TaskOwner owner;
    private final @NotNull TaskType type;
    private @Nullable Long delay;
    private @Nullable Long period;

    public TaskInfo(@NotNull TaskOwner owner, @NotNull TaskType type) {
        this.owner = owner;
        this.type = type;
        this.delay = null;
        this.period = null;
    }

    public final @NotNull TaskOwner getOwner() {
        return this.owner;
    }

    public final @NotNull TaskType getType() {
        return this.type;
    }

    public final @Nullable Long getDelay() {
        return this.delay;
    }

    public final TaskInfo setDelay(@Nullable Long delay) {
        this.delay = delay;
        return this;
    }

    public final @Nullable Long getPeriod() {
        return this.period;
    }

    public final TaskInfo setPeriod(@Nullable Long period) {
        this.period = period;
        return this;
    }
}
