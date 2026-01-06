package org.samo_lego.antilogout.datatracker;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongConsumer;
import java.util.function.LongUnaryOperator;

import static org.samo_lego.antilogout.AntiLogout.config;

public interface ILogoutRules {

    /**
     * Set of players that have disconnected,
     * but are still present in the world.
     */
    Set<ServerPlayer> DISCONNECTED_PLAYERS = new HashSet<>();

    Map<UUID, Component> SKIPPED_DEATH_MESSAGES = new HashMap<>();

    /**
     * Whether to allow disconnect for this player.
     *
     * @return true if allowed, false otherwise
     */
    boolean al$allowDisconnect();

    /**
     * Sets the time when the player can disconnect.
     *
     * @param systemTime time in milliseconds at which the player can disconnect without staying in the world.
     */
    void al$setAllowDisconnectAt(long systemTime);

    /**
     * Sets whether the player can disconnect.
     *
     * @param allow true if disconnect is allowed, false otherwise
     */

    void al$setAllowDisconnect(boolean allow);

    /**
     * Marks the player as in combat state until the specified time.
     *
     * @param endTime time in milliseconds at which the player leaves state.
     */
    default void al$setInCombatUntil(long endTime) {
        this.al$setAllowDisconnectAt(endTime);

        if (config.combatLog.notifyInCombat) {
            LongUnaryOperator timeLeftOp = (timeAtRun) -> (long) Math.ceil((endTime - timeAtRun) / 1000.0D);
            // Inform player
            long currentTime = System.currentTimeMillis();
            long timeLeft = timeLeftOp.applyAsLong(currentTime);

            ((ServerPlayer) this).displayClientMessage(this.al$getInCombatMessage(timeLeft), true);
            this.al$tickInCombat((endTime - 1), (timeAtRun) ->
                    ((ServerPlayer) this).displayClientMessage(this.al$getInCombatMessage(timeLeftOp.applyAsLong(timeAtRun)), true));
            this.al$delay(endTime, () ->
                    ((ServerPlayer) this).displayClientMessage(this.al$getEndCombatMessage(timeLeft), true));
        }
    }

    /**
     * Schedules a task execution after the specified delay.
     * Only one can be scheduled at a time.
     * (Scheduling new task will cancel the previous one)
     *
     * @param at   system time at which the task should be executed
     * @param task task to execute
     */
    void al$delay(long at, Runnable task);

    /**
     * Schedules a task to be executed each tick while in combat.
     * Only one can be scheduled at a time.
     * (Scheduling new task will cancel the previous one)
     *
     * @param finalTick system time at which the player leaves combat
     * @param task      task to execute, receives remaining time in milliseconds
     */
    void al$tickInCombat(long finalTick, LongConsumer task);

    /**
     * Gets the combat message.
     *
     * @param timeLeft duration of combat state in seconds
     * @return combat message
     */
    @ApiStatus.Internal
    default Component al$getInCombatMessage(long timeLeft) {

        return NonWrappingComponentSerializer.INSTANCE.serialize(MiniMessage.miniMessage().deserialize(config.combatLog.combatEnterMessage, Placeholder.unparsed("duration", String.valueOf(timeLeft))));
    }

    @ApiStatus.Internal
    default Component al$getEndCombatMessage(long duration) {

        return NonWrappingComponentSerializer.INSTANCE.serialize(MiniMessage.miniMessage().deserialize(config.combatLog.combatEndMessage, Placeholder.unparsed("duration", String.valueOf(duration))));
    }

    /**
     * Whether the player is fake (present in the world, but not connected).
     *
     * @return true if fake, false otherwise
     */
    boolean al$isFake();


    /**
     * Called when the player disconnects.
     */
    void al$onRealDisconnect();
}
