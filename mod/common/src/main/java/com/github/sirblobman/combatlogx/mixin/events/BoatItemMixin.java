package com.github.sirblobman.combatlogx.mixin.events;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity.EntityPlaceEvent;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle./*? if >=1.21.10 {*/ /*AbstractBoat*/ /*?} else {*/ Boat /*?}*/;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatItem.class)
public class BoatItemMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void firePlaceEvent(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<?> cir, // multiver
                                @Local(ordinal = 0) HitResult hitResult,
                                @Local(ordinal = 0) /*? if >=1.21.10 {*/ /*AbstractBoat*/ /*?} else {*/ Boat /*?}*/ boat) {

        if (!(hitResult instanceof BlockHitResult blockHitResult && player instanceof ServerPlayer p)) return;

        EntityPlaceEvent event = new EntityPlaceEvent(boat, p, blockHitResult.getBlockPos(), p.getDirection(), hand);
        event.dispatch();
    }
}
