package com.github.sirblobman.combatlogx.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

/**
 * Kicks same players that are in {@link UntagEventListener#DISCONNECTED} list
 * when player with same UUID joins.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    // swapped back to replacing NPC instead of hijacking.
    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void captureIsNpc(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie cookie, CallbackInfo ci, @Share("isNpc") LocalBooleanRef isNpc) {
        isNpc.set(UntagEventListener.DISCONNECTED.contains(serverPlayer));
        CombatLogX.LOGGER.info("Is NPC: {}", isNpc.get());
    }

    // // todo in future we can hijack this & instead send our own join msg
    // @WrapOperation(
    //         method = "placeNewPlayer",
    //         at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V")
    // )
    // private void skipJoinBroadcast(PlayerList instance, Component component, boolean overlay, Operation<Void> original, @Share("isNpc") LocalBooleanRef isNpc) {
    //     if (isNpc.get()) return;
    //     original.call(instance, component, overlay);
    // }

    // fixme why is this done? do we inject to put it later? commented out now/.
    // @WrapOperation(
    //         method = "placeNewPlayer",
    //         at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    // )
    // private Object skipPutByUUID(Map<UUID, ServerPlayer> instance, Object uuid, Object player, Operation<Object> original, @Share("isNpc") LocalBooleanRef isNpc) {
    //     if (isNpc.get()) return instance.get(uuid);
    //     return original.call(instance, uuid, player);
    // }
}
