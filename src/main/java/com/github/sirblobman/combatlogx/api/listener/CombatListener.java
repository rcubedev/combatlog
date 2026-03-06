package com.github.sirblobman.combatlogx.api.listener;

import java.util.Locale;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.github.sirblobman.combatlogx.configuration.MainConfiguration;
import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import org.slf4j.Logger;

public abstract class CombatListener {
    private final ICombatLogX mod;

    public CombatListener(@NotNull ICombatLogX mod) {
        this.mod = mod;
    }

    protected final @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    protected final @NotNull Logger getPluginLogger() {
        ICombatLogX plugin = getCombatLogX();
        return plugin.getLogger();
    }

    protected final @NotNull ICombatManager getCombatManager() {
        ICombatLogX plugin = getCombatLogX();
        return plugin.getCombatManager();
    }

    protected final @NotNull IDeathManager getDeathManager() {
        ICombatLogX plugin = getCombatLogX();
        return plugin.getDeathManager();
    }

    protected final boolean isInCombat(@NotNull Player player) {
        ICombatManager combatManager = getCombatManager();
        return combatManager.isInCombat(player);
    }

    protected final boolean isDebugModeDisabled() {
        ICombatLogX plugin = getCombatLogX();
        return plugin.isDebugModeDisabled();
    }

    protected void printDebug(@NotNull String message) {
        if (isDebugModeDisabled()) {
            return;
        }

        Class<?> thisClass = getClass();
        String className = thisClass.getSimpleName();
        String logMessage = String.format(Locale.US, "[Debug] [%s] %s", className, message);

        Logger pluginLogger = getPluginLogger();
        pluginLogger.info(logMessage);
    }

    protected final boolean isWorldDisabled(@NotNull Entity entity) {
        Level world = entity.level();
        return isWorldDisabled(world);
    }

    protected final boolean isWorldDisabled(@NotNull Level world) {
        ICombatLogX combatLogX = getCombatLogX();
        MainConfiguration configuration = combatLogX.getConfiguration();
        return configuration.isDisabled(world);
    }
}
