package com.github.sirblobman.combatlogx.mixin;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;

import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {

    @WrapOperation(
        method = "verifyLoginAndFinishConnectionSetup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;disconnectAllPlayersWithProfile(Lcom/mojang/authlib/GameProfile;)Z"
        )
    )
    private boolean skipNpcDisconnect(PlayerList instance, GameProfile gameProfile, Operation<Boolean> original) {
        boolean hasNpc = instance.getPlayers().stream().anyMatch(p -> p.getUUID().equals(gameProfile.getId())
                        && UntagEventListener.DISCONNECTED.contains(p));

        return !hasNpc && original.call(instance, gameProfile);
    }}