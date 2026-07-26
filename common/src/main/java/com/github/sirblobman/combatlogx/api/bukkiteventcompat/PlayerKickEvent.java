package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import com.github.rcubedev.example.platform.IAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player gets kicked from the server
 */
public class PlayerKickEvent extends PlayerEvent {

    private final Component kickReason;

    @ApiStatus.Internal
    public PlayerKickEvent(@NotNull ServerPlayer playerKicked, @NotNull Component kickReason) {
        super(playerKicked);
        this.kickReason = kickReason;
    }

    /**
     * Gets the reason why the player is getting kicked
     *
     * @return kick reason
     */
    public @NotNull Component getKickReason() {
        return this.kickReason;
    }

    /**
     * Gets the reason why the player is getting kicked
     *
     * @return string kick reason
     */
    public @NotNull String getReason() {
        // fixme idk if this works w/ translatable msgs (e.g. default), needs testing
        return LegacyComponentSerializer.legacySection().serialize(IAdventure.getInstance().asAdventure(this.kickReason));
    }
}