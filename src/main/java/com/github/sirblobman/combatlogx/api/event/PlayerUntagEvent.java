package com.github.sirblobman.combatlogx.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.github.rcubedev.example.event.api.EventHandler;
import com.github.rcubedev.example.event.api.EventHandlerFactory;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import org.jetbrains.annotations.NotNull;

/**
 * A custom event that is fired when a player is removed from combat.
 *
 * @author SirBlobman
 */
public final class PlayerUntagEvent extends CustomPlayerEvent {

    public static final EventHandler<PlayerUntagEvent> EVENT = EventHandlerFactory.createArrayBacked(PlayerUntagEvent.class);
    private final UntagReason untagReason;
    private final List<Entity> previousEnemyList;

    public PlayerUntagEvent(@NotNull Player player, @NotNull UntagReason untagReason,
                            @NotNull List<Entity> previousEnemyList) {
        super(player);
        this.untagReason = untagReason;
        this.previousEnemyList = new ArrayList<>(previousEnemyList);
    }

    /**
     * @return The reason that the player was removed from combat.
     * @see #getPlayer()
     */
    public @NotNull UntagReason getUntagReason() {
        return this.untagReason;
    }

    public @NotNull List<Entity> getPreviousEnemies() {
        return Collections.unmodifiableList(this.previousEnemyList);
    }

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public EventHandler<PlayerUntagEvent> handler() {
        return EVENT;
    }
}
