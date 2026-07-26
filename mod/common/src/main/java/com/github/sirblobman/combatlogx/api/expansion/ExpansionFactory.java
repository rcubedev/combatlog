package com.github.sirblobman.combatlogx.api.expansion;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ExpansionFactory {

    @NotNull Expansion create(ICombatLogX api);
}
