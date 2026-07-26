package com.github.sirblobman.combatlogx.fabric.compat.textplaceholderapi;

import com.mojang.authlib.GameProfile;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;

/**
 * Utility class for interacting with TextPlaceholderAPI in a safe and isolated manner.
 */
public final class TextPlaceholderAPIIntegration {

    public TextPlaceholderAPIIntegration() {}

    public static Component format(MinecraftServer server, Component input) {
        PlaceholderContext context = PlaceholderContext.of(server);
        return parse(context, input);
    }

    public static Component format(GameProfile profile, MinecraftServer server, Component input) {
        PlaceholderContext context = PlaceholderContext.of(profile, server);
        return parse(context, input);
    }

    public static Component format(ServerPlayer player, Component input) {
        PlaceholderContext context = PlaceholderContext.of(player);
        return parse(context, input);
    }

    public static Component format(CommandSourceStack sourceStack, Component input) {
        PlaceholderContext context = PlaceholderContext.of(sourceStack);
        return parse(context, input);
    }

    public static Component format(Entity entity, Component input) {
        PlaceholderContext context = PlaceholderContext.of(entity);
        return parse(context, input);
    }

    private static Component parse(PlaceholderContext context, Component input) {
        return Placeholders.parseText(input, context);
    }
}