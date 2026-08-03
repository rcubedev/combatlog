package com.github.rcubedev.example.task.api.details.abstracts;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

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
