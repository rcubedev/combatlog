package com.github.sirblobman.combatlogx.datatracker;

public interface ILogoutRules {

    /**
     * Whether the player is fake (present in the world, but not connected).
     *
     * @return true if fake, false otherwise
     */
    boolean al$isFake();

    /**
     * Set {@code disconnected} to true.
     */
    void al$setDisconnected();
}
