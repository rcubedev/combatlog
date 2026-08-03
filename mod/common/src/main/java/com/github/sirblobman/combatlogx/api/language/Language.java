package com.github.sirblobman.combatlogx.api.language;

import folk.sisby.kaleido.api.WrappedConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Language<T extends WrappedConfig & ILanguage> {
    private final String languageName;
    private final T configuration;
    private final Locale javaLocale;
    private final MiniMessage miniMessage;
    private final Map<String, String> rawMessageMap;
    private final Map<String, Component> messageMap;
    private final Map<String, List<Component>> messageListMap;
    private DecimalFormat decimalFormat;

    public Language(@NotNull String languageName, @NotNull T configuration, @NotNull MiniMessage miniMessage) {
        this.languageName = languageName;
        this.configuration = configuration;
        Locale javaLocale = Locale.forLanguageTag(languageName);
        this.javaLocale = javaLocale != null ? javaLocale : Locale.US;

        this.miniMessage = miniMessage;
        this.rawMessageMap = new ConcurrentHashMap<>();
        this.messageMap = new ConcurrentHashMap<>();
        this.messageListMap = new ConcurrentHashMap<>();
    }

    public @NotNull String getLanguageName() {
        return this.languageName;
    }

    public @NotNull T getConfiguration() {
        return this.configuration;
    }

    public @NotNull Locale getJavaLocale() {
        return this.javaLocale;
    }

    public @NotNull MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    public @NotNull String getRawMessage(@NotNull String path, @NotNull ConfigGetter<T, String> getter) {
        return this.rawMessageMap.computeIfAbsent(path, s -> fetchRawMessage(getter));
    }

    private @NotNull String fetchRawMessage(@NotNull ConfigGetter<T, String> getter) {
        return getter.apply(getConfiguration());
    }

    public @NotNull Component getMessage(@NotNull String path, @NotNull ConfigGetter<T, String> getter) {
        return this.messageMap.computeIfAbsent(path, s -> this.fetchMessage(s, getter));
    }

    private @NotNull Component fetchMessage(@NotNull String path, @NotNull ConfigGetter<T, String> getter) {
        String rawMessage = this.getRawMessage(path, getter);
        MiniMessage miniMessage = this.getMiniMessage();
        return miniMessage.deserialize(rawMessage);
    }

    public @NotNull List<Component> getMessageList(@NotNull String path, @NotNull ConfigGetter<T, List<String>> getter) {
        return this.messageListMap.computeIfAbsent(path, s -> this.fetchMessageList(getter));
    }

    private @NotNull List<Component> fetchMessageList(@NotNull ConfigGetter<T, List<String>> getter) {
        List<String> rawMessages = getter.apply(getConfiguration());
        List<Component> messages = new ArrayList<>();
        MiniMessage miniMessage = this.getMiniMessage();

        for (String rawMessage : rawMessages) {
            Component component = miniMessage.deserialize(rawMessage);
            messages.add(component);
        }

        return messages;
    }

    public @NotNull DecimalFormat getDecimalFormat() {
        if (this.decimalFormat != null) {
            return this.decimalFormat;
        }
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        this.decimalFormat = new DecimalFormat("0.00", symbols);
        return this.decimalFormat;
    }

    public void setDecimalFormat(@NotNull DecimalFormat format) {
        this.decimalFormat = format;
    }
}
