package com.github.sirblobman.combatlogx;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.configuration.MainConfiguration;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import com.github.sirblobman.combatlogx.command.AfkCommand;
import com.github.sirblobman.combatlogx.command.AntiLogoutCommand;
import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CombatLogX implements ICombatLogX, DedicatedServerModInitializer {
    public static final String MOD_ID = "antilogout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MainConfiguration.ConfigReader CONFIG_READER = new MainConfiguration.ConfigReader(MainConfiguration.createToml(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "config", MainConfiguration.class));
    public static final Component AFK_MESSAGE;
    static {
        // config = OldConfiguration.readConfigFile();

        AFK_MESSAGE = Component.translatable(CONFIG_READER.config().afk.afkMessage);
    }

    @Override
    public void onInitializeServer() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {});
        AttackEntityCallback.EVENT.register(DamageEventListener::onAttack);
        ServerLivingEntityEvents.AFTER_DEATH.register(DamageEventListener::onDeath);
        ServerPlayConnectionEvents.JOIN.register(DamageEventListener::onPlayerJoin);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AfkCommand.register(dispatcher);
            AntiLogoutCommand.register(dispatcher);
        });

        LOGGER.info("AntiLogout initialized.");
    }

    // TODO :: gotta make something to call onReload

    // FIXME :: add a thread safe singleton logger thingy
    public static void debugInfo(String msg) {
        if (isDebugMode()) LOGGER.info(msg);
    }

    /**
     * @return The main configuration
     */
    @Override
    public @NotNull MainConfiguration getConfiguration() {
        return CONFIG_READER.config();
    }
}