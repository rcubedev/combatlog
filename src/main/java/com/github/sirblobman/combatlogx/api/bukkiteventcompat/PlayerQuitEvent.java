package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player leaves a server
 */
public class PlayerQuitEvent extends PlayerEvent {

    @ApiStatus.Internal
    public PlayerQuitEvent(@NotNull final ServerPlayer player) {
        super(player);
    }
}