package com.github.rcubedev.example.task.impl;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TickContext(@NotNull MinecraftServer server, @Nullable ServerPlayer player) {
    public static TickContext ofServer(@NotNull MinecraftServer server) {
        return new TickContext(server, null);
    }

    public static TickContext ofPlayer(@NotNull ServerPlayer player) {
        return new TickContext(player.server, player);
    }

    /**
     * @return the players this context should dispatch player-tasks to.
     */
    public List<ServerPlayer> playerTargets() {
        return player != null ? List.of(player) : server.getPlayerList().getPlayers();
    }
}