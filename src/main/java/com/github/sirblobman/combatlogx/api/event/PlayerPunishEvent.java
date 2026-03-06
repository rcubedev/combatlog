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
 * A custom event that will fire before a player is punished.
 * If the event is cancelled, the player will not be punished.
 *
 * @author SirBlobman
 */
public final class PlayerPunishEvent extends CustomPlayerEventCancellable {

    public static final EventHandler<PlayerPunishEvent> EVENT = EventHandlerFactory.createArrayBacked(PlayerPunishEvent.class);
    private final UntagReason punishReason;
    private final List<Entity> enemyList;

    public PlayerPunishEvent(@NotNull Player player, @NotNull UntagReason punishReason,
                             @NotNull List<Entity> enemyList) {
        super(player);
        this.punishReason = punishReason;
        this.enemyList = new ArrayList<>(enemyList);
    }

    /**
     * @return The original {@link UntagReason} that the player was punished for.
     */
    public @NotNull UntagReason getPunishReason() {
        return this.punishReason;
    }

    /**
     * @return The list of enemies the player had when punished.
     */
    public @NotNull List<Entity> getEnemies() {
        return Collections.unmodifiableList(this.enemyList);
    }

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public EventHandler<PlayerPunishEvent> handler() {
        return EVENT;
    }
}
