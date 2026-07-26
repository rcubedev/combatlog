package com.github.rcubedev.example.task.api;

/**
 * Defines when a scheduled task is executed relative to server/player tick phases.
 */
public enum TaskType {

    /**
     * Runs at the very beginning of a server tick, before any game logic.
     */
    START_TICK,

    /**
     * Runs at the very end of a server tick, after all game logic.
     */
    END_TICK,

    /**
     * Runs at the start of a {@link net.minecraft.server.level.ServerPlayer ServerPlayer} tick.
     * <p>
     * The task will run for every online player.
     */
    START_SERVER_PLAYER_TICK,

    /**
     * Runs at the end of a {@link net.minecraft.server.level.ServerPlayer ServerPlayer} tick.
     * <p>
     * The task will run for every online player.
     */
    END_SERVER_PLAYER_TICK
}
