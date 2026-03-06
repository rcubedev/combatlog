package com.github.sirblobman.combatlogx.api.event;

import net.minecraft.world.entity.player.Player;

import com.github.rcubedev.example.event.api.Event;
import com.github.rcubedev.example.event.api.EventHandler;
import org.jetbrains.annotations.NotNull;

public abstract class CustomPlayerEvent extends Event {

    protected Player player;

    public CustomPlayerEvent(@NotNull Player player) {
        this.player = player;
    }

    /**
     * {@inheritDoc}
     *
     * @return The handler instance.
     */
    @Override
    public abstract EventHandler<? extends CustomPlayerEvent> handler();

    /**
     * Returns the player involved in this event
     *
     * @return the player who is involved in this event
     */
    @NotNull
    public final Player getPlayer() {
        return player;
    }
}
