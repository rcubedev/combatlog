package com.github.rcubedev.example.event.server.lifecycle;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class ServerStartedEvent extends ServerLifecycleEvent {

    public ServerStartedEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
