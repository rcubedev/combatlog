package com.github.sirblobman.combatlogx.api.mod;

import java.util.Locale;

import com.github.sirblobman.combatlogx.api.ILoggingProvider;
import com.github.sirblobman.combatlogx.api.configuration.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigurableMod implements ILoggingProvider {
    // private final ConfigurationManager configurationManager;
    // private final PlayerDataManager playerDataManager;

    public ConfigurableMod() {
        // this.configurationManager = new ConfigurationManager(this);
        // this.playerDataManager = new PlayerDataManager(this);
    }

    public abstract void onInitializeServer();

    public @NotNull ConfigurableMod getMod() {
        return this;
    }

    protected void reloadConfiguration() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");
    }

    public final @NotNull ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    @Override
    public boolean isDebugMode() {
        if (ILoggingProvider.super.isDebugMode()) return true;
        return configuration.getBoolean("debug-mode", false);
    }
}