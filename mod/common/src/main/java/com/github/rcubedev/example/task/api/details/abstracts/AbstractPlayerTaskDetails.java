package com.github.rcubedev.example.task.api.details.abstracts;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractPlayerTaskDetails extends AbstractTaskDetails implements Consumer<ServerPlayer> {

    public AbstractPlayerTaskDetails(@NotNull TaskInfo info) {
        super(info);
    }

    /**
     * Called by {@link TaskScheduler TaskScheduler}.
     * @param player the player
     */
    @Override
    public abstract void accept(@NotNull ServerPlayer player);

    public final void run(@NotNull ServerPlayer player) {
        accept(player);
    }
}
