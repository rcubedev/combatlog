package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public final class LongReplacer extends Replacer {
    private final long replacement;

    public LongReplacer(String target, long replacement) {
        super(target);
        this.replacement = replacement;
    }

    private long getLong() {
        return this.replacement;
    }

    public @NotNull Component getReplacement() {
        long replacement = this.getLong();
        return Component.text(replacement);
    }

    public @NotNull String getReplacementString() {
        long replacement = this.getLong();
        return Long.toString(replacement);
    }
}
