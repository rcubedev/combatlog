package com.github.sirblobman.combatlogx.fabric;

import com.github.sirblobman.combatlogx.fabric.compat.textplaceholderapi.TextPlaceholderAPIIntegration;
import com.github.sirblobman.combatlogx.platform.IPlaceholderAPI;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TextPlaceholderAPI implements IPlaceholderAPI {

    @Override
    public Component format(MinecraftServer server, Component input) {
        return TextPlaceholderAPIIntegration.format(server, input);
    }

    @Override
    public Component format(GameProfile profile, MinecraftServer server, Component input) {
        return TextPlaceholderAPIIntegration.format(profile, server, input);
    }

    @Override
    public Component format(ServerPlayer player, Component input) {
        return TextPlaceholderAPIIntegration.format(player, input);
    }

    @Override
    public Component format(CommandSourceStack sourceStack, Component input) {
        return TextPlaceholderAPIIntegration.format(sourceStack, input);
    }

    @Override
    public Component format(Entity entity, Component input) {
        return TextPlaceholderAPIIntegration.format(entity, input);
    }

    @Override
    public boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("placeholder-api");
    }
}
