package com.github.sirblobman.combatlogx.listener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.*;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.CommandConfiguration;
import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.UntagReason;

public final class ConfigurationListener extends CombatListener {
    public ConfigurationListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.NORMAL, ignoreCancelled = true)
    public void beforeTag(PlayerPreTagEvent e) {
        printDebug("Detected PlayerPreTagEvent.");

        TagReason tagReason = e.getTagReason();
        printDebug("Tag Reason: " + tagReason);
        if (isDisabled(tagReason)) {
            printDebug("Reason disabled by configuration.");
            e.cancel();
            return;
        }

        ServerPlayer player = e.getPlayer();
        printDebug("Player: " + player.getName().getString());

        if (isWorldDisabled(player)) {
            printDebug("Player is in disabled world, cancelling.");
            e.cancel();
            return;
        }

        if (checkBypass(player)) {
            printDebug("Player has bypass, cancelling.");
            e.cancel();
            return;
        }

        Entity enemy = e.getEnemy();
        if (isSelfCombatDisabled(player, enemy)) {
            printDebug("Self combat is disabled, cancelling.");
            e.cancel();
            return;
        }

        printDebug("Finished default beforeTag check without cancellation.");
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onTag(PlayerTagEvent e) {
        ServerPlayer player = e.getPlayer();
        Entity enemy = e.getEnemy();
        runTagCommands(player, enemy);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        ServerPlayer player = e.getPlayer();
        checkDeathUntag(player);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    // todo bukkit has events running from any order but types are grouped and order doesn't change after listeners registered.
    public void onDeath(PlayerDeathEvent e) {
        ServerPlayer player = e.getEntity();
        checkDeathUntag(player);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        checkEnemyDeathUntag(entity);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) return;

        checkEnemyDeathUntag(livingEntity);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ServerPlayer player = e.getPlayer();
        if (((ILogoutRules) player).clx$isFake()) return;
        checkEnemyQuitUntag(player);
    }

    private boolean checkBypass(@NotNull ServerPlayer player) {
        ICombatManager combatManager = getCombatManager();
        return combatManager.canBypass(player);
    }

    private boolean isSelfCombatDisabled(@NotNull ServerPlayer player, @Nullable Entity enemy) {
        if (getConfiguration().selfCombat) return false;

        return (enemy != null && isEqual(player, enemy));
    }

    private boolean isEqual(@NotNull Entity entity1, @NotNull Entity entity2) {
        if (entity1 == entity2) {
            return true;
        }

        UUID entityId1 = entity1.getUUID(); // todo i could just use Entity#equals which compares entity id
        UUID entityId2 = entity2.getUUID();
        return entityId1.equals(entityId2);
    }

    private void checkDeathUntag(@NotNull ServerPlayer player) {
        ICombatManager combatManager = getCombatManager();
        if (getConfiguration().untagOnDeath && combatManager.isInCombat(player)) {
            combatManager.untag(player, UntagReason.SELF_DEATH);
        }
    }

    private void checkEnemyDeathUntag(@NotNull LivingEntity enemy) {
        ICombatManager combatManager = getCombatManager();
        if (getConfiguration().untagOnEnemyDeath) {
            List<ServerPlayer> playerList = combatManager.getPlayersInCombat(Objects.requireNonNull(enemy.level().getServer()));
            for (ServerPlayer player : playerList) {
                combatManager.untag(player, enemy, UntagReason.ENEMY_DEATH);
            }
        }
    }

    private void checkEnemyQuitUntag(@NotNull ServerPlayer enemy) {
        ICombatManager combatManager = getCombatManager();
        if (getConfiguration().untagOnEnemyQuit) {
            List<ServerPlayer> playerList = combatManager.getPlayersInCombat(Objects.requireNonNull(enemy.level().getServer()));
            for (ServerPlayer player : playerList) {
                combatManager.untag(player, enemy, UntagReason.ENEMY_QUIT);
            }
        }
    }

    private void runTagCommands(@NotNull ServerPlayer player, @Nullable Entity enemy) {
        ICombatLogX mod = getCombatLogX();
        CommandConfiguration commandConfiguration = mod.getCommandConfiguration();
        List<String> tagCommandList = commandConfiguration.tagCommandList;

        List<Entity> enemyList = (enemy == null ? Collections.emptyList() : Collections.singletonList(enemy));
        IPlaceholderManager placeholderManager = mod.getPlaceholderManager();
        placeholderManager.runReplacedCommands(player, enemyList, tagCommandList);
    }

    private MainConfiguration getConfiguration() {
        ICombatLogX mod = getCombatLogX();
        return mod.getConfiguration();
    }

    private boolean isDisabled(@NotNull TagReason reason) {
        List<TagReason> tagReasons = getConfiguration().getEnabledTagReasons();
        return !tagReasons.contains(reason);
    }

    private boolean isWorldDisabled(@NotNull Entity entity) {
        Level world = entity.level();
        return isWorldDisabled(world);
    }

    private boolean isWorldDisabled(@NotNull Level world) {
        MainConfiguration configuration = getConfiguration();
        return configuration.isDisabled(world);
    }
}
