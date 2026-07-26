package com.github.sirblobman.combatlogx.api.placeholder;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * You must override all required methods and at least one of the following methods:
 * <ul>
 * <li>{@link #getReplacementString(ServerPlayer, List, String)}</li>
 * <li>{@link #getReplacement(ServerPlayer, List, String)}</li>
 * </ul>
 * <p>
 * If you do not override at least one, you will get an infinite loop.
 */
public interface IPlaceholderExpansion extends ICombatLogXNeeded {
    @NotNull String getId();

    default @Nullable String getReplacementString(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                                  @NotNull String placeholder) {
        Component replacement = getReplacement(player, enemyList, placeholder);
        if (replacement == null || Component.empty().equals(replacement)) {
            return "";
        }

        // todo shouldnt this use minimsg?
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        return serializer.serialize(replacement);
    }

    default @Nullable Component getReplacement(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                               @NotNull String placeholder) {
        String string = getReplacementString(player, enemyList, placeholder);
        if (string == null || string.isEmpty()) {
            return Component.empty();
        }

        if (string.contains("§")) {
            LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
            return serializer.deserialize(string);
        }

        if (string.contains("&")) {
            LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
            return serializer.deserialize(string);
        }

        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        return miniMessage.deserialize(string);
    }
}
