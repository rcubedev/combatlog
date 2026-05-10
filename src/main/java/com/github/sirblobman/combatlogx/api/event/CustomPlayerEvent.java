package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class CustomPlayerEvent extends PlayerEvent {

    public CustomPlayerEvent(@NotNull ServerPlayer player) {
        super(player);
    }
}
