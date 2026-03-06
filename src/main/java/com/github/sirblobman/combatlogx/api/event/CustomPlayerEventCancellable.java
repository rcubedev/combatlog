package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.world.entity.player.Player;

import com.github.rcubedev.example.event.api.Cancellable;
import com.github.rcubedev.example.event.api.EventHandler;
import org.jetbrains.annotations.NotNull;

public abstract class CustomPlayerEventCancellable extends CustomPlayerEvent implements Cancellable {

    private boolean cancelled;

    public CustomPlayerEventCancellable(@NotNull Player player) {
        super(player);
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public abstract EventHandler<? extends CustomPlayerEventCancellable> handler();

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
