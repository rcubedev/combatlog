package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.github.rcubedev.utils.event.api.Cancellable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

// todo not a bukkit event
/**
 * Called when the player disconnects.<br>
 * (kicked or presses disconnect)
 * <p>
 * <ul>When cancelled:
 * <li>the players body will remain online but the client will be disconnected.
 * <li>{@link PlayerQuitEvent} will not fire.
 * </ul>
 * In the event the player relogs whilst the NPC exists, {@link PlayerNPCReplaceEvent} will fire.
 */
public class PlayerDisconnectEvent extends PlayerEvent implements Cancellable {

    private volatile boolean cancelled;
    private final ServerGamePacketListenerImpl packetListener;
    private final Component disconnectReason;

    @ApiStatus.Internal
    public PlayerDisconnectEvent(@NotNull ServerPlayer player, @NotNull ServerGamePacketListenerImpl packetListener, @NotNull Component disconnectReason) {
        super(player);
        this.packetListener = packetListener;
        this.disconnectReason = disconnectReason;
    }

    /**
     * Returns the {@link ServerGamePacketListenerImpl}.
     *
     * @return disconnect reason
     */
    public @NotNull ServerGamePacketListenerImpl getPacketListener() {
        return this.packetListener;
    }

    /**
     * Gets the reason why the player disconnected.
     *
     * @return disconnect reason
     */
    public @NotNull Component getDisconnectReason() {
        return this.disconnectReason;
    }

    /**
     * Check if event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancel the event. Cannot be undone
     */
    @Override
    public void cancel() {
        this.cancelled = true;
    }
}
