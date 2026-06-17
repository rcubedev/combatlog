package com.github.sirblobman.combatlogx.api.language.replacer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

public final class TranslatableReplacer extends Replacer {
    private final String translationKey;
    private final ComponentLike[] args;

    public TranslatableReplacer(@NotNull String target, @NotNull String translationKey, @NotNull ComponentLike... args) {
        super(target);
        this.translationKey = translationKey;
        this.args = args;
    }

    public @NotNull Component getReplacement() {
        return Component.translatable(this.translationKey, this.args);
    }

    public @NotNull String getReplacementString() {
        Component replacement = this.getReplacement();
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        return serializer.serialize(replacement);
    }
}
