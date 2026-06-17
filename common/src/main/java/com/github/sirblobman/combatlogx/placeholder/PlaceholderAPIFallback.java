package com.github.sirblobman.combatlogx.placeholder;

import com.github.sirblobman.combatlogx.platform.IPlaceholderAPI;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PlaceholderAPIFallback implements IPlaceholderAPI {

    @Override
    public Component format(MinecraftServer server, Component input) {
        return input;
    }

    @Override
    public Component format(GameProfile profile, MinecraftServer server, Component input) {
        return input;
    }

    @Override
    public Component format(ServerPlayer player, Component input) {
        return input;
    }

    @Override
    public Component format(CommandSourceStack sourceStack, Component input) {
        return input;
    }

    @Override
    public Component format(Entity entity, Component input) {
        return input;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
