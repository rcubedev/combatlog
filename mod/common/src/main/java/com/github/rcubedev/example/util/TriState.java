package com.github.rcubedev.example.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    @Contract(pure = true)
    public static TriState fromBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Contract(pure = true)
    public static @NotNull TriState fromBoolean(@Nullable Boolean value) {
        return value == null ? DEFAULT : value ? TRUE : FALSE;
    }

    @Contract(pure = true)
    public boolean toBoolean(boolean defaultValue) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            case DEFAULT -> defaultValue;
        };
    }

    @Contract(pure = true)
    public static @Nullable Boolean toBoolean(@Nullable TriState triState) {
        if (triState == null) return false;
        return switch (triState) {
            case TRUE -> true;
            case FALSE -> false;
            case DEFAULT -> null;
        };
    }
}
