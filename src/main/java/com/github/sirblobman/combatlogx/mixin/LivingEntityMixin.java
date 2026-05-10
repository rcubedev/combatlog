package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityDamageByEntityEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityDamageEvent;
import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import com.github.sirblobman.combatlogx.listener.EndCrystalListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void onHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        // todo :: maybe use after hurt event instead but that isnt called on death; is that wanted though? PaperMC calls on death too
        // fixme should this only be called if the entity is hurt by another?
        Entity directEntity = damageSource.getDirectEntity();
        EntityDamageEvent event = directEntity != null ? new EntityDamageByEntityEvent(directEntity, (LivingEntity) (Object) this, damageSource) : new EntityDamageEvent((LivingEntity) (Object) this, damageSource);
        event.dispatch();
    }
}
