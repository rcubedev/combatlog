package com.github.sirblobman.combatlogx.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;

public final class CrystalManager extends Manager implements ICrystalManager {
    private final Map<UUID, UUID> endCrystalMap;

    public CrystalManager(@NotNull ICombatLogX mod) {
        super(mod);
        this.endCrystalMap = new ConcurrentHashMap<>();
    }

    @Override
    public @Nullable ServerPlayer getPlacer(@NotNull EndCrystal crystal) {
        UUID entityId = crystal.getUUID();
        UUID playerId = this.endCrystalMap.get(entityId);
        if (playerId == null) return null;
        
        MinecraftServer server = crystal.level().getServer();
        return server == null ? null : server.getPlayerList().getPlayer(playerId);
    }

    @Override
    public void setPlacer(@NotNull EndCrystal crystal, @NotNull ServerPlayer player) {
        UUID entityId = crystal.getUUID();
        UUID playerId = player.getUUID();
        this.endCrystalMap.put(entityId, playerId);
    }

    @Override
    public void remove(@NotNull UUID crystalId) {
        this.endCrystalMap.remove(crystalId);
    }
}
