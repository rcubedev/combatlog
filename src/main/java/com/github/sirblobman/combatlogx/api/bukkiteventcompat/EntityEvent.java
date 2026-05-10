package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an Entity-related event
 */
public abstract class EntityEvent extends BukkitEvent {

    protected Entity entity;

    protected EntityEvent(@NotNull final Entity entity) {
        this.entity = entity;
    }

    /**
     * Returns the Entity involved in this event
     *
     * @return Entity who is involved in this event
     */
    public @NotNull Entity getEntity() {
        return this.entity;
    }
}