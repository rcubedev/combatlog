package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void onHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer player) {
            // todo :: maybe use after hurt event instead but that isnt called on death; is that wanted though?
            DamageEventListener.onHurt(player, damageSource);
        }
    }
}
