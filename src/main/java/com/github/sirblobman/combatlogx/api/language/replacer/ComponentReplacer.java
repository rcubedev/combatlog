package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class ComponentReplacer extends Replacer {
    private final Component replacement;

    public ComponentReplacer(@NotNull String target, @NotNull Component replacement) {
        super(target);
        this.replacement = replacement;
    }

    public @NotNull Component getReplacement() {
        return this.replacement;
    }

    public @NotNull String getReplacementString() {
        Component replacement = this.getReplacement();
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        return serializer.serialize(replacement);
    }
}
