package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class FabricEventHook {

    public void register() {
        ServerPlayerEvents.JOIN.register(this::fireJoinEvent);
        ServerPlayerEvents.LEAVE.register(this::fireQuitEvent);
        ServerLivingEntityEvents.AFTER_DEATH.register(this::fireDeathEvent);
    }

    public void fireJoinEvent(ServerPlayer player) {
        PlayerJoinEvent event = new PlayerJoinEvent(player);
        event.dispatch();
    }

    public void fireQuitEvent(ServerPlayer player) {
        PlayerQuitEvent event = new PlayerQuitEvent(player);
        event.dispatch();
    }

    public void fireDeathEvent(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer) return; // no-op; PlayerDeathEvent dispatched by mixin
        EntityDeathEvent event = new EntityDeathEvent(entity, source);
        event.dispatch();
    }
}
