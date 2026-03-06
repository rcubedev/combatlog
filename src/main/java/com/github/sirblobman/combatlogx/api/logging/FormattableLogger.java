package com.github.sirblobman.combatlogx.api.logging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

public abstract class FormattableLogger implements Logger, ICustomLogger {

    private final Logger logger;

    public FormattableLogger(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * @return the wrapped SLF4J logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Formats the given log message before it is logged. This method should
     * be overridden by subclasses to provide custom message formatting
     * logic (e.g., adding prefixes, suffixes, or any other modifications).
     * <p>
     * The formatted message will be passed to the wrapped SLF4J logger for actual logging.
     *
     * @param message the original log message to be formatted
     * @return the formatted log message that will be logged.
     */
    public abstract String formatMessage(String message);

    // Wrapped methods
    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(formatMessage(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(formatMessage(format), arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(formatMessage(format), arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(formatMessage(format), arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(formatMessage(msg), t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(marker, formatMessage(msg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, formatMessage(format), arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, formatMessage(format), arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments) {
        logger.trace(marker, formatMessage(format), arguments);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, formatMessage(msg), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(formatMessage(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(formatMessage(format), arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(formatMessage(format), arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(formatMessage(format), arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(formatMessage(msg), t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(marker, formatMessage(msg));
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, formatMessage(format), arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, formatMessage(format), arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, formatMessage(format), arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, formatMessage(msg), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(formatMessage(msg));
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(formatMessage(format), arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(formatMessage(format), arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(formatMessage(format), arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(formatMessage(msg), t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(marker, formatMessage(msg));
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, formatMessage(format), arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, formatMessage(format), arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, formatMessage(format), arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, formatMessage(msg), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(formatMessage(msg));
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(formatMessage(format), arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(formatMessage(format), arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(formatMessage(format), arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(formatMessage(msg), t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(marker, formatMessage(msg));
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, formatMessage(format), arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, formatMessage(format), arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, formatMessage(format), arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, formatMessage(msg), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(formatMessage(msg));
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(formatMessage(format), arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(formatMessage(format), arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(formatMessage(format), arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(formatMessage(msg), t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(marker, formatMessage(msg));
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, formatMessage(format), arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, formatMessage(format), arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, formatMessage(format), arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, formatMessage(msg), t);
    }
}
