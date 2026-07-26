package com.github.sirblobman.combatlogx.api.expansion;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;

public final class ExpansionFactoryImpl implements ExpansionFactory {

    private final ExpansionInitializer initializer;
    private final ExpansionMetadata description;

    public ExpansionFactoryImpl(ExpansionInitializer initializer, ExpansionMetadata description) {
        this.initializer = initializer;
        this.description = description;
    }

    @Override
    public @NotNull Expansion create(ICombatLogX api) {
        return this.initializer.onInitializeExpansion(api, this.description);
    }
}
