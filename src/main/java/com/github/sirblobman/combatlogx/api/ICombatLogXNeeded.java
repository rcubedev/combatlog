package com.github.sirblobman.combatlogx.api;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.github.sirblobman.combatlogx.CombatLogX.config;

public interface ICombatLogXNeeded extends ILoggingProvider {

    @NotNull ICombatLogX getCombatLogX();

    @Override
    default @NotNull Logger getLogger() {
        ICombatLogX combatLogX = getCombatLogX();
        return combatLogX.getLogger();
    }

    @Override
    default boolean isDebugMode() {
        ICombatLogX combatLogX = getCombatLogX();
        return FabricLoader.getInstance().isDevelopmentEnvironment() || config.debugMode);
    }
}
