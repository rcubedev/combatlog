package com.github.sirblobman.combatlogx.platform;

import com.github.rcubedev.example.util.IService;
import com.github.rcubedev.utils.services.api.spi.Eager;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public interface IPlaceholderAPI extends IService, Eager {

    /**
     * Placeholder API instance getter.
     *
     * @return The placeholder API instance.
     */
    static IPlaceholderAPI getInstance() {
        return Holder.INSTANCE;
    }

    Component format(MinecraftServer server, Component input);

    Component format(GameProfile profile, MinecraftServer server, Component input);

    Component format(ServerPlayer player, Component input);

    Component format(CommandSourceStack sourceStack, Component input);

    Component format(Entity entity, Component input);

    static class Holder {
        private static final IPlaceholderAPI INSTANCE = IService.createInstance(IPlaceholderAPI.class);
    }
}
