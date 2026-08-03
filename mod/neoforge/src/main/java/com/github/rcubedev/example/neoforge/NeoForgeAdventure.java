package com.github.rcubedev.example.neoforge;

import com.github.rcubedev.example.platform.IAdventure;
import com.google.common.collect.Iterables;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

public class NeoForgeAdventure implements IAdventure {

    @Override
    public @NotNull MinecraftServerAudiences provider(@NotNull MinecraftServer server) {
        return MinecraftServerAudiences.of(server);
    }

    @Override
    public @NotNull Audience audience(@NotNull ServerPlayer player) {
        return this.provider(player.level().getServer()).audience(player);
    }

    @Override
    public @NotNull Audience audience(@NotNull Iterable<ServerPlayer> players) {
        Iterator<ServerPlayer> it = players.iterator();
        if (!it.hasNext()) return Audience.audience(List.of());

        MinecraftServer server = it.next().level().getServer();
        return this.provider(server).audience(players);
    }

    @Override
    public @NotNull Component asAdventure(net.minecraft.network.chat.Component component) {
        return this.nonWrappingSerializer().deserialize(component);
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component asNative(Component component) {
        return this.nonWrappingSerializer().serialize(component);
    }

    @Override
    public @NotNull ComponentSerializer<Component, Component, net.minecraft.network.chat.Component> nonWrappingSerializer() {
        //return new NonWrappingComponentSerializer(Suppliers.ofInstance(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)));
        return NonWrappingComponentSerializer.INSTANCE;
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component update(net.minecraft.network.chat.Component input,
                                                                UnaryOperator<Component> modifier, MinecraftServer server) {
        return this.provider(server).update(input, modifier);
    }
}
