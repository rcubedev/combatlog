package org.samo_lego.antilogout.mixin;

import java.util.function.LongConsumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import org.samo_lego.antilogout.AntiLogout;
import org.samo_lego.antilogout.datatracker.ILogoutRules;
import org.samo_lego.antilogout.event.EventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Implements {@link ILogoutRules} for {@link ServerPlayer}.
 */
@Mixin(ServerPlayer.class)
public abstract class CLogoutRulesPlayer implements ILogoutRules {
    @Shadow
    private boolean disconnected;

    @Unique
    private long al$allowDisconnectTime = 0;

    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Unique
    private boolean al$executedDisconnect = false;
    @Unique
    private Runnable al$delayedTask;
    @Unique
    private LongConsumer al$inCombatTask;
    @Unique
    private long al$taskTime;
    @Unique
    private long al$finalTickTime;

    @Override
    public boolean al$allowDisconnect() {
        return this.al$allowDisconnectTime != -1 && this.al$allowDisconnectTime <= System.currentTimeMillis() && !AntiLogout.config.disableAllLogouts;
    }

    @Override
    public void al$setAllowDisconnectAt(long systemTime) {
        this.al$allowDisconnectTime = systemTime;
    }

    @Override
    public void al$setAllowDisconnect(boolean allow) {
        this.al$allowDisconnectTime = allow ? 0 : -1;
    }

    @Override
    public boolean al$isFake() {
        return this.disconnected;
    }

    @Override
    public void al$onRealDisconnect() {
        this.disconnected = true;

        if (!this.al$allowDisconnect()) {
            DISCONNECTED_PLAYERS.add((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "hasDisconnected", at = @At("HEAD"), cancellable = true)
    public void hasDisconnected(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.al$allowDisconnect() && this.disconnected);
    }

    @Inject(method = "doTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"),
            cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (this.al$isFake()) {
            if (this.al$allowDisconnect() && !this.al$executedDisconnect) {
                this.connection.disconnect(Component.empty());
                this.al$executedDisconnect = true;  // Prevent disconnecting twice
            }
            ci.cancel();
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (this.al$taskTime <= currentTimeMillis && this.al$delayedTask != null) {
            this.al$delayedTask.run();
            this.al$delayedTask = null;
        } if (this.al$inCombatTask != null) {
            if (this.al$finalTickTime >= currentTimeMillis && !(this.al$taskTime <= currentTimeMillis))
                    this.al$inCombatTask.accept(currentTimeMillis);
            else this.al$inCombatTask = null;
        }
    }


    @Inject(method = "disconnect", at = @At("TAIL"))
    private void al$disconnect(CallbackInfo ci) {
        DISCONNECTED_PLAYERS.remove((ServerPlayer) (Object) this);
    }

    @Inject(method = "hurt", at = @At("TAIL"))
    private void onHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        EventHandler.onHurt((ServerPlayer) (Object) this, damageSource);
    }

    @Override
    public void al$delay(long tickDuration, Runnable task) {
        this.al$delayedTask = task;
        this.al$taskTime = tickDuration;
    }

    @Override
    public void al$tickInCombat(long finalTick, LongConsumer task) {
        this.al$inCombatTask = task;
        this.al$finalTickTime = finalTick;
    }
}
