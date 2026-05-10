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
import net.minecraft.server.players.PlayerList;

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
import org.spongepowered.asm.mixin.injection.At;

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
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component bypassServerFull(@Nullable Component original, @Share("npc") LocalRef<@Nullable ServerPlayer> npc) {
        if (original != null && original.equals(Component.translatable("multiplayer.disconnect.server_full"))) {
            if (npc.get() != null) return null;
        }
        return original;
    }

    @WrapOperation(
            method = "handleConfigurationFinished",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
            )
    )
    private ServerPlayer kickNPC(PlayerList instance, GameProfile gameProfile, ClientInformation clientInformation, Operation<ServerPlayer> original, @Share("npc") LocalRef<@Nullable ServerPlayer> npcOpt) {
        ServerPlayer npc = npcOpt.get();
        if (npc == null) return original.call(instance, gameProfile, clientInformation);

        this.server.getPlayerList().remove(npc); // fires quit event
        UntagEventListener.DISCONNECTED.remove(npc);
        ServerPlayer newPlayer = original.call(instance, gameProfile, clientInformation);

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