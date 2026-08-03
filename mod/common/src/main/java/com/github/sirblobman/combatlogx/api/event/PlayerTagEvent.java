package com.github.sirblobman.combatlogx.api.event;

import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom event that will be fired when a player is put into combat.
 * If you want to prevent a player from being tagged, check {@link PlayerPreTagEvent}
 *
 * @author SirBlobman
 */
public final class PlayerTagEvent extends CustomPlayerEvent {

    private final Entity enemy;
    private final TagType tagType;
    private final TagReason tagReason;
    private long combatEndMillis;

    public PlayerTagEvent(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                          @NotNull TagReason tagReason, long combatEndMillis) {
        super(player);
        this.enemy = enemy;
        this.tagType = tagType;
        this.tagReason = tagReason;
        this.combatEndMillis = combatEndMillis;
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
     * @return The time (in millis) that the combat timer will end. This can change if the player is tagged again
     * @see #getPlayer()
     */
    public long getEndTime() {
        return this.combatEndMillis;
    }

    /**
     * Set the amount of time to wait before the player escapes from combat.
     *
     * @param millis The epoch time (in milliseconds) that the timer will end.
     * @see ICombatManager#getMaxTimerSeconds(ServerPlayer)
     */
    public void setEndTime(long millis) {
        this.combatEndMillis = millis;
    }
}
