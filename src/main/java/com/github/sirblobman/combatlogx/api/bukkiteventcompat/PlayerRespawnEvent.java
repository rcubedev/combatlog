package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class PlayerRespawnEvent extends PlayerEvent {

    @ApiStatus.Internal
    public PlayerRespawnEvent(@NotNull ServerPlayer player) {
        super(player);
    }
}
