package com.github.sirblobman.combatlogx.api;

import com.github.rcubedev.example.platform.IPlatformHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public interface ILoggingProvider {

    @NotNull Logger getLogger();

    default boolean isDebugMode() {
        return IPlatformHelper.getInstance().isDevelopmentEnvironment();
    }

    default void printDebug(String @NotNull ... messages) {
        if (!isDebugMode()) return;
        Logger logger = getLogger();
        for (String message : messages) {
            String prependedMessage = "[Debug] " + message;
            logger.info(prependedMessage);
        }
    }

    default void printDebug(@NotNull String message) {
        if (!isDebugMode()) return;
        getLogger().info("[Debug] {}", message);
    }

    default void printDebug(@NotNull Throwable ex) {
        if (!isDebugMode()) return;
        getLogger().warn("[Debug] Full Error Details: ", ex);
    }
}
