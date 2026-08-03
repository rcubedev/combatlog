package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

// matches bukkit by extending event instead of entityevent
public abstract class PlayerEvent extends BukkitEvent {

    protected @NotNull ServerPlayer player;

    protected PlayerEvent(@NotNull ServerPlayer player) {
        this.player = player;
    }

    /**
     * Returns the player involved in this event
     *
     * @return the player who is involved in this event
     */
    public @NotNull ServerPlayer getPlayer() {
        return player;
    }
}
