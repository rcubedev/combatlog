package com.github.rcubedev.example.event.server.lifecycle;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class ServerStoppedEvent extends ServerLifecycleEvent {

    public ServerStoppedEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
