package com.github.rcubedev.example.task.api.details;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractServerTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A basic implementation of {@link ServerTaskDetails}.
 * <p>
 * This implementation is non-repeating and uses a {@link Consumer} of {@link MinecraftServer}.
 * <p>
 * For more control, extend {@link AbstractServerTaskDetails} and provide your own implementation.
 */
public final class ServerTaskDetails extends AbstractServerTaskDetails {

    private final Consumer<MinecraftServer> consumer;

    public ServerTaskDetails(@NotNull TaskOwner owner, long delay, @NotNull TaskType type,
                             @NotNull Consumer<MinecraftServer> consumer) {
        super(new TaskInfo(owner, type).setDelay(delay));
        this.consumer = consumer;
    }

    @Override
    public void accept(@NotNull MinecraftServer server) {
        consumer.accept(server);
    }
}