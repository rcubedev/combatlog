package com.github.sirblobman.combatlogx.api.configuration;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

// todo don't use SavedData, make own NBT thing.
//  see https://github.com/gigaherz/Ender-Rift/blob/master/src/main/java/dev/gigaherz/enderrift/rift/storage/RiftStorage.java
//  and https://github.com/gigaherz/Ender-Rift/pull/56
public final class PlayerData extends SavedData {

    private @Nullable CompoundTag tag;

    private PlayerData() {}

    // make sure to mark dirty when done!
    private @NotNull CompoundTag getTag() {
        return this.tag == null ? this.tag = new CompoundTag() : this.tag;
    }

    public void transform(@NotNull Consumer<CompoundTag> transformation) {
        CompoundTag tag = getTag();
        transformation.accept(tag);
        this.setDirty();
    }

    // returns a copy
    public CompoundTag getData() {
        CompoundTag tag = getTag();
        return tag.copy();
    }

    // do not call; the game will call it for us.
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        if (this.tag == null) return tag;
        tag.merge(this.tag);
        return tag;
    }

    public static @Nullable PlayerData loadIfPresent(@NotNull ServerPlayer player) {
        return player.server.overworld().getDataStorage().get(factory(), "clx_playerdata_" + player.getStringUUID());
    }

    public static @NotNull PlayerData load(@NotNull ServerPlayer player) {
        return player.server.overworld().getDataStorage().computeIfAbsent(factory(), "clx_playerdata_" + player.getStringUUID());
    }

    private static @NotNull PlayerData create() {
        PlayerData data = new PlayerData();
        data.tag = new CompoundTag();
        return data;
    }

    private static @NotNull PlayerData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        PlayerData data = PlayerData.create();
        data.tag = tag;
        return data;
    }

    private static Factory<PlayerData> factory() {
        // fabric api & neo make it safe to pass null.
        return new Factory<>(PlayerData::create, PlayerData::load, null);
    }
}