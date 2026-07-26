package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity explodes interacting with blocks. The
 * event isn't called if mob griefing is disabled as no block
 * interaction will occur.
 */
public class EntityExplodeEvent extends EntityEvent {

    private final Vec3 location;
    private final Explosion.BlockInteraction result;

    @ApiStatus.Internal
    public EntityExplodeEvent(@NotNull Entity entity, @NotNull Vec3 location, @NotNull Explosion.BlockInteraction result) {
        super(entity);
        this.location = location;
        this.result = result;
    }

    public @NotNull Explosion.BlockInteraction getExplosionResult() {
        return this.result;
    }

    public @NotNull Vec3 getLocation() {
        return this.location;
    }
}