package com.github.sirblobman.combatlogx.mixin.events;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity.EntityPlaceEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalItem.class)
public class EndCrystalItemMixin {

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void firePlaceEvent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir,
                                @Local(ordinal = 0) EndCrystal crystal) {

        EntityPlaceEvent event = new EntityPlaceEvent(crystal, context);
        event.dispatch();
    }
}
