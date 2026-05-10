package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom event that will be fired before a player is put into combat.
 * If the event is cancelled, the player will not be tagged.
 *
 * @author SirBlobman
 */
public final class PlayerPreTagEvent extends CustomPlayerEventCancellable {

    private final Entity enemy;
    private final TagType tagType;
    private final TagReason tagReason;

    public PlayerPreTagEvent(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                             @NotNull TagReason tagReason) {
        super(player);
        this.enemy = enemy;
        this.tagType = tagType;
        this.tagReason = tagReason;
    }

    /**
     * @return The enemy that will tag the player or null if an enemy does not exist
     * @see #getPlayer()
     */
    public @Nullable Entity getEnemy() {
        return this.enemy;
    }

    /**
     * @return The type of entity that will cause this player to be tagged
     * @see #getPlayer()
     */
    public @NotNull TagType getTagType() {
        return this.tagType;
    }

    /**
     * @return The reason that will cause this player to be tagged.
     * @see #getPlayer()
     */
    public @NotNull TagReason getTagReason() {
        return this.tagReason;
    }
}
