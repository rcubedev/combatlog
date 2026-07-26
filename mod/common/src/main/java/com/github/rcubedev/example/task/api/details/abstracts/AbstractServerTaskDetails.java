package com.github.rcubedev.example.task.api.details.abstracts;

import java.util.function.Consumer;

import com.github.rcubedev.example.task.api.TaskScheduler;
import net.minecraft.server.MinecraftServer;

import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractServerTaskDetails extends AbstractTaskDetails implements Consumer<MinecraftServer> {
    public AbstractServerTaskDetails(@NotNull TaskInfo info) {
        super(info);
    }

    /**
     * Called by {@link TaskScheduler TaskScheduler}.
     * @param server the server
     */
    @Override
    public abstract void accept(@NotNull MinecraftServer server);

    public final void run(@NotNull MinecraftServer server) {
        accept(server);
    }
}
