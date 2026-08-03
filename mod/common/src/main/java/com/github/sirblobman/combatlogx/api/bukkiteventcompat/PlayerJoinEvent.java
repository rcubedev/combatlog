package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player joins a server
 */
public class PlayerJoinEvent extends PlayerEvent {

    @ApiStatus.Internal
    public PlayerJoinEvent(@NotNull ServerPlayer playerJoined) {
        super(playerJoined);
    }
}
