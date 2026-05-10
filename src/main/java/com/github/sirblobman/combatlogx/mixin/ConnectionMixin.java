package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDisconnectEvent;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow
    public abstract PacketListener getPacketListener();

    @Shadow
    private boolean disconnectionHandled;

    @Shadow
    private Channel channel;

    /**
     * Called when a player disconnects<br>
     * (kicked or presses disconnect)
     *
     * @param ci the callback info for this method
     */
    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V"), cancellable = true)
    private void handleDisconnection(CallbackInfo ci, @Local(ordinal = 0) DisconnectionDetails disconnectionDetails) {
        if (!(this.getPacketListener() instanceof ServerGamePacketListenerImpl packetListener)) return;
        PlayerDisconnectEvent event = new PlayerDisconnectEvent(packetListener.getPlayer(), packetListener, disconnectionDetails.reason());
        event.dispatch();
        CombatLogX.LOGGER.info("Fired PlayerDisconnectEvent, cancelled: {}. Channel open: {}, active: {}", event.isCancelled(), this.channel.isOpen(), this.channel.isActive());
        if (event.isCancelled()) {
            this.channel.close(); // the channel should already be closed
            this.disconnectionHandled = false;
            ci.cancel();
        }
    }
}
