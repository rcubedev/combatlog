package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("TAIL"))
    private void fireKickEvent(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        // CombatLogX.LOGGER.info("Disconnect invoked. DisconnectionDetails: {}", disconnectionDetails);
        if ((Object) this instanceof ServerPlayerConnection serverPlayerConnection) { // could use ServerGamePacketListenerImpl
            PlayerKickEvent event = new PlayerKickEvent(serverPlayerConnection.getPlayer(), disconnectionDetails.reason());
            event.dispatch();
        }
    }

    // // fixme i dont think this is needed; removed and seems to work fine
    // @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("TAIL"))
    // private void disconnect(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
    //     if (((Object) this) instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
    //         if (((ILogoutRules) serverGamePacketListener.player).al$isFake()) {
    //             serverGamePacketListener.onDisconnect(disconnectionDetails);
    //         }
    //     }
    // }
}
