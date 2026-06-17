package com.github.sirblobman.combatlogx.api.configuration;

import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class PlayerDataManager {

    private final ICombatLogX mod;

    public PlayerDataManager(ICombatLogX mod) {
        this.mod = mod;
    }

    private @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    private @NotNull Logger getLogger() {
        return getCombatLogX().getLogger();
    }

    public @NotNull PlayerData get(@NotNull ServerPlayer player) {
        return PlayerData.load(player);
    }

    public @Nullable PlayerData getIfPresent(@NotNull ServerPlayer player) {
        return PlayerData.loadIfPresent(player);
    }

    @Deprecated
    public void save(@NotNull ServerPlayer player) {
        get(player).setDirty();
    }
}
