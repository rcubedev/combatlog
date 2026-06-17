package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;

import com.github.rcubedev.example.event.api.Event;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityExplodeEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private @Nullable Entity source;

    @Shadow
    public abstract Explosion.BlockInteraction getBlockInteraction();

    @Inject(method = "finalizeExplosion", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;iterator()Lit/unimi/dsi/fastutil/objects/ObjectListIterator;", ordinal = 0))
    private void fireExplodeEvent(boolean bl, CallbackInfo ci) {
        Event event;
        if (this.source != null) event = new EntityExplodeEvent(this.source, this.source.position(), getBlockInteraction());
        else return; // for now no-op; if needed in future impl block explosion event
        event.dispatch();
    }
}
