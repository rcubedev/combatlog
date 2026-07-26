package com.github.rcubedev.example.event.server.lifecycle;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class ServerStartingEvent extends ServerLifecycleEvent {

    public ServerStartingEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
