package com.github.sirblobman.combatlogx.mixin;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.PrepareSpawnTask;import net.minecraft.server.players.PlayerList;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerNPCReplaceEvent;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;import java.util.function.Supplier;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplLoginMixin extends ServerCommonPacketListenerImpl {

    public ServerConfigurationPacketListenerImplLoginMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow
    protected abstract @NotNull GameProfile playerProfile();

    @ModifyExpressionValue(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayer(Ljava/util/UUID;)Lnet/minecraft/server/level/ServerPlayer;"
            )
    )
    private ServerPlayer skipDuplicateCheckForNpc(ServerPlayer original, @Share("npc") LocalRef<@Nullable ServerPlayer> npcSetter) {
        if (original != null && UntagEventListener.DISCONNECTED.contains(original)) {
            ServerPlayer optNPC = UntagEventListener.DISCONNECTED.stream().filter(p -> p.getUUID().equals(original.getUUID())).findFirst().orElseThrow();
            npcSetter.set(optNPC);
            return null;
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = /*? if >=1.21.10 {*/ "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"
                    /*?} else {*/ /*"Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"*/ /*?}*/
            )
    )
    private Component bypassServerFull(@Nullable Component original, @Share("npc") LocalRef<@Nullable ServerPlayer> npc) {
        if (original != null && original.equals(Component.translatable("multiplayer.disconnect.server_full"))) {
            if (npc.get() != null) return null;
        }
        return original;
    }

    //fixme inject before PrepareSpawnTask#spawnPlayer
    @WrapOperation(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = /*? if >=1.21.10 {*/ "Lnet/minecraft/server/network/config/PrepareSpawnTask;spawnPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/level/ServerPlayer;"
                    /*?} else {*/ /*"Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"*/ /*?}*/
            )
    )
    private ServerPlayer kickNPC(
            /*? if >=1.21.10 {*/ PrepareSpawnTask instance, Connection connection, CommonListenerCookie cookie
            /*?} else {*/ /*PlayerList instance, GameProfile gameProfile, ClientInformation clientInformation *//*?}*/,
            Operation<ServerPlayer> original, @Share("npc") LocalRef<@Nullable ServerPlayer> npcOpt) {

        return kickNPC(npcOpt.get(), () -> original.call(instance, /*? if >=1.21.10 {*/ connection, cookie /*?} else {*/ /*gameProfile, clientInformation *//*?}*/));
    }

    private ServerPlayer kickNPC(@Nullable ServerPlayer npc, Supplier<ServerPlayer> original) {
        if (npc == null) return original.get();

        this.server.getPlayerList().remove(npc);
        UntagEventListener.DISCONNECTED.remove(npc);
        ServerPlayer newPlayer = original.get();

        PlayerNPCReplaceEvent event = new PlayerNPCReplaceEvent(npc, newPlayer);
        event.dispatch();
        return newPlayer;
    }

    // @WrapOperation(
    //         method = "handleConfigurationFinished",
    //         at = @At(
    //                 value = "INVOKE",
    //                 target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
    //         )
    // )
    // private ServerPlayer hijackPlayer(PlayerList instance, GameProfile gameProfile, ClientInformation clientInformation, Operation<ServerPlayer> original, @Share("npc") LocalRef<ServerPlayer> npcOpt) {
    //     ServerPlayer npc = npcOpt.get();
    //     return npc != null ? npc : original.call(instance, gameProfile, clientInformation);
    // }
}