package com.github.sirblobman.combatlogx.api.manager;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import org.jetbrains.annotations.NotNull;

public interface IPunishManager extends ICombatLogXNeeded {
    /**
     * Punish a player for logging out during combat.
     * Also called when expire punishing is enabled in the configuration.
     *
     * @param player          The {@link ServerPlayer} to punish.
     * @param punishReason    The original reason that the player was removed from combat.
     * @param previousEnemies The list of enemies that the player had when they were untagged.
     * @return {@code true} if the mod was able to punish the player successfully.
     */
    boolean punish(@NotNull ServerPlayer player, @NotNull UntagReason punishReason, @NotNull List<Entity> previousEnemies);

    /**
     * Get the total amount of times a player was punished.
     * If the punishment tracker is disabled, this will always return a value of zero.
     *
     * @param player The {@link ServerPlayer} to check.
     * @return The amount of times the player was punished.
     */
    long getPunishmentCount(@NotNull ServerPlayer player);

    void resetPunishmentCount(@NotNull ServerPlayer player);
}
