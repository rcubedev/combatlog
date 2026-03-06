package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.github.rcubedev.example.event.api.EventHandler;
import com.github.rcubedev.example.event.api.EventHandlerFactory;
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
    public static EventHandler<PlayerPreTagEvent> EVENT = EventHandlerFactory.createArrayBacked(PlayerPreTagEvent.class);

    private final Entity enemy;
    private final TagType tagType;
    private final TagReason tagReason;

    public PlayerPreTagEvent(@NotNull Player player, @Nullable Entity enemy, @NotNull TagType tagType,
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

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public EventHandler<PlayerPreTagEvent> handler() {
        return EVENT;
    }
}
