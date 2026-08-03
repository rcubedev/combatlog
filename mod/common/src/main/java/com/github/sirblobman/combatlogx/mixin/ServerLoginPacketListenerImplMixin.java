package com.github.sirblobman.combatlogx.mixin;

import com.github.sirblobman.combatlogx.VersionUtil;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
//? if >=1.21.10
/*import net.minecraft.server.players.NameAndId;*/
import net.minecraft.server.players.PlayerList;

import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.net.SocketAddress;import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {

    @WrapOperation(
            method = "verifyLoginAndFinishConnectionSetup",
            at = @At(value = "INVOKE", target =
                    //? if >=1.21.10 {
                    /*"Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"
                    *//*?} else {*/ "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;" /*?}*/
            )
    )
    private @Nullable Component skipNpcDisconnect(PlayerList instance, SocketAddress address, /*? if >=1.21.10 {*/ /*NameAndId *//*?} else {*/ GameProfile /*?}*/ nameAndId, Operation<@Nullable Component> original, @Share("hasNpc") LocalBooleanRef hasNpc) {
        boolean npc = instance.getPlayers().stream().anyMatch(p -> p.getUUID().equals(VersionUtil.getUUID(p.getGameProfile()))
                && UntagEventListener.DISCONNECTED.contains(p));
        hasNpc.set(npc);
        return npc == true ? null : original.call(instance, address, nameAndId);
    }

    @WrapOperation(
        method = "verifyLoginAndFinishConnectionSetup",
        at = @At(
            value = "INVOKE",
            target = /*? if >=1.21.10 {*/ /*"Lnet/minecraft/server/players/PlayerList;disconnectAllPlayersWithProfile(Ljava/util/UUID;)Z"
                    *//*?} else {*/ "Lnet/minecraft/server/players/PlayerList;disconnectAllPlayersWithProfile(Lcom/mojang/authlib/GameProfile;)Z" /*?}*/
        )
    )
    private boolean skipNpcDisconnect(PlayerList instance, /*? if >=1.21.10 {*/ /*UUID *//*?} else {*/ GameProfile /*?}*/ id, Operation<Boolean> original, @Share("hasNpc") LocalBooleanRef hasNpc) {
        return !hasNpc.get() && original.call(instance, id);
    }
}
