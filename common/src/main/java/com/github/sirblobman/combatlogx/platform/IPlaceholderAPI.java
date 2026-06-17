package com.github.sirblobman.combatlogx.platform;

import com.github.rcubedev.example.services.api.spi.Eager;
import com.github.rcubedev.example.util.IService;
import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public interface IPlaceholderAPI extends IService, Eager {
    CompletableFuture<IPlaceholderAPI> INSTANCE_FUTURE = new CompletableFuture<>();

    /**
     * Placeholder API instance getter.
     * Blocks until the placeholder API instance is available.
     *
     * @return The placeholder API instance.
     */
    static IPlaceholderAPI getInstance() {
        return IService.getInstance(INSTANCE_FUTURE);
    }

    /**
     * Sets the placeholder API instance.
     * @param instance The placeholder API instance
     */
    static void setInstance(IPlaceholderAPI instance) {
        IService.setInstance(INSTANCE_FUTURE, instance);
    }

    Component format(MinecraftServer server, Component input);

    Component format(GameProfile profile, MinecraftServer server, Component input);

    Component format(ServerPlayer player, Component input);

    Component format(CommandSourceStack sourceStack, Component input);

    Component format(Entity entity, Component input);
}
