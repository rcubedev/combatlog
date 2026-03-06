package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.github.rcubedev.example.event.api.EventHandler;
import com.github.rcubedev.example.event.api.EventHandlerFactory;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import org.jetbrains.annotations.NotNull;

/**
 * A custom event that is fired when a player is removed from combat with a specific enemy.
 * The event may not be called for enemy entities that have already been removed from the server.
 *
 * @author SirBlobman
 * @see PlayerUntagEvent
 */
public final class PlayerEnemyRemoveEvent extends CustomPlayerEvent {

    public static EventHandler<PlayerEnemyRemoveEvent> EVENT = EventHandlerFactory.createArrayBacked(PlayerEnemyRemoveEvent.class);
    private final UntagReason untagReason;
    private final Entity enemy;

    public PlayerEnemyRemoveEvent(@NotNull Player player, @NotNull UntagReason untagReason, @NotNull Entity enemy) {
        super(player);
        this.untagReason = untagReason;
        this.enemy = enemy;
    }

    /**
     * @return The reason that the player was removed from combat with the enemy.
     * @see #getPlayer()
     */
    public @NotNull UntagReason getUntagReason() {
        return this.untagReason;
    }

    /**
     * @return The previous enemy of the player./
     */
    public @NotNull Entity getEnemy() {
        return this.enemy;
    }

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public EventHandler<PlayerEnemyRemoveEvent> handler() {
        return EVENT;
    }
}
