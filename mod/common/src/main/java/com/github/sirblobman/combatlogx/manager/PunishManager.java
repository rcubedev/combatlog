package com.github.sirblobman.combatlogx.manager;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.CommandConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.event.PlayerPunishEvent;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.object.KillTime;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import org.jetbrains.annotations.NotNull;

public final class PunishManager extends Manager implements IPunishManager {
    public PunishManager(@NotNull ICombatLogX plugin) {
        super(plugin);
    }

    @Override
    public boolean punish(@NotNull ServerPlayer player, @NotNull UntagReason punishReason, @NotNull List<Entity> enemyList) {
        System.out.println("PunishManager#punish called");
        PlayerPunishEvent punishEvent = new PlayerPunishEvent(player, punishReason, enemyList);
        punishEvent.dispatch();

        if (punishEvent.isCancelled()) return false;

        increasePunishmentCount(player);
        System.out.println("Running kill check");
        runKillCheck(player, enemyList);

        ICombatLogX plugin = getCombatLogX();
        CommandConfiguration commandConfiguration = plugin.getCommandConfiguration();

        List<String> punishCommandList = commandConfiguration.punishCommandList;
        if (!punishCommandList.isEmpty()) {
            runPunishCommands(player, enemyList, punishCommandList);
        }

        return true;
    }

    @Override
    public long getPunishmentCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        PunishConfiguration punishConfiguration = combatLogX.getPunishConfiguration();

        if (punishConfiguration.enablePunishmentCounter) {
            PlayerDataManager playerDataManager = getPlayerDataManager();
            PlayerData playerData = playerDataManager.getIfPresent(player);

            if (playerData == null) return 0L;
            return playerData.getData().getLong("punishmentCount")/*? if >=1.21.10 {*/ /*.orElse(0L) *//*?}*/;
        }

        return 0L;
    }

    private void increasePunishmentCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        PunishConfiguration punishConfiguration = combatLogX.getPunishConfiguration();
        if (!punishConfiguration.enablePunishmentCounter) return;

        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData playerData = playerDataManager.get(player);

        long currentCount = playerData.getData().getLong("punishmentCount")/*? if >=1.21.10 {*/ /*.orElse(0L) *//*?}*/;
        playerData.transform(tag -> tag.putLong("punishmentCount", currentCount + 1L));

        // playerDataManager.save(player);
    }

    @Override
    public void resetPunishmentCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        PunishConfiguration punishConfiguration = combatLogX.getPunishConfiguration();
        if (!punishConfiguration.enablePunishmentCounter) return;

        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData playerData = playerDataManager.get(player);

        playerData.transform(tag -> tag.putLong("punishmentCount", 0L));

        // playerDataManager.save(player);
    }

    private void runKillCheck(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList) {
        ICombatLogX combatLogX = getCombatLogX();
        PunishConfiguration punishConfiguration = combatLogX.getPunishConfiguration();
        KillTime killTime = punishConfiguration.killTime;

        switch (killTime) {
            case JOIN:
                System.out.println("KillTime was join");
                killOnJoin(player);
                break;
            case QUIT:
                System.out.println("KillTime was quit");
                killOnQuit(player, enemyList);
                break;
            default:
                break;
        }
    }

    private void killOnJoin(@NotNull ServerPlayer player) {
        ICombatLogX plugin = getCombatLogX();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.get(player);

        playerData.transform(tag -> tag.putBoolean("killOnJoin", true));
        // System.out.println("Set killOnJoin. New value: " + playerData.getData().getBoolean("killOnJoin").orElse(false));

        // playerDataManager.save(player);
    }

    private void killOnQuit(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList) {
        ICombatLogX plugin = getCombatLogX();
        IDeathManager deathManager = plugin.getDeathManager();
        deathManager.kill(player, enemyList);
    }

    private void runPunishCommands(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                   @NotNull List<String> punishCommandList) {
        if (punishCommandList.isEmpty()) return;

        ICombatLogX plugin = getCombatLogX();
        IPlaceholderManager placeholderManager = plugin.getPlaceholderManager();
        placeholderManager.runReplacedCommands(player, enemyList, punishCommandList);
    }
}
