package com.github.sirblobman.combatlogx.api.manager;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import com.github.sirblobman.combatlogx.api.placeholder.IPlaceholderExpansion;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlaceholderManager extends ICombatLogXNeeded {
    void registerPlaceholderExpansion(@NotNull IPlaceholderExpansion expansion);

    @Nullable IPlaceholderExpansion getPlaceholderExpansion(@NotNull String id);

    @NotNull List<IPlaceholderExpansion> getPlaceholderExpansions();

    @Nullable String getPlaceholderReplacement(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                               @NotNull String placeholder);

    @Nullable Component getPlaceholderReplacementComponent(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                                           @NotNull String placeholder);

    @NotNull Component replaceAll(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList, @NotNull String string);

    void runReplacedCommands(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                             @NotNull Iterable<String> commands);
}
