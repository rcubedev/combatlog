package com.github.sirblobman.combatlogx.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import org.jetbrains.annotations.NotNull;

// todo handle replacing ServerPlayer
public final class DeathManager extends Manager implements IDeathManager {
    private final Map<UUID, List<Entity>> killedPlayerMap;

    public DeathManager(@NotNull ICombatLogX mod) {
        super(mod);
        this.killedPlayerMap = new ConcurrentHashMap<>();
    }

    public Map<UUID, List<Entity>> getKilledPlayerMap() {
        return this.killedPlayerMap;
    }

    @Override
    public void kill(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList) {
        UUID playerId = player.getUUID();
        this.killedPlayerMap.put(playerId, new ArrayList<>(enemyList)); //fixme
        System.out.println("Should be killing the player");
        System.out.println(player.level());
        //fixme this didnt work either man
        // player.die(player.damageSources().genericKill());
        player.kill(/*? if >= 1.21.10 {*/ player.level() /*?}*/); // death msg changed by DeathListener
    }

    // fixme do i want to make it a punish kill if we keep them online forcefully and a player kills them
    //  (KillTime.KEEP_ONLINE)
    @Override
    public boolean wasPunishKilled(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        return this.killedPlayerMap.containsKey(playerId);
    }

    @Override
    public boolean stopTracking(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        List<Entity> oldValue = this.killedPlayerMap.remove(playerId);
        return (oldValue != null);
    }

    @Override
    public @NotNull List<Entity> getTrackedEnemies(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        return this.killedPlayerMap.getOrDefault(playerId, Collections.emptyList());
    }

    @Override
    public void transferState(@NotNull ServerPlayer oldPlayer, @NotNull ServerPlayer newPlayer) {
        this.killedPlayerMap.values().forEach(killerList -> killerList.replaceAll(entity -> entity == oldPlayer ? newPlayer : entity));
    }
}
