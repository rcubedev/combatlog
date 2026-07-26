package com.github.rcubedev.example.event.server;

import com.github.rcubedev.example.event.api.Event;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public abstract class ServerEvent extends Event {

    private final MinecraftServer server;

    public ServerEvent(@NotNull MinecraftServer server) {
        this.server = server;
    }

    public @NotNull MinecraftServer getServer() {
        return this.server;
    }
}
