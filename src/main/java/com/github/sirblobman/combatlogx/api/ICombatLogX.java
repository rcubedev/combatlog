package com.github.sirblobman.combatlogx.api;

import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import com.github.sirblobman.combatlogx.configuration.MainConfiguration;
import org.jetbrains.annotations.NotNull;

public interface ICombatLogX extends ILoggingProvider {

    /**
     * Called when the configuration files should be reloaded.
     */
    void onReload();

    /**
     * @return The combat manager for this plugin.
     */
    @NotNull ICombatManager getCombatManager();

    /**
     * @return The timer and notification manager for this plugin.
     */
    @NotNull ITimerManager getTimerManager();

    /**
     * @return The punishment manager for this plugin.
     */
    @NotNull IPunishManager getPunishManager();

    /**
     * @return The death manager for this plugin.
     */
    @NotNull IDeathManager getDeathManager();

    /**
     * @return The placeholder hook manager for this plugin.
     */
    @NotNull IPlaceholderManager getPlaceholderManager();

    /**
     * @return The combat forgiveness manager for this plugin.
     */
    @NotNull IForgiveManager getForgiveManager();

    /**
     * @return {@code true} if the debug mode feature is disabled, otherwise {@code false}.
     */
    boolean isDebugModeDisabled();

    /**
     * Print some messages to the server logs.
     * If debug-mode is not enabled, the messages should not be sent.
     *
     * @param messageArray An array of messages to print
     * @see #isDebugModeDisabled()
     */
    void printDebug(String @NotNull ... messageArray);

    /**
     * Print a thrown exception to the server logs.
     * If debug-mode is not enabled, the error should not be sent.
     *
     * @param ex The error that was thrown.
     * @see #printDebug(String...)
     * @see #isDebugModeDisabled()
     */
    void printDebug(@NotNull Throwable ex);

    /**
     * @return The main configuration
     */
    @NotNull MainConfiguration getConfiguration();
    //
    // /**
    //  * @return The configuration reader for 'commands.yml'
    //  */
    // @NotNull CommandConfiguration getCommandConfiguration();
    //
    // /**
    //  * @return The configuration reader for 'punish.yml'
    //  */
    // @NotNull PunishConfiguration getPunishConfiguration();

    @NotNull ICrystalManager getCrystalManager();
}
