package com.github.sirblobman.combatlogx.listener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.rcubedev.example.platform.IAdventure;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.RunnableTaskDetails;
import com.github.rcubedev.utils.event.api.Priority;
import com.github.rcubedev.utils.event.api.annotation.SubscribeEvent;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDeathEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerJoinEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerQuitEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerRespawnEvent;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.KillTime;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;

public final class DeathListener extends CombatListener {
    public DeathListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        ServerPlayer player = e.getPlayer();

        /*Consumer<ServerPlayer> deathMsg = UntagEventListener.DEATH_MESSAGES.remove(player.getUUID());
        if (deathMsg != null) {
            deathMsg.accept(player);
        }*/
        sendOfflineDeathMessage(player);

        PunishConfiguration punishConfiguration = getPunishConfiguration();
        KillTime killTime = punishConfiguration.killTime;
        if (killTime != KillTime.JOIN) return;

        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData playerData = playerDataManager.getIfPresent(player);
        if (playerData == null || !playerData.getData().getBoolean("killOnJoin")/*? if >=1.21.10 {*/ /*.orElse(false) *//*?}*/) return;

        playerData.transform(tag -> tag.putBoolean("killOnJoin", false));
        // playerDataManager.save(player);

        IDeathManager deathManager = getDeathManager();
        List<Entity> enemyList = Collections.emptyList();
        deathManager.kill(player, enemyList);
    }

    private void sendOfflineDeathMessage(ServerPlayer player) {
        PlayerData data = getPlayerDataManager().getIfPresent(player);
        if (data == null) return;

        CompoundTag offlineDeath = data.getData().getCompound("offlineDeath")/*? if >=1.21.10 {*/ /*.orElse(new CompoundTag()) *//*?}*/;;
        if (offlineDeath.isEmpty()) return;

        data.transform(tag -> tag.remove("offlineDeath"));
        Tag deathMsgSerialized = offlineDeath.get("primary");
        Tag fallbackSerialized = offlineDeath.get("fallback");
        if (deathMsgSerialized == null) return;

        net.minecraft.network.chat.Component deathMsg = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, deathMsgSerialized).resultOrPartial(CombatLogX.LOGGER::error).orElseThrow();

        player.displayClientMessage(deathMsg, false);
        if (fallbackSerialized == null) {
            player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), deathMsg));
            return;
        }

        player.connection.send(new ClientboundPlayerCombatKillPacket(player.getId(), deathMsg),
                PacketSendListener.exceptionallySend(() -> {
                    net.minecraft.network.chat.Component fallback = ComponentSerialization.CODEC
                            .parse(NbtOps.INSTANCE, fallbackSerialized)
                            .resultOrPartial(CombatLogX.LOGGER::error)
                            .orElseThrow();
                    return new ClientboundPlayerCombatKillPacket(player.getId(), fallback);
                })
        );
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        ServerPlayer player = e.getPlayer();
        IDeathManager deathManager = getDeathManager();
        deathManager.stopTracking(player);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ServerPlayer player = e.getPlayer();
        ICombatLogX mod = getCombatLogX();
        IDeathManager deathManager = getDeathManager();

        // todo CLX tasks run for next tick, do we want that?
        RunnableTaskDetails task = new RunnableTaskDetails(mod, TaskType.START_TICK, 0, () -> deathManager.stopTracking(player));
        TaskScheduler scheduler = getCombatLogX().getScheduler();
        scheduler.schedule(task);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        ServerPlayer player = e.getEntity();
        ICombatLogX mod = getCombatLogX();
        IDeathManager deathManager = mod.getDeathManager();
        if (!deathManager.wasPunishKilled(player)) return;

        List<Entity> enemyList = deathManager.getTrackedEnemies(player);
        String randomMessage = getRandomDeathMessage();
        if (randomMessage == null) return;

        IPlaceholderManager placeholderManager = mod.getPlaceholderManager();
        Component replacedMessage = placeholderManager.replaceAll(player, enemyList, randomMessage);

        e.setDeathMessage(IAdventure.getInstance().asNative(replacedMessage));
    }

    private PunishConfiguration getPunishConfiguration() {
        ICombatLogX mod = getCombatLogX();
        return mod.getPunishConfiguration();
    }

    private String getRandomDeathMessage() {
        PunishConfiguration punishConfiguration = getPunishConfiguration();
        List<String> customDeathMessageList = punishConfiguration.customDeathMessageList;
        if (customDeathMessageList.isEmpty()) return null;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int customDeathMessageListSize = customDeathMessageList.size();
        int customDeathMessageIndex = random.nextInt(customDeathMessageListSize);
        return customDeathMessageList.get(customDeathMessageIndex);
    }
}
