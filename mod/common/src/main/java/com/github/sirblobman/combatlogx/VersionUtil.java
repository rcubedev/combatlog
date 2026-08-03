package com.github.sirblobman.combatlogx;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// fixme move
public final class VersionUtil {

    private VersionUtil() {}

    public static @NotNull MinecraftServer getServer(@NotNull ServerPlayer player) {
        return player./*? if >=1.21.10 {*/ /*level(). *//*?}*/getServer();
    }

    public static @NotNull String getName(@NotNull GameProfile profile) {
        return profile./*? if >=1.21.10 {*/ /*name() *//*?} else {*/ getName() /*?}*/;
    }

    public static @NotNull UUID getUUID(@NotNull GameProfile profile) {
        return profile./*? if >=1.21.10 {*/ /*id() *//*?} else {*/ getId() /*?}*/;
    }
}
