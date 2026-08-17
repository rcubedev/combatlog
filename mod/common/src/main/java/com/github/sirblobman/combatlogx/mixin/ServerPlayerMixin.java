package com.github.sirblobman.combatlogx.mixin;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerLocaleChangeEvent;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//? if >=1.21.10
/*import com.llamalad7.mixinextras.sugar.Local;*/
import com.mojang.authlib.GameProfile;

import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDeathEvent;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Supplier;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ILogoutRules {

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, /*? if <1.21.10 {*/blockPos, f,/*?}*/ gameProfile);
    }

    @Unique
    private static final int al$MAX_DEATH_MESSAGE_LENGTH = 256;  // This constant is from mc source code, but it's declared locally there.

    @Shadow
    private boolean disconnected;

    @Shadow
    private String language;

    @Unique
    private boolean clx$isFake = false;

    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void getPreviousLocale(ClientInformation information, CallbackInfo ci, @Share("preLocale") LocalRef<String> preLocaleConsumer) {
        preLocaleConsumer.set(this.language);
    }

    @Inject(method = "updateOptions", at = @At("RETURN"))
    private void fireLocaleChangeEvent(ClientInformation information, CallbackInfo ci, @Share("preLocale") LocalRef<String> preLocaleSupplier) {
        String previousLocale = preLocaleSupplier.get();
        if (Objects.equals(previousLocale, this.language)) return;

        PlayerLocaleChangeEvent event = new PlayerLocaleChangeEvent((ServerPlayer) (Object) this, this.language);
        event.dispatch();
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void fireDeathEvent(DamageSource damageSource, CallbackInfo ci, @Share("deathEvent") LocalRef<PlayerDeathEvent> eventConsumer) {
        PlayerDeathEvent event = new PlayerDeathEvent((ServerPlayer) (Object) this, damageSource);
        eventConsumer.set(event);
        event.dispatch();
    }

    //old
    /*@ModifyVariable(method = "die", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"), ordinal = 0)
    private Component setDeathMessage(Component original, @Share("deathEvent") LocalRef<PlayerDeathEvent> eventSupplier) {
        PlayerDeathEvent event = eventSupplier.get();
        Component custom = event.getDeathMessage();
        return (custom != null && !CommonComponents.EMPTY.equals(custom)) ? custom : original;
    }*/

    @ModifyExpressionValue(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
    private Component setDeathMessage(Component original, @Share("deathEvent") LocalRef<PlayerDeathEvent> eventSupplier) {
        PlayerDeathEvent event = eventSupplier.get();
        Component custom = event.getDeathMessage();
        return (custom != null /*&& !CommonComponents.EMPTY.equals(custom)*/) ? custom : original; // fixme prob
    }

    /*@Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void fireRespawnEvent(boolean bl, DimensionTransition.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        PlayerRespawnEvent event = new PlayerRespawnEvent((ServerPlayer) (Object) this);
        // DimensionTransition dimensionTransition = cir.getReturnValue();
        // PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(
        //         al$self, dimensionTransition.pos(), dimensionTransition.newLevel(),
        //         dimensionTransition.yRot(), dimensionTransition.yRot());
        event.dispatch();
    }*/

    //? if >=1.21.10 {
    /*@ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketSendListener;exceptionallySend(Ljava/util/function/Supplier;)Lio/netty/channel/ChannelFutureListener;"))
    private Supplier<Packet<?>> captureFailure(Supplier<Packet<?>> packet, @Local(ordinal = 0) Component component) {
        al$saveDeathMsg(component, packet);
        return packet;
    }
    *///?} else {
    @WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
    private void die(ServerGamePacketListenerImpl instance, Packet<?> packet, PacketSendListener listener, Operation<Void> original) {
        if (!this.clx$isFake() || !(packet instanceof ClientboundPlayerCombatKillPacket combatKillPacket)) {
            original.call(instance, packet, listener);
            return;
        }

        al$saveDeathMsg(combatKillPacket.message(), listener::onFailure);
        /*UntagEventListener.DEATH_MESSAGES.put(this.getUUID(), p -> {
            p.displayClientMessage(deathMsg, false);
            p.connection.send(new ClientboundPlayerCombatKillPacket(p.getId(), deathMsg),
                    PacketSendListener.exceptionallySend(() -> {
                        Packet<?> failure = listener.onFailure();
                        if (!(failure instanceof ClientboundPlayerCombatKillPacket failKillPacket)) return null;
                        return new ClientboundPlayerCombatKillPacket(p.getId(), failKillPacket.message());
                    }));
        });*/
        original.call(instance, packet, listener);
    }
    //?}

    @WrapOperation(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void die(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original) {
        if (!this.clx$isFake() || !(packet instanceof ClientboundPlayerCombatKillPacket combatKillPacket)) {
            original.call(instance, packet);
            return;
        }

        al$saveDeathMsg(combatKillPacket.message(), null);
        /*UntagEventListener.DEATH_MESSAGES.put(this.getUUID(), p -> {
            p.connection.send(new ClientboundPlayerCombatKillPacket(p.getId(), combatKillPacket.message()));
        });*/
        original.call(instance, packet);
    }

    //fixme prob move out
    @Unique
    private void al$saveDeathMsg(Component deathMsg, @Nullable Supplier<@Nullable Packet<?>> listener) {
        ICombatLogX instance = CombatLogX.INSTANCE;
        if (instance == null) return;

        PlayerData data = instance.getPlayerDataManager().get((ServerPlayer) (Object) this);
        data.transform(tag -> {
            CompoundTag msgTag = new CompoundTag();

            DataResult<Tag> serializingNormal = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, deathMsg);
            Tag serializedNormal = serializingNormal.resultOrPartial(CombatLogX.LOGGER::error).orElseThrow();
            msgTag.put("primary", serializedNormal);

            if (listener != null) {
                Packet<?> failure = listener.get();
                if (failure instanceof ClientboundPlayerCombatKillPacket failKillPacket) {
                    DataResult<Tag> serializingFallback = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, failKillPacket.message());
                    Tag serializedFallback = serializingFallback.resultOrPartial(CombatLogX.LOGGER::error).orElseThrow();
                    msgTag.put("fallback", serializedFallback);
                }
            }

            tag.put("offlineDeath", msgTag);
        });
    }

    /**
     * Saves death message for later if player is fake.
     */
    /*@Inject(method = "die", at = @At("RETURN"))
    private void constructor(DamageSource damageSource, CallbackInfo ci) {
        if (this.al$isFake()) {
            boolean seeDeathMsgs = this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);

            Component deathMsg;
            if (seeDeathMsgs) {
                //fixme this wont use the custom msg
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
    }*/

    @Inject(method = "disconnect", at = @At("RETURN"))
    private void disconnect(CallbackInfo ci) {
        UntagEventListener.DISCONNECTED.remove((ServerPlayer) (Object) this);
    }

    @ModifyReturnValue(method = "hasDisconnected", at = @At("RETURN"))
    public boolean hasDisconnected(boolean original) {
        if (!original) return false;

        ICombatLogX instance = CombatLogX.INSTANCE;
        return instance == null || !instance.getCombatManager().isInCombat((ServerPlayer) (Object) this); // todo?
    }

    @WrapOperation(
            method = "doTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void stopQueuingPackets(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original) {
        if (!this.clx$isFake()) original.call(instance, packet);
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void fireTasks(CallbackInfo ci) {
        ModdedTaskScheduler.getScheduler().fireTasks(TaskType.START_SERVER_PLAYER_TICK, TickContext.ofPlayer((ServerPlayer) (Object) this));
    }

    @Inject(method = "doTick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ModdedTaskScheduler.getScheduler().fireTasks(TaskType.END_SERVER_PLAYER_TICK, TickContext.ofPlayer((ServerPlayer) (Object) this));
    }

    @Override
    public boolean clx$isFake() {
        return this.clx$isFake;
    }

    @Override
    public void clx$setFake() {
        this.clx$isFake = true;
        this.disconnected = true;
    }
}
