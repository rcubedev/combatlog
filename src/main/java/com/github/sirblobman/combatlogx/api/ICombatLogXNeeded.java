package com.github.sirblobman.combatlogx.api;

import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import org.jetbrains.annotations.NotNull;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;

public interface ICombatLogXNeeded extends ILoggingProvider {

    /**
     * Called when the configuration files should be reloaded.
     */
    void onReload();

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
}
