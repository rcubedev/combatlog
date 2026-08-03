package com.github.rcubedev.example.task.api.details;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractPlayerTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A basic implementation of {@link PlayerTaskDetails}.
 * <p>
 * This implementation is non-repeating and uses a {@link Consumer} of {@link ServerPlayer}.
 * <p>
 * For more control, extend {@link AbstractPlayerTaskDetails} and provide your own implementation.
 */
public final class PlayerTaskDetails extends AbstractPlayerTaskDetails {

    private final Consumer<ServerPlayer> consumer;

    public PlayerTaskDetails(@NotNull TaskOwner owner, @NotNull TaskType type, long delay,
                             @NotNull Consumer<ServerPlayer> consumer) {
        super(new TaskInfo(owner, type).setDelay(delay));
        this.consumer = consumer;
    }

    @Override
    public void accept(@NotNull ServerPlayer player) {
        consumer.accept(player);
    }
}