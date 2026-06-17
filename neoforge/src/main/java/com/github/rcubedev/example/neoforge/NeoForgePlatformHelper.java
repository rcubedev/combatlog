package com.github.rcubedev.example.neoforge;

import com.github.rcubedev.example.platform.IPlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getModsDir() {
        return FMLPaths.MODSDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader/*? if >=1.21.10 {*/ /*.getCurrent()*/ /*?}*/ .isProduction();
    }

    @Override
    public boolean isNPCInst(ServerPlayer player) {
        return player instanceof FakePlayer;
    }
}
