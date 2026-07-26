package com.github.rcubedev.example.event.server.lifecycle;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class ServerStoppingEvent extends ServerLifecycleEvent {

    public ServerStoppingEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
