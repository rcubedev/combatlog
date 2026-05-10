package com.github.sirblobman.combatlogx.api.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.object.UntagReason;
import org.jetbrains.annotations.NotNull;

/**
 * A custom event that will fire before a player is punished.
 * If the event is cancelled, the player will not be punished.
 *
 * @author SirBlobman
 */
public final class PlayerPunishEvent extends CustomPlayerEventCancellable {

    private final UntagReason punishReason;
    private final List<Entity> enemyList;

    public PlayerPunishEvent(@NotNull ServerPlayer player, @NotNull UntagReason punishReason,
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
}
