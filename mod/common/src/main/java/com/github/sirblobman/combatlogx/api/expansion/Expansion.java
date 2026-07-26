package com.github.sirblobman.combatlogx.api.expansion;

import net.minecraft.server.MinecraftServer;import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.slf4j.Logger;

public abstract class Expansion {
    private final ICombatLogX mod;
    private final ExpansionMetadata description;
    private volatile State state = State.UNLOADED;

    /**
     * Constructs a new {@link Expansion} instance.
     *
     * @implSpec Basic initialisation may be performed in the constructor.
     * Work that depends on the CombatLogX lifecycle or other expansions being
     * available should be performed in {@link #onLoad()} or later lifecycle methods.
     *
     * @param mod the parent {@link ICombatLogX} instance managing this expansion
     * @param metadata metadata describing this expansion
     */
    public Expansion(@NotNull ICombatLogX mod, @NotNull ExpansionMetadata metadata) {
        this.mod = mod;
        this.description = metadata;
    }

    /**
     * Gets the current lifecycle state of this expansion.
     *
     * @return the current {@link State} of this expansion
     */
    public final @NotNull State getState() {
        return this.state;
    }

    final void setState(@NotNull State state) {
        this.state = state;
    }

    /**
     * Gets the main mod instance.
     *
     * @return the {@link ICombatLogX} manager instance
     */
    public final @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    /**
     * Gets the metadata for this expansion.
     *
     * @return the {@link ExpansionMetadata} container
     */
    public final @NotNull ExpansionMetadata getDescription() {
        return this.description;
    }

    /**
     * Gets the ID of the expansion.
     *
     * @return the ID defined in {@link ExpansionMetadata#getId()}
     */
    public final @NotNull String getId() {
        ExpansionMetadata description = getDescription();
        return description.getId();
    }

    /**
     * Gets the {@link Logger} for this expansion.
     *
     * @return the {@link Logger}
     */
    public abstract @NotNull Logger getLogger();

    /**
     * Called during mod initialisation phase when the expansion is first loaded
     */
    public abstract void onLoad();

    /**
     * Called when the expansion is enabled, during or after server startup
     *
     * @param server the active {@link MinecraftServer} instance
     */
    public abstract void onEnable(@NotNull MinecraftServer server);

    /**
     * Called when the expansion is being shut down or disabled.
     *
     * @param server the active {@link MinecraftServer} during teardown
     * @apiNote Guaranteed to run only after {@link #onLoad()} completes.<br>
     *          May be called without {@link #onEnable(MinecraftServer)} ever running.
     */
    public abstract void onDisable(@NotNull MinecraftServer server);

    /**
     * Called when a configuration reload request is dispatched to this expansion.
     */
    public abstract void reloadConfig();

    // todo maybe add self disable like parity?
}
