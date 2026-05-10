package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Dispatched whenever a LivingEntity dies
 */
public class EntityDeathEvent extends EntityEvent {

    private final DamageSource damageSource;
    private boolean cancelled;

    @ApiStatus.Internal
    public EntityDeathEvent(@NotNull LivingEntity livingEntity, @NotNull DamageSource damageSource) {
        super(livingEntity);
        this.damageSource = damageSource;
    }

    @NotNull
    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) this.entity;
    }

    /**
     * Gets the source of damage which caused the death.
     *
     * @return a DamageSource detailing the source of the damage for the death.
     */
    @NotNull
    public DamageSource getDamageSource() {
        return this.damageSource;
    }
}