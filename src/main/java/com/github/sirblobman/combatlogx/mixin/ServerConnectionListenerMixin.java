package com.github.sirblobman.combatlogx.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConnectionListener.class)
public class ServerConnectionListenerMixin {

    /**
     * Ticks all the players that are in {@link UntagEventListener#DISCONNECTED}
     *
     * @param ci
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickConnections(CallbackInfo ci) {
        // Tick "disconnected" players as well
        UntagEventListener.DISCONNECTED.forEach(ServerPlayer::doTick);
    }
}
