package com.github.sirblobman.combatlogx.command.combatlogx;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.command.CommandProperties;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import org.jetbrains.annotations.NotNull;

public abstract class Command extends com.github.sirblobman.combatlogx.api.command.Command {

    private final @NotNull ICombatLogX mod;

    public Command(@NotNull ICombatLogX mod, @NotNull CommandProperties commandProperties) {
        super(commandProperties, mod.getLogger());
        this.mod = mod;
    }

    protected final @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    protected final @NotNull LanguageManager<LanguageFileConfiguration> getLanguageManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getLanguageManager();
    }
}
