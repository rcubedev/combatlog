package com.github.rcubedev.example.event.server.lifecycle;

import com.github.rcubedev.example.event.server.ServerEvent;import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public abstract class ServerLifecycleEvent extends ServerEvent {

    public ServerLifecycleEvent(@NotNull MinecraftServer server) {
        super(server);
    }
}
