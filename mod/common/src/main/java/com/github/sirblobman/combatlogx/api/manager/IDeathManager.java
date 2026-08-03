package com.github.sirblobman.combatlogx.api.manager;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDeathManager extends ICombatLogXNeeded {
    /**
     * Track and kill a player.
     * The player will be killed by {@link ServerPlayer#kill()}
     *
     * @param player The {@link ServerPlayer} to kill.
     */
    void kill(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList);

    /**
     * Check if a player was killed while tracked.
     *
     * @param player The {@link ServerPlayer} to check.
     * @return {@code true} if the player died from CombatLogX,
     * {@code false} if they were killed by any other reason.
     */
    boolean wasPunishKilled(@NotNull ServerPlayer player);

    /**
     * Stop tracking a player.
     *
     * @param player The {@link ServerPlayer} to stop tracking.
     * @return {@code true} if the player was previously being tracked.
     */
    boolean stopTracking(@NotNull ServerPlayer player);

    /**
     * @param player The player to check.
     * @return A list of tracked enemies for the player.
     */
    @NotNull List<Entity> getTrackedEnemies(@NotNull ServerPlayer player);

    /**
     * Updates all internal death records to replace an old player entity with a new one.
     * This is necessary when a player logs back in to replace the temporary NPC entity
     * with the new ServerPlayer entity. It iterates through all tracked death records
     * and replaces any reference to {@code oldPlayer} with {@code newPlayer}.
     *
     * @param oldPlayer The old, stale player entity (usually an NPC).
     * @param newPlayer The new, active player entity.
     */
    void transferState(@NotNull ServerPlayer oldPlayer, @NotNull ServerPlayer newPlayer);
}
