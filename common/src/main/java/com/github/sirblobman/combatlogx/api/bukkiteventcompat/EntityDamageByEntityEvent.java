package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class EntityDamageByEntityEvent extends EntityDamageEvent {

    private final Entity damager;

    @ApiStatus.Internal
    public EntityDamageByEntityEvent(@NotNull Entity damager, @NotNull Entity damagee, @NotNull DamageSource source) {
        super(damagee, source);
        this.damager = damager;
    }

    @NotNull
    public Entity getDamager() {
        return this.damager;
    }
}
