package com.github.sirblobman.combatlogx.api.placeholder;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.compat.textplaceholderapi.TextPlaceholderAPIIntegration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderHelper {

    public static @NotNull net.minecraft.network.chat.Component getEnemyNameNative(@NotNull ICombatLogX mod, @NotNull ServerPlayer player,
                                                                                   @Nullable Entity entity) {
        if (entity == null) {
            return CombatLogX.createAudiences(player).toNative(getUnknownEnemy(mod, player));
        }

        return entity.getName();
    }
    public static @NotNull Component getEnemyName(@NotNull ICombatLogX mod, @NotNull ServerPlayer player,
                                                  @Nullable Entity entity) {
        if (entity == null) {
            return getUnknownEnemy(mod, player);
        }

        return CombatLogX.createAudiences(player).toAdventure(entity.getName());
    }

    public static @NotNull Component getUnknownEnemy(@NotNull ICombatLogX mod, @NotNull ServerPlayer player) {
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        return languageManager.getMessage(player.createCommandSourceStack(), "placeholder.unknownEnemy", c -> c.placeholder.unknownEnemy);
    }

    public static @NotNull Component replacePlaceholderAPI(@NotNull ServerPlayer player, @NotNull Component component) {
        FabricServerAudiences audiences = CombatLogX.createAudiences(player);
        if (FabricLoader.getInstance().isModLoaded("placeholder-api")) {
            return audiences.toAdventure(TextPlaceholderAPIIntegration.format(player, audiences.toNative(component))); // fixme loss
        }

        return component;
    }

    public static @NotNull Component replacePlaceholderAPI(@NotNull ServerPlayer player, @NotNull String component, @NotNull ICombatLogX mod) {
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        return replacePlaceholderAPI(player, miniMessage.deserialize(component));
    }

    // returns a minimsg string.
    public static @NotNull String replacePlaceholderAPIAsString(@NotNull ServerPlayer player, @NotNull Component component, @NotNull ICombatLogX mod) {
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        return miniMessage.serialize(replacePlaceholderAPI(player, component));
    }

    // returns a minimsg string.
    public static @NotNull String replacePlaceholderAPIAsString(@NotNull ServerPlayer player, @NotNull String string, @NotNull ICombatLogX mod) {
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        return miniMessage.serialize(replacePlaceholderAPI(player, Component.text(string)));
    }
}
