package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.util.IService;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Platform abstraction for loader-specific helpers.
 */
public interface IPlatformHelper extends IService {

    /**
     * Platform helper getter.
     *
     * @return The platform helper instance.
     */
    static IPlatformHelper getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Config directory getter.
     *
     * @return The config directory path.
     */
    Path getConfigDir();

    /**
     * Mods directory getter.
     *
     * @return The mods directory path.
     */
    Path getModsDir();

    /**
     * Checks if the mod is loaded
     *
     * @return True if the a mod by the {@code modId} is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Development environment checker.
     *
     * @return True if running in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * NPC instance checker.
     *
     * @return True if the {@link ServerPlayer} is an instance of the loader's NPC type
     */
    boolean isNPCInst(ServerPlayer player);

    static class Holder {
        private static final IPlatformHelper INSTANCE = IService.createInstance(IPlatformHelper.class);
    }
}
