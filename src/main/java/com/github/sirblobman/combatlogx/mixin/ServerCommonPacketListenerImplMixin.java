package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("TAIL"))
    private void disconnect(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        if (((Object) this) instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
            if (((ILogoutRules) serverGamePacketListener.player).al$isFake()) {
                serverGamePacketListener.onDisconnect(disconnectionDetails);
            }
        }
    }
}
