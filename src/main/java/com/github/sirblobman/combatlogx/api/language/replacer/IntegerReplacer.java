package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class IntegerReplacer extends Replacer {
    private final int replacement;

    public IntegerReplacer(@NotNull String target, int replacement) {
        super(target);
        this.replacement = replacement;
    }

    private int getInteger() {
        return this.replacement;
    }

    public @NotNull Component getReplacement() {
        int replacement = this.getInteger();
        return Component.text(replacement);
    }

    public @NotNull String getReplacementString() {
        int replacement = this.getInteger();
        return Integer.toString(replacement);
    }
}
