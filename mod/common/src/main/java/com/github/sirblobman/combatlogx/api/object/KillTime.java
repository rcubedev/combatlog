package com.github.sirblobman.combatlogx.api.object;

import com.github.rcubedev.example.ISerializableEnum;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;

/**
 * The time that CombatLogX will kill players.
 *
 * @see PunishConfiguration#killTime
 */
public enum KillTime implements ISerializableEnum<KillTime> {
    /**
     * Kill the player the instant that they disconnect from the server.
     */
    QUIT,

    /**
     * Kill the player as soon as they log back in to the server.
     */
    JOIN,

    /**
     * Keep the player online.
     */
    KEEP_ONLINE,

    /**
     * Tell CombatLogX to not handle player killing.
     */
    NEVER;
}
