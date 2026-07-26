package com.github.sirblobman.combatlogx.api.expansion;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;

/**
 * CombatLogX expansion entrypoint.
 * <p>
 * Implementations are discovered by the current platform and invoked to
 * create an {@link Expansion} during CombatLogX startup.
 *
 * <p>
 * Discovery is platform-specific.
 * <p>
 * <b>Fabric:</b> In {@code fabric.mod.json}, the entrypoint is defined with the {@code combatlogx} key.
 * <p>
 * <b>NeoForge:</b> Implement this interface. CombatLogX automatically discovers implementations during mod loading.
 * Implementing classes must list this interface directly in their {@code implements} clause.<br>
 * Alternatively, use {@code @CLXExpansion}
 */
@FunctionalInterface
public interface ExpansionInitializer {

    String ENTRYPOINT_KEY = "combatlogx";

    @NotNull Expansion onInitializeExpansion(@NotNull ICombatLogX api, @NotNull ExpansionMetadata description);
}
