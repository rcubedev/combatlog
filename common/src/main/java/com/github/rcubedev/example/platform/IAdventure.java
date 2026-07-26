package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.util.IService;
import com.github.sirblobman.combatlogx.VersionUtil;import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public interface IAdventure extends IService {
    /**
     * Adventure instance getter.
     *
     * @return The adventure instance.
     */
    static IAdventure getInstance() {
        return Holder.INSTANCE;
    }

    @NotNull Component asAdventure(net.minecraft.network.chat.Component component);

    @NotNull net.minecraft.network.chat.Component asNative(Component component);

    @NotNull ComponentSerializer<Component, Component, net.minecraft.network.chat.Component> nonWrappingSerializer();

    default @NotNull net.minecraft.network.chat.Component update(net.minecraft.network.chat.Component input,
                                                        UnaryOperator<Component> modifier, MinecraftServer server) {
        Component original = this.asAdventure(input);
        Component modified = modifier.apply(original);
        return this.asNative(modified);
    };

    default @NotNull net.minecraft.network.chat.Component update(net.minecraft.network.chat.Component input,
                                                                 UnaryOperator<Component> modifier, ServerPlayer player) {
        return update(input, modifier, VersionUtil.getServer(player));
    };

    default void sendSuccess(CommandSourceStack source, net.minecraft.network.chat.Component component,
                             UnaryOperator<Component> modifier, boolean sendToOps) {
        source.sendSuccess(() -> IAdventure.getInstance().update(component, modifier, source.getServer()), sendToOps);
    }

    default void sendSuccess(CommandSourceStack source, Component component, boolean sendToOps) {
        source.sendSuccess(() -> IAdventure.getInstance().asNative(component), sendToOps);
    }

    default void sendFailure(CommandSourceStack source, Component component) {
        source.sendFailure(IAdventure.getInstance().asNative(component));
    }

    static class Holder {
        private static final IAdventure INSTANCE = IService.createInstance(IAdventure.class);
    }
}
