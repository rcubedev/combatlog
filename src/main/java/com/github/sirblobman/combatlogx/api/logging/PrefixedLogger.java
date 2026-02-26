package com.github.sirblobman.combatlogx.api.logging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PrefixedLogger extends FormattableLogger {

    private final String prefix;

    public PrefixedLogger(@NotNull Logger logger, @NotNull String prefix) {
        super(logger);
        this.prefix = prefix;
    }

    @Override
    public String formatMessage(String message) {
        return String.format("[%s] %s", prefix, message);
    }
}
