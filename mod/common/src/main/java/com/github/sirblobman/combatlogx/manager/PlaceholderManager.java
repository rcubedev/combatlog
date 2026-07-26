package com.github.sirblobman.combatlogx.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sirblobman.combatlogx.VersionUtil;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.placeholder.IPlaceholderExpansion;
import com.github.sirblobman.combatlogx.api.placeholder.PlaceholderHelper;
import com.github.sirblobman.combatlogx.api.utility.CommandHelper;

public final class PlaceholderManager extends Manager implements IPlaceholderManager {
    private static final Pattern BRACKET_PLACEHOLDER_PATTERN;

    static {
        BRACKET_PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\S+)}");
    }

    private final Map<String, IPlaceholderExpansion> expansionMap;

    public PlaceholderManager(@NotNull ICombatLogX plugin) {
        super(plugin);
        this.expansionMap = new LinkedHashMap<>();
    }

    @Override
    public @Nullable IPlaceholderExpansion getPlaceholderExpansion(@NotNull String id) {
        return this.expansionMap.get(id);
    }

    @Override
    public @NotNull List<IPlaceholderExpansion> getPlaceholderExpansions() {
        Collection<IPlaceholderExpansion> valueCollection = this.expansionMap.values();
        List<IPlaceholderExpansion> valueList = new ArrayList<>(valueCollection);
        return Collections.unmodifiableList(valueList);
    }

    @Override
    public void registerPlaceholderExpansion(@NotNull IPlaceholderExpansion expansion) {
        String expansionId = expansion.getId();
        IPlaceholderExpansion oldRegistry = this.expansionMap.putIfAbsent(expansionId, expansion);
        if (oldRegistry != null) {
            String errorMessage = "A placeholder expansion with id '" + expansionId + "' is already registered.";
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @Override
    public @Nullable String getPlaceholderReplacement(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                                      @NotNull String placeholder) {
        return getExpansionResult(placeholder, (exp, sub) ->
                exp.getReplacementString(player, enemyList, sub));
    }

    @Override
    public @Nullable Component getPlaceholderReplacementComponent(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                                                  @NotNull String placeholder) {
        return getExpansionResult(placeholder, (exp, sub) ->
                exp.getReplacement(player, enemyList, sub));
    }

    @Override
    public @NotNull Component replaceAll(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList, @NotNull String string) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = BRACKET_PLACEHOLDER_PATTERN.matcher(string);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = getPlaceholderReplacement(player, enemyList, placeholder);
            if (replacement != null) {
                matcher.appendReplacement(builder, replacement);
            }
        }

        matcher.appendTail(builder);
        String replaced = builder.toString();
        return PlaceholderHelper.replacePlaceholderAPI(player, replaced, getCombatLogX());
    }

    @Override
    public void runReplacedCommands(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                    @NotNull Iterable<String> commands) {
        ICombatLogX plugin = getCombatLogX();
        for (String originalCommand : commands) {
            String replacedCommand = PlainTextComponentSerializer.plainText().serialize(replaceAll(player, enemyList, originalCommand));
            if (replacedCommand.startsWith("[PLAYER]")) {
                String playerCommand = replacedCommand.substring(8);
                CommandHelper.runSync(plugin, () -> CommandHelper.runAsPlayer(plugin, player, playerCommand));
            } else if (replacedCommand.startsWith("[OP]")) {
                String opCommand = replacedCommand.substring(4);
                CommandHelper.runSync(plugin, () -> CommandHelper.runAsOperator(plugin, player, opCommand));
            } else {
                CommandHelper.runSync(plugin, () -> CommandHelper.runAsConsole(plugin, VersionUtil.getServer(player), replacedCommand));
            }
        }
    }

    private <T> @Nullable T getExpansionResult(@NotNull String placeholder,
                                               BiFunction<IPlaceholderExpansion, String, T> mapper) {
        int colonIdx = placeholder.indexOf(':');
        if (colonIdx == -1) return null;

        String expansionId = placeholder.substring(0, colonIdx);
        IPlaceholderExpansion expansion = getPlaceholderExpansion(expansionId);
        if (expansion == null) return null;

        String subPlaceholder = placeholder.substring(colonIdx + 1);
        return mapper.apply(expansion, subPlaceholder);
    }
}
