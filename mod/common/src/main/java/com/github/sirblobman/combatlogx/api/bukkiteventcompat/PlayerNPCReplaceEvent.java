package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a player logs in and there is an existing NPC entity for them created by {@link PlayerDisconnectEvent#cancel()}.
 */
public class PlayerNPCReplaceEvent extends PlayerEvent {

    private final ServerPlayer oldNpc;

    @ApiStatus.Internal
    public PlayerNPCReplaceEvent(@NotNull ServerPlayer oldNpc, @NotNull ServerPlayer player) {
        super(player);
        this.oldNpc = oldNpc;
    }

    /**
     * @return The old NPC entity that was left behind when the player disconnected.
     */
    public @NotNull ServerPlayer getOldNPC() {
        return this.oldNpc;
    }

    /**
     * @return The new {@link ServerPlayer} entity that has just logged in.
     */
    public @NotNull ServerPlayer getNewPlayer() {
        return this.getPlayer();
    }
}
