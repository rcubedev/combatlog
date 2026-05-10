package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;

public abstract class Replacer {
    private final String target;

    public Replacer(@NotNull String target) {
        this.target = target;
    }

    public @NotNull String getTarget() {
        return this.target;
    }

    public abstract @NotNull Component getReplacement();

    public abstract @NotNull String getReplacementString();

    public final @NotNull String replaceString(@NotNull String original) {
        String target = this.getTarget();
        String replacement = this.getReplacementString();
        return original.replace(target, replacement);
    }

    public final @NotNull TextReplacementConfig asReplacementConfig() {
        String target = this.getTarget();
        Component replacement = this.getReplacement();
        TextReplacementConfig.Builder builder = TextReplacementConfig.builder();
        return builder.matchLiteral(target).replacement(replacement).build();
    }
}
