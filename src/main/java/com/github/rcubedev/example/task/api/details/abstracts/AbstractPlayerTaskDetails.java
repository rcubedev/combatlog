package com.github.rcubedev.example.task.api.details.abstracts;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPlayerTaskDetails extends AbstractTaskDetails implements Consumer<ServerPlayer> {

    public AbstractPlayerTaskDetails(@NotNull TaskInfo info) {
        super(info);
    }

    /**
     * Called by {@link com.github.rcubedev.example.task.api.TaskScheduler TaskScheduler}.
     * @param player the player
     */
    @Override
    public abstract void accept(@NotNull ServerPlayer player);

    public final void run(@NotNull ServerPlayer player) {
        accept(player);
    }
}
