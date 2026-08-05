package com.github.sirblobman.combatlogx.mixin;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerNPCReplaceEvent;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.MinecraftServer;
//? if <1.21.10
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.server./*? if >=1.21.10 {*/ /*network.config.PrepareSpawnTask *//*?} else {*/ players.PlayerList /*?}*/;

import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {

    public ServerConfigurationPacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow
    protected abstract @NotNull GameProfile playerProfile();

    //? if >=1.21.10 {
    /*@Shadow
    private @Nullable PrepareSpawnTask prepareSpawnTask;

    @Unique
    private @Nullable ServerPlayer clx$npc;

    @WrapOperation(
            method = "startNextTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ConfigurationTask;start(Ljava/util/function/Consumer;)V"
            )
    )
    private void kickNPC(ConfigurationTask instance, Consumer<Packet<?>> task, Operation<Void> original) {

        if (instance == prepareSpawnTask) {
            UUID uuid = playerProfile().id();
            clx$npc = UntagEventListener.DISCONNECTED.stream().filter(p -> p.getUUID().equals(uuid)).findFirst().orElse(null);
            clx$kickNPC(clx$npc);
        }

        original.call(instance, task);
    }

    @Inject(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void setNPC(ServerboundFinishConfigurationPacket packet, CallbackInfo ci, @Share("npc") LocalRef<@Nullable ServerPlayer> npcSetter) {
        npcSetter.set(clx$npc);
        clx$npc = null;
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void clearNPC(DisconnectionDetails details, CallbackInfo ci) {
        clx$npc = null;
    }

    *///?} else if <1.21.10 {
    @ModifyExpressionValue(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayer(Ljava/util/UUID;)Lnet/minecraft/server/level/ServerPlayer;"
            )
    )
    private ServerPlayer kickNPC(ServerPlayer original, @Share("npc") LocalRef<@Nullable ServerPlayer> npcSetter) {
        UUID uuid = playerProfile().getId();
        if (original != null && UntagEventListener.DISCONNECTED.contains(original)) {
            ServerPlayer npc = UntagEventListener.DISCONNECTED.stream().filter(p -> p.getUUID().equals(original.getUUID())).findFirst().orElseThrow();
            npcSetter.set(npc);
            clx$kickNPC(npc);
            return null;
        }
        return original;
    }
    //?}

    @WrapOperation(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = /*? if >=1.21.10 {*/ /*"Lnet/minecraft/server/network/config/PrepareSpawnTask;spawnPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/level/ServerPlayer;"
                    *//*?} else {*/ "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;" /*?}*/
            )
    )
    private ServerPlayer fireNPCReplaceEvent(
            /*? if >=1.21.10 {*/ /*PrepareSpawnTask instance, Connection connection, CommonListenerCookie cookie
            *//*?} else {*/ PlayerList instance, GameProfile gameProfile, ClientInformation clientInformation /*?}*/,
            Operation<ServerPlayer> original, @Share("npc") LocalRef<@Nullable ServerPlayer> npcOpt) {

        ServerPlayer newPlayer = original.call(instance, /*? if >=1.21.10 {*/ /*connection, cookie *//*?} else {*/ gameProfile, clientInformation /*?}*/);
        ServerPlayer npc = npcOpt.get();
        if (npc != null) {
            PlayerNPCReplaceEvent event = new PlayerNPCReplaceEvent(npc, newPlayer);
            event.dispatch();
        }
        return newPlayer;
    }

    @Unique
    private void clx$kickNPC(@Nullable ServerPlayer npc) {
        if (npc == null) return;
        npc.connection.onDisconnect(new DisconnectionDetails(Component.literal("TODO"))); // fixme
        UntagEventListener.DISCONNECTED.remove(npc);
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