package com.github.sirblobman.combatlogx.manager;

import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;

public abstract class Manager implements ICombatLogXNeeded {
    private final ICombatLogX mod;

    public Manager(@NotNull ICombatLogX mod) {
        this.mod = mod;
    }

    @Override
    public final @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    protected final @NotNull PlayerDataManager getPlayerDataManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getPlayerDataManager();
    }

    protected final void printDebug(String @NotNull ... messages) {
        ICombatLogX mod = getCombatLogX();
        mod.printDebug(messages);
    }
}
