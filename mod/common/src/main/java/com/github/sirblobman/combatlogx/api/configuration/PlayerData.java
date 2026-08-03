package com.github.sirblobman.combatlogx.api.configuration;

import com.mojang.serialization.Codec;
//? if <1.21.10
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21.10
/*import net.minecraft.world.level.saveddata.SavedDataType;*/
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// todo don't use SavedData, make own NBT thing.
//  see https://github.com/gigaherz/Ender-Rift/blob/master/src/main/java/dev/gigaherz/enderrift/rift/storage/RiftStorage.java
//  and https://github.com/gigaherz/Ender-Rift/pull/56
// todo not thread safe, not really any reason to use CompoundTag instead of serializing data
public final class PlayerData extends SavedData {

    private final @NotNull CompoundTag tag;

    private PlayerData(@NotNull CompoundTag tag) {
        this.tag = tag;
    }

    // make sure to mark dirty when done!
    private @NotNull CompoundTag getTag() {
        return this.tag;
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
    //? if <1.21.10 {
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        tag.merge(this.tag); // fixme i dont think this is even needed
        return tag;
    }
    //?}

    public static @Nullable PlayerData loadIfPresent(@NotNull ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().get(
                /*? if >=1.21.10 {*/ /*type(player) *//*?} else {*/ FACTORY, "clx_playerdata_" + player.getStringUUID()/*?}*/);
    }

    public static @NotNull PlayerData load(@NotNull ServerPlayer player) {
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(
                /*? if >=1.21.10 {*/ /*type(player) *//*?} else {*/ FACTORY, "clx_playerdata_" + player.getStringUUID()/*?}*/);
    }

    //? if >=1.21.10 {
    /*private static final Codec<PlayerData> CODEC = CompoundTag.CODEC.xmap(PlayerData::new, PlayerData::getTag);
    private static @NotNull SavedDataType<PlayerData> type(@NotNull ServerPlayer player) {
        return new SavedDataType<>("clx_playerdata_" + player.getStringUUID(),
                () -> new PlayerData(new CompoundTag()), CODEC, null);
    }
    *///?} else {
    private static final Factory<PlayerData> FACTORY = new Factory<>(() -> new PlayerData(new CompoundTag()),
              (tag, prov) -> new PlayerData(tag), null);
    //?}
}