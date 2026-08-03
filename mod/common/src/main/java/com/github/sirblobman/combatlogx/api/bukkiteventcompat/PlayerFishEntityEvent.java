package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

// originally PlayerFishEvent
public class PlayerFishEntityEvent extends PlayerEvent {

    private final Entity entity;

    public PlayerFishEntityEvent(@NotNull ServerPlayer player, @NotNull Entity entity) {
        super(player);
        this.entity = entity;
    }

    public Entity getCaught() {
        return this.entity;
    }
}
