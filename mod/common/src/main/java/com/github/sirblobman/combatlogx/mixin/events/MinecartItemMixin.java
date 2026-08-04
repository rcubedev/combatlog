package com.github.sirblobman.combatlogx.mixin.events;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity.EntityPlaceEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartItem.class)
public class MinecartItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void firePlaceEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                @Local(ordinal = 0) AbstractMinecart cart) {

        EntityPlaceEvent event = new EntityPlaceEvent(cart, context);
        event.dispatch();
    }
}
