package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;

import org.jetbrains.annotations.Nullable;
import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Shadow
    private @Nullable Entity hookedIn;

    @Shadow
    public abstract @Nullable Player getPlayerOwner();

    @Inject(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;pullEntity(Lnet/minecraft/world/entity/Entity;)V"))
    private void caughtEntity(CallbackInfoReturnable<Integer> cir) {
        Player player = this.getPlayerOwner();
        DamageEventListener.onFishEntity(player, hookedIn);
    }
}
