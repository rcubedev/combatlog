package org.samo_lego.antilogout;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import net.fabricmc.loader.api.FabricLoader;
import org.samo_lego.antilogout.command.AfkCommand;
import org.samo_lego.antilogout.command.AntiLogoutCommand;
import org.samo_lego.antilogout.config.LogoutConfig;
import org.samo_lego.antilogout.listener.DamageEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AntiLogout implements DedicatedServerModInitializer {
    public static final String MOD_ID = "antilogout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final LogoutConfig config;
    public static final Component AFK_MESSAGE;

    static {
        config = LogoutConfig.readConfigFile();

        AFK_MESSAGE = Component.translatable(config.afk.afkMessage);
    }

    @Override
    public void onInitializeServer() {
        ServerLivingEntityEvents.AFTER_DAMAGE
        AttackEntityCallback.EVENT.register(DamageEventListener::onAttack);
        ServerLivingEntityEvents.AFTER_DEATH.register(DamageEventListener::onDeath);
        ServerPlayConnectionEvents.JOIN.register(DamageEventListener::onPlayerJoin);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AfkCommand.register(dispatcher);
            AntiLogoutCommand.register(dispatcher);
        });

        LOGGER.info("AntiLogout initialized.");
    }

    public static void debugInfo(String msg) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info(msg);
    }
}