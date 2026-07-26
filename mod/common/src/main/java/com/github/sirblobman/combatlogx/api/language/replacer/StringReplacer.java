package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class StringReplacer extends Replacer {
    private final String replacement;

    public StringReplacer(@NotNull String target, @NotNull String replacement) {
        super(target);
        this.replacement = replacement;
    }

    public @NotNull Component getReplacement() {
        String replacement = this.getReplacementString();
        return Component.text(replacement);
    }

    public @NotNull String getReplacementString() {
        return this.replacement;
    }
}
