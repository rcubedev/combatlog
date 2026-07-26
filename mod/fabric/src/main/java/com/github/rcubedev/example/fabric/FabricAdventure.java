package com.github.rcubedev.example.fabric;

import com.github.rcubedev.example.platform.IAdventure;
//? if <1.21.10 {
import net.kyori.adventure.platform.fabric.FabricAudiences;
//?} else {
/*import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.kyori.adventure.platform.modcommon.impl.NonWrappingComponentSerializer;
*///?}
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class FabricAdventure implements IAdventure {

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
        return /*? if >=1.21.10 {*/ /*NonWrappingComponentSerializer.INSTANCE *//*?} else {*/ FabricAudiences.nonWrappingSerializer()/*?}*/;
    }

    @Override
    public @NotNull net.minecraft.network.chat.Component update(net.minecraft.network.chat.Component input,
                                                                UnaryOperator<Component> modifier, MinecraftServer server) {
        return /*? if >=1.21.10 {*/ /*MinecraftServerAudiences.of(server) *//*?} else {*/ FabricAudiences /*?}*/.update(input, modifier);
    }
}
