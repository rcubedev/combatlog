package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.utils.event.api.Cancellable;
import org.jetbrains.annotations.NotNull;

public abstract class CustomPlayerEventCancellable extends CustomPlayerEvent implements Cancellable {

    private boolean cancelled;

    public CustomPlayerEventCancellable(@NotNull ServerPlayer player) {
        super(player);
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
