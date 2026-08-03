package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

// should fix this up but think this works? also pretty sure it's dispatched even when damage immune as its injected before actuallyHurt and actuallyHurt has check
public class EntityDamageEvent extends EntityEvent {

    private final DamageSource source;

    @ApiStatus.Internal
    public EntityDamageEvent(@NotNull Entity damagee, @NotNull DamageSource source) {
        super(damagee);
        this.source = source;
    }

    public @NotNull DamageSource getDamageSource() {
        return this.source;
    }
}
