package com.github.sirblobman.combatlogx.mixin.events;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity.EntityPlaceEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandItem.class)
public class ArmorStandItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void firePlaceEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                @Local(ordinal = 0) ArmorStand stand) {

        EntityPlaceEvent event = new EntityPlaceEvent(stand, context);
        event.dispatch();
    }
}
