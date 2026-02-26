package com.github.sirblobman.combatlogx.mixin;

import java.util.function.LongConsumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ILogoutRules {

    private static final int MAX_DEATH_MESSAGE_LENGTH = 256;  // This constant is from mc source code, but it's declared locally there.

    @Unique
    private long al$allowDisconnectTime = 0;

    @Unique
    private Runnable al$delayedTask;

    @Unique
    private boolean al$executedDisconnect = false;

    @Unique
    private long al$finalTickTime;

    @Unique
    private LongConsumer al$inCombatTask;

    @Unique
    private long al$taskTime;

    @Unique
    private final ServerPlayer al$self = (ServerPlayer) (Object) this;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    private boolean disconnected;

    @Shadow
    public abstract ServerLevel serverLevel();

    /**
     * Saves death message for later if player is fake.
     */
    @Inject(method = "die", at = @At("RETURN"))
    private void constructor(DamageSource damageSource, CallbackInfo ci) {
        if (this.al$isFake()) {
            boolean seeDeathMsgs = this.serverLevel().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);

            Component deathMsg;
            if (seeDeathMsgs) {
                deathMsg = al$self.getCombatTracker().getDeathMessage();

                if (deathMsg.getString().length() > MAX_DEATH_MESSAGE_LENGTH) {
                    String string = deathMsg.getString(256);
                    var attackTooLongMsg = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
                    deathMsg = Component.translatable("death.attack.even_more_magic", al$self.getDisplayName()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, attackTooLongMsg)));
                }
            } else {
                deathMsg = CommonComponents.EMPTY;
            }

            // Player won't see death message, we must save it for later (issue #1)
            ILogoutRules.SKIPPED_DEATH_MESSAGES.put(al$self.getUUID(), deathMsg);
        }
    }

    @Inject(method = "disconnect", at = @At("TAIL"))
    private void disconnect(CallbackInfo ci) {
        DISCONNECTED_PLAYERS.remove(al$self);
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

    @Override
    public boolean al$allowDisconnect() {
        return this.al$allowDisconnectTime != -1 && this.al$allowDisconnectTime <= System.currentTimeMillis() && !CombatLogX.config.disableAllLogouts;
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
            DISCONNECTED_PLAYERS.add(al$self);
        }
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
