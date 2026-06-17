package com.github.rcubedev.example.fabric;

import java.nio.file.Path;

import com.github.rcubedev.example.platform.IPlatformHelper;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getModsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isNPCInst(ServerPlayer player) {
        return player instanceof FakePlayer;
    }
}
