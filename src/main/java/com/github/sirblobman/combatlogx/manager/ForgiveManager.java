package com.github.sirblobman.combatlogx.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.object.CombatTag;

public final class ForgiveManager extends Manager implements IForgiveManager {
    private final Map<UUID, CombatTag> requestMap;

    public ForgiveManager(@NotNull ICombatLogX mod) {
        super(mod);
        this.requestMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean getToggleValue(@NotNull ServerPlayer player) {
        ICombatLogX mod = getCombatLogX();
        PlayerDataManager playerDataManager = mod.getPlayerDataManager();
        PlayerData data = playerDataManager.get(player);
        return data.getData().getBoolean("forgiveToggle");
    }

    @Override
    public void setToggle(@NotNull ServerPlayer player, boolean value) {
        ICombatLogX mod = getCombatLogX();
        PlayerDataManager playerDataManager = mod.getPlayerDataManager();
        PlayerData data = playerDataManager.get(player);

        data.transform(tag -> tag.putBoolean("forgiveToggle", value));
        // playerDataManager.save(player);
    }

    @Override
    public @Nullable CombatTag getActiveRequest(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        CombatTag combatTag = this.requestMap.get(playerId);
        if (combatTag == null) return null;

        if (combatTag.isExpired()) {
            removeRequest(player);
            return null;
        }

        return combatTag;
    }

    @Override
    public void setRequest(@NotNull ServerPlayer player, @NotNull CombatTag tag) {
        UUID playerId = player.getUUID();
        this.requestMap.put(playerId, tag);
    }

    @Override
    public void removeRequest(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        this.requestMap.remove(playerId);
    }
}
