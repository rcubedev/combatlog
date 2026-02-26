package com.github.sirblobman.combatlogx.api;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface ILoggingProvider {

    @NotNull Logger getLogger();

    default boolean isDebugMode() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    default void printDebug(@NotNull String message) {
        if (isDebugMode()) {
            getLogger().info("[Debug] {}", message);
        }
    }
}
