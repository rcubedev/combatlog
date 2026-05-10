package com.github.sirblobman.combatlogx.api.object;

import java.lang.ref.WeakReference;
import java.util.UUID;

import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// todo reapply new serverplayer on relog. should be done
// ensure to add and remove from ICombatManager#allCombatTags
public final class CombatTag implements Comparable<CombatTag> {
    private final UUID enemyId;
    private final TagType tagType;
    private final TagReason tagReason;
    private final long expireMillis;
    private WeakReference<Entity> enemyReference;

    public CombatTag(@Nullable Entity enemy, @NotNull TagType tagType, @NotNull TagReason tagReason,
                     long expireMillis) {
        if (enemy != null) {
            this.enemyId = enemy.getUUID();
            this.enemyReference = new WeakReference<>(enemy);
        } else {
            this.enemyId = null;
            this.enemyReference = null;
        }

        this.tagType = tagType;
        this.tagReason = tagReason;
        this.expireMillis = expireMillis;
    }

    public @NotNull CombatTag register(ICombatLogX mod) {
        mod.getCombatManager().getAllCombatTags().add(this);
        return this;
    }

    public @Nullable UUID getEnemyId() {
        return this.enemyId;
    }

    public @Nullable Entity getEnemy() {
        if (this.enemyReference == null) return null;

        return this.enemyReference.get();
    }

    public void setEnemy(@NotNull Entity enemy) {
        if (!enemy.getUUID().equals(this.enemyId)) {
            throw new IllegalArgumentException("Cannot change the enemy of a CombatTag to a different entity UUID.");
        }
        this.enemyReference = new WeakReference<>(enemy);
    }

    public boolean doesEnemyMatch(@NotNull Entity entity) {
        Entity enemy = getEnemy();
        return entity == enemy || entity.getUUID().equals(getEnemyId());
        // todo this isnt really right as old combattags may have unloaded entities with the same UUID
        //  e.g. entity combat tags player -> entity gets unloaded -> new entity loaded with same uuid -> potential issue.
    }

    public @NotNull TagType getTagType() {
        return this.tagType;
    }

    public @NotNull TagReason getTagReason() {
        return this.tagReason;
    }

    public long getExpireMillis() {
        return this.expireMillis;
    }

    public boolean isExpired() {
        long systemMillis = System.currentTimeMillis();
        long expireMillis = getExpireMillis();
        return (systemMillis >= expireMillis);
    }

    @Override
    public int compareTo(@NotNull CombatTag other) {
        long thisExpireMillis = getExpireMillis();
        long otherExpireMillis = other.getExpireMillis();
        return Long.compare(thisExpireMillis, otherExpireMillis);
    }
}
