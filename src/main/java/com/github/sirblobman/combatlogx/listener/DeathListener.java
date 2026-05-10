package com.github.sirblobman.combatlogx.listener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.RunnableTaskDetails;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDeathEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerJoinEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerQuitEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerRespawnEvent;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.KillTime;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;


import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;

// fixme readd KillTime.
public final class DeathListener extends CombatListener {
    public DeathListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.NORMAL, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        PunishConfiguration punishConfiguration = getPunishConfiguration();
        KillTime killTime = punishConfiguration.killTime;
        if (killTime != KillTime.JOIN) return;

        ServerPlayer player = e.getPlayer();
        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData playerData = playerDataManager.getIfPresent(player);
        if (playerData == null || !playerData.getData().getBoolean("killOnJoin")) return;

        playerData.transform(tag -> tag.putBoolean("killOnJoin", false));
        // playerDataManager.save(player);

        IDeathManager deathManager = getDeathManager();
        List<Entity> enemyList = Collections.emptyList();
        deathManager.kill(player, enemyList);
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

        e.setDeathMessage(CombatLogX.createAudiences(player).toNative(replacedMessage));
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
