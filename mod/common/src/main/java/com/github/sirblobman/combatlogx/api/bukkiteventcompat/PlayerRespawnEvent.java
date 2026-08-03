package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

// fixme this doesn't really have bukkit parity as respawn event is dispatched on end portal travel
public class PlayerRespawnEvent extends PlayerEvent {

    @ApiStatus.Internal
    public PlayerRespawnEvent(@NotNull ServerPlayer player) {
        super(player);
    }
}
