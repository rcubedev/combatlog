package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Dispatched whenever a {@link ServerPlayer} dies
 */
public class PlayerDeathEvent extends EntityDeathEvent {

    private @Nullable Component deathMessage;

    @ApiStatus.Internal
    public PlayerDeathEvent(final @NotNull ServerPlayer player, final @NotNull DamageSource damageSource) {
        super(player, damageSource);
    }

    @NotNull
    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) this.entity;
    }

    /**
     * Clarity method for getting the player. Not really needed except
     * for reasons of clarity.
     *
     * @return Player who is involved in this event
     */
    public @NotNull ServerPlayer getPlayer() {
        return this.getEntity();
    }

    public @Nullable Component getDeathMessage() {
        return this.deathMessage;
    }

    public void setDeathMessage(@Nullable Component deathMessage) {
        this.deathMessage = deathMessage;
    }
}