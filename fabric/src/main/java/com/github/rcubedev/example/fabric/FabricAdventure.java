package com.github.rcubedev.example.fabric;

import com.github.rcubedev.example.platform.IAdventure;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class FabricAdventure implements IAdventure {

    @Override
    public @NotNull Component asAdventure(net.minecraft.network.chat.Component component) {
        return component.asComponent();
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component asNative(Component component) {
        return this.nonWrappingSerializer().serialize(component);
    }

    @Override
    public @NotNull ComponentSerializer<Component, Component, net.minecraft.network.chat.Component> nonWrappingSerializer() {
        return FabricAudiences.nonWrappingSerializer();
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component update(net.minecraft.network.chat.Component input,
                                                                UnaryOperator<Component> modifier, MinecraftServer server) {
        return FabricAudiences.update(input, modifier);
    }
}
