package com.github.sirblobman.combatlogx.api.bukkiteventcompat;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player changes their locale in the client settings or on creation of the {@link ServerPlayer}.<br>
 * This may happen on login, respawn, ... fixme
 * <p>
 * The {@link ServerPlayer} has not yet finished its constructor. Ensure caution.
 */
public class PlayerLocaleChangeEvent extends PlayerEvent {

    private final String locale;

    @ApiStatus.Internal
    public PlayerLocaleChangeEvent(@NotNull ServerPlayer player, @NotNull String locale) {
        super(player);
        this.locale = locale;
    }

    /**
     * @return the player's locale
     * @see ServerPlayer#language
     */
    public @NotNull String getLocale() {
        return this.locale;
    }
}
