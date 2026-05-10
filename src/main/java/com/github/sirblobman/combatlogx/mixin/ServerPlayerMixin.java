package com.github.sirblobman.combatlogx.mixin;

import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.FabricTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDeathEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerRespawnEvent;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ILogoutRules {

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Unique
    private static final int al$MAX_DEATH_MESSAGE_LENGTH = 256;  // This constant is from mc source code, but it's declared locally there.

    @Shadow
    private boolean disconnected;

    @Inject(method = "die", at = @At("HEAD"))
    private void fireDeathEvent(DamageSource damageSource, CallbackInfo ci, @Share("deathEvent") LocalRef<PlayerDeathEvent> eventConsumer) {
        PlayerDeathEvent event = new PlayerDeathEvent((ServerPlayer) (Object) this, damageSource);
        eventConsumer.set(event);
        event.dispatch();
    }

    @ModifyVariable(method = "die", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"), ordinal = 0)
    private Component setDeathMessage(Component original, @Share("deathEvent") LocalRef<PlayerDeathEvent> eventSupplier) {
        PlayerDeathEvent event = eventSupplier.get();
        Component custom = event.getDeathMessage();
        return (custom != null && !CommonComponents.EMPTY.equals(custom)) ? custom : original;
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void fireRespawnEvent(boolean bl, DimensionTransition.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        PlayerRespawnEvent event = new PlayerRespawnEvent((ServerPlayer) (Object) this);
        // DimensionTransition dimensionTransition = cir.getReturnValue();
        // PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(
        //         al$self, dimensionTransition.pos(), dimensionTransition.newLevel(),
        //         dimensionTransition.yRot(), dimensionTransition.yRot());
        event.dispatch();
    }

    /**
     * Saves death message for later if player is fake.
     */
    @Inject(method = "die", at = @At("RETURN"))
    private void constructor(DamageSource damageSource, CallbackInfo ci) {
        if (this.al$isFake()) {
            boolean seeDeathMsgs = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);

            Component deathMsg;
            if (seeDeathMsgs) {
                deathMsg = this.getCombatTracker().getDeathMessage();

                if (deathMsg.getString().length() > al$MAX_DEATH_MESSAGE_LENGTH) {
                    String string = deathMsg.getString(256);
                    var attackTooLongMsg = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
                    deathMsg = Component.translatable("death.attack.even_more_magic", this.getDisplayName()).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, attackTooLongMsg)));
                }
            } else {
                deathMsg = CommonComponents.EMPTY;
            }

            // Player won't see death message, we must save it for later (issue #1)
            UntagEventListener.SKIPPED_DEATH_MESSAGES.put(this.getUUID(), deathMsg);
        }
    }

    @Inject(method = "disconnect", at = @At("RETURN"))
    private void disconnect(CallbackInfo ci) {
        UntagEventListener.DISCONNECTED.remove((ServerPlayer) (Object) this);
    }

    @ModifyReturnValue(method = "hasDisconnected", at = @At("RETURN"))
    public boolean hasDisconnected(boolean original) {
        return original && !CombatLogX.INSTANCE.getCombatManager().isInCombat((ServerPlayer) (Object) this); // todo?
    }

    @WrapOperation(
            method = "doTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void stopQueuingPackets(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original) {
        if (!this.al$isFake()) original.call(instance, packet);
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void fireTasks(CallbackInfo ci) {
        FabricTaskScheduler.getScheduler().fireTasks(TaskType.START_SERVER_PLAYER_TICK, TickContext.ofPlayer((ServerPlayer) (Object) this));
    }

    @Inject(method = "doTick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        FabricTaskScheduler.getScheduler().fireTasks(TaskType.END_SERVER_PLAYER_TICK, TickContext.ofPlayer((ServerPlayer) (Object) this));
    }

    @Override
    public boolean al$isFake() {
        return this.disconnected;
    }

    @Override
    public void al$setDisconnected() {
        this.disconnected = true;
    }
}
