package com.github.sirblobman.combatlogx.api;

import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.sirblobman.combatlogx.PermissionHolder;
import com.github.sirblobman.combatlogx.api.configuration.CommandConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionManager;import com.github.sirblobman.combatlogx.api.expansion.ExpansionRegistry;import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import org.jetbrains.annotations.NotNull;

public interface ICombatLogX extends ILoggingProvider, TaskOwner {

    /**
     * Called when the configuration files should be reloaded.
     */
    void onReload();

    /**
     * todo
     * @return
     */
    @NotNull PermissionHolder getPermissionHolder();

    /**
     * @return The player data file manager for this mod.
     */
    @NotNull PlayerDataManager getPlayerDataManager();

    /**
     * @return The language configuration manager for this mod.
     */
    @NotNull LanguageManager<LanguageFileConfiguration> getLanguageManager();

    /**
     * @return The expansion registry for this mod.
     */
    @NotNull ExpansionRegistry getExpansionRegistry();

    /**
     * @return The expansion manager for this mod.
     */
    @NotNull ExpansionManager getExpansionManager();

    /**
     * @return The combat manager for this mod.
     */
    @NotNull ICombatManager getCombatManager();

    /**
     * @return The timer and notification manager for this mod.
     */
    @NotNull ITimerManager getTimerManager();

    /**
     * @return The punishment manager for this mod.
     */
    @NotNull IPunishManager getPunishManager();

    /**
     * @return The death manager for this mod.
     */
    @NotNull IDeathManager getDeathManager();

    /**
     * @return The placeholder hook manager for this mod.
     */
    @NotNull IPlaceholderManager getPlaceholderManager();

    /**
     * @return The combat forgiveness manager for this mod.
     */
    @NotNull IForgiveManager getForgiveManager();

    /**
     * @return The task scheduler for this mod.
     */
    @NotNull TaskScheduler getScheduler();

    /**
     * @return {@code true} if the debug mode feature is disabled, otherwise {@code false}.
     */
    @Override
    default boolean isDebugMode() {
        return getConfiguration().debugMode || ILoggingProvider.super.isDebugMode();
    }

    /**
     * Print some messages to the server logs.
     * If debug-mode is not enabled, the messages should not be sent.
     *
     * @param messageArray An array of messages to print
     * @see #isDebugMode()
     */
    default void printDebug(String @NotNull ... messageArray) {
        ILoggingProvider.super.printDebug(messageArray);
    }

    /**
     * Print a thrown exception to the server logs.
     * If debug-mode is not enabled, the error should not be sent.
     *
     * @param ex The error that was thrown.
     * @see #printDebug(String...)
     * @see #isDebugMode()
     */
    default void printDebug(@NotNull Throwable ex) {
        ILoggingProvider.super.printDebug(ex);
    }

    /**
     * @return The main configuration
     */
    @NotNull MainConfiguration getConfiguration();

    /**
     * @return The command configuration
     */
    @NotNull CommandConfiguration getCommandConfiguration();

    /**
     * @return The punishment configuration
     */
    @NotNull PunishConfiguration getPunishConfiguration();

    @NotNull ICrystalManager getCrystalManager();
}
