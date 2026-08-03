package com.github.sirblobman.combatlogx.api.listener;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Locale;

public abstract class CombatListener {
    private final ICombatLogX mod;

    public CombatListener(@NotNull ICombatLogX mod) {
        this.mod = mod;
    }

    /*public @NotNull Subscription register() {
        ICombatLogX combatLogX = getCombatLogX();
        IEventBus<Event> bus = combatLogX.getEventBus();
        Identity id = Identity.of(MethodHandles.lookup());

        return bus.register(this, id);
    }*/

    protected final @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    public @NotNull Logger getLogger() {
        ICombatLogX mod = getCombatLogX();
        return mod.getLogger();
    }

    protected final @NotNull LanguageManager<LanguageFileConfiguration> getLanguageManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getLanguageManager();
    }

    protected final @NotNull PlayerDataManager getPlayerDataManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getPlayerDataManager();
    }

    protected final @NotNull ICombatManager getCombatManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getCombatManager();
    }

    protected final @NotNull IDeathManager getDeathManager() {
        ICombatLogX mod = getCombatLogX();
        return mod.getDeathManager();
    }

    protected final boolean isInCombat(@NotNull ServerPlayer player) {
        ICombatManager combatManager = getCombatManager();
        return combatManager.isInCombat(player);
    }

    protected final boolean isDebugMode() {
        ICombatLogX mod = getCombatLogX();
        return mod.isDebugMode();
    }

    protected void printDebug(@NotNull String message) {
        if (!isDebugMode()) return;

        Class<?> thisClass = getClass();
        String className = thisClass.getSimpleName();
        String logMessage = String.format(Locale.US, "[Debug] [%s] %s", className, message);

        Logger modLogger = getLogger();
        modLogger.info(logMessage);
    }
}
