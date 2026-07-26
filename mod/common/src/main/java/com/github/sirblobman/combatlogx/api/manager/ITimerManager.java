package com.github.sirblobman.combatlogx.api.manager;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import com.github.sirblobman.combatlogx.api.object.TimerUpdater;
import org.jetbrains.annotations.NotNull;

public interface ITimerManager extends ICombatLogXNeeded {
    /**
     * @return A {@link Set} of {@link TimerUpdater}s that are currently registered.
     */
    @NotNull Set<TimerUpdater> getTimerUpdaters();

    /**
     * Register a {@link TimerUpdater} instance.
     *
     * @param task The instance to register.
     */
    void addUpdaterTask(@NotNull TimerUpdater task);

    /**
     * Remove all timers in this manager from the player.
     *
     * @param player The {@link ServerPlayer} to remove the timers from.
     */
    void remove(@NotNull ServerPlayer player);

    /**
     * Register the manager
     */
    void register();
}
