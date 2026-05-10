package com.github.sirblobman.combatlogx;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.github.rcubedev.example.event.api.Event;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import com.github.rcubedev.example.event.buses.MainBus;
import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.FabricTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.FabricEventHook;
import com.github.sirblobman.combatlogx.api.configuration.CommandConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IForgiveManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import com.github.sirblobman.combatlogx.listener.ConfigurationListener;
import com.github.sirblobman.combatlogx.listener.DeathListener;
import com.github.sirblobman.combatlogx.listener.PunishListener;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import com.github.sirblobman.combatlogx.manager.CombatManager;
import com.github.sirblobman.combatlogx.manager.CrystalManager;
import com.github.sirblobman.combatlogx.manager.DeathManager;
import com.github.sirblobman.combatlogx.manager.ForgiveManager;
import com.github.sirblobman.combatlogx.manager.PlaceholderManager;
import com.github.sirblobman.combatlogx.manager.PunishManager;
import com.github.sirblobman.combatlogx.mixin.WrappedConfigAccessor;
import com.github.sirblobman.combatlogx.placeholder.BasePlaceholderExpansion;
import com.github.sirblobman.combatlogx.task.TimerUpdateTask;
import com.github.sirblobman.combatlogx.task.UntagTask;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.ConfigImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.builders.ConfigBuilderImpl;
import net.fabricmc.api.DedicatedServerModInitializer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CombatLogX implements ICombatLogX, DedicatedServerModInitializer {
    public static ICombatLogX INSTANCE; // todo
    public static final String MOD_ID = "antilogout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final MainConfiguration configuration = MainConfiguration.createToml(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "config", MainConfiguration.class);
    private final CommandConfiguration commandConfiguration = CommandConfiguration.createToml(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "commands", CommandConfiguration.class);
    private final PunishConfiguration punishConfiguration = PunishConfiguration.createToml(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "punish", PunishConfiguration.class);
    private final LanguageConfiguration languageConfiguration = LanguageConfiguration.createToml(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "language", LanguageConfiguration.class);
    private final TimerUpdateTask timerUpdateTask = new TimerUpdateTask(this);
    private final PlayerDataManager playerDataManager = new PlayerDataManager(this);
    private final LanguageManager<LanguageFileConfiguration> languageManager = new LanguageManager<>(this, LanguageFileConfiguration.class, languageConfiguration, "prefix", c -> c.prefix);
    private final CombatManager combatManager = new CombatManager(this);
    private final PunishManager punishManager = new PunishManager(this);
    // private final ExpansionManager expansionManager = new ExpansionManager(this);
    private final PlaceholderManager placeholderManager = new PlaceholderManager(this);
    private final DeathManager deathManager = new DeathManager(this);
    private final ForgiveManager forgiveManager = new ForgiveManager(this);
    private final CrystalManager crystalManager = new CrystalManager(this);

    @Override
    public void onInitializeServer() {
        INSTANCE = this;

        registerCommands();
        registerListeners();
        registerTasks();
        registerBasePlaceholders();

        LanguageManager<LanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.onInitialize();
        languageManager.loadDefaultLanguageFiles(FabricLoader.getInstance().getConfigDir(), "CombatLogX", "language");
        languageManager.reloadLanguages();

        // ServerLifecycleEvents.SERVER_STARTING.register(server -> audiences = MinecraftServerAudiences.of(server));
        // ServerLifecycleEvents.SERVER_STOPPED.register(server -> audiences = null);

        // ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {});
        // AttackEntityCallback.EVENT.register(DamageEventListener::onAttack);
        // ServerLivingEntityEvents.AFTER_DEATH.register(DamageEventListener::onDeath);
        // ServerPlayConnectionEvents.JOIN.register(DamageEventListener::onPlayerJoin);


        // CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
        //     AfkCommand.register(dispatcher);
        //     AntiLogoutCommand.register(dispatcher);
        // });

        LOGGER.info("AntiLogout initialized.");
    }

    // todo: gotta make something to call onReload
    // fixme: add a thread safe singleton logger thingy

    @Override
    public void onReload() {
        // todo
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public @NotNull PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    @Override
    public @NotNull LanguageManager<LanguageFileConfiguration> getLanguageManager() {
        return this.languageManager;
    }

    @Override
    public @NotNull ICombatManager getCombatManager() {
        return this.combatManager;
    }

    @Override
    public @NotNull IPunishManager getPunishManager() {
        return this.punishManager;
    }

    @Override
    public @NotNull ITimerManager getTimerManager() {
        return this.timerUpdateTask;
    }

    @Override
    public @NotNull IDeathManager getDeathManager() {
        return this.deathManager;
    }

    @Override
    public @NotNull IPlaceholderManager getPlaceholderManager() {
        return this.placeholderManager;
    }

    @Override
    public @NotNull IForgiveManager getForgiveManager() {
        return this.forgiveManager;
    }

    @Override
    public @NotNull TaskScheduler getScheduler() {
        return FabricTaskScheduler.getScheduler(); //fixme
    }

    @Override
    public @NotNull MainConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * @return The command configuration
     */
    @Override
    public @NotNull CommandConfiguration getCommandConfiguration() {
        return this.commandConfiguration;
    }

    /**
     * @return The punishment configuration
     */
    @Override
    public @NotNull PunishConfiguration getPunishConfiguration() {
        return this.punishConfiguration;
    }

    @Override
    public @NotNull ICrystalManager getCrystalManager() {
        return this.crystalManager;
    }

    public static <T extends WrappedConfig> void reload(T config) {
        try {
            Config wrapped = ((WrappedConfigAccessor) config).al$getWrapped();
            if (!(wrapped instanceof ConfigImpl wrappedImpl)) throw new IllegalStateException("Unexpected config type: " + wrapped.getClass().getName());
            ConfigBuilderImpl.doInitialSerialization(wrappedImpl);
        } catch (Throwable t) {
            LOGGER.error("An error occurred while reloading a config.", t);
        }
    }

    // fixme jank. see QuiltMC/quilt-config#62
    public static <T extends WrappedConfig> int reload(T config, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            Config wrapped = ((WrappedConfigAccessor) config).al$getWrapped();
            if (!(wrapped instanceof ConfigImpl wrappedImpl)) throw new IllegalStateException("Unexpected config type: " + wrapped.getClass().getName());
            ConfigBuilderImpl.doInitialSerialization(wrappedImpl);
            context.getSource().sendSuccess(() -> Component.literal("Reloaded config."), false);
            return 1;
        } catch (Throwable t) {
            if (t instanceof CommandSyntaxException commandSyntaxException) throw commandSyntaxException;
            LOGGER.error("An error occurred while reloading a config.", t);
            context.getSource().sendFailure(Component.literal("An error occurred while reloading."));
            return 0;
        }
    }

    // todo maybe cache?
    public static FabricServerAudiences createAudiences(@NotNull MinecraftServer server) {
        return FabricServerAudiences.of(server);
    }

    public static FabricServerAudiences createAudiences(@NotNull ServerPlayer player) {
        return createAudiences(player.server);
        // return Objects.requireNonNull(audiences, "server hasn't initialized yet");
    }

    private void registerCommands() {
        // todo
    }

    private void registerListeners() {
        // fixme todo
        ServerTickEvents.START_SERVER_TICK.register(server -> FabricTaskScheduler.getScheduler().fireTasks(TaskType.START_TICK, TickContext.ofServer(server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> FabricTaskScheduler.getScheduler().fireTasks(TaskType.END_TICK, TickContext.ofServer(server)));

        new FabricEventHook().register();
        MainBus.BUS.register(new ConfigurationListener(this));
        MainBus.BUS.register(new DamageEventListener(this));
        MainBus.BUS.register(new PunishListener(this));
        MainBus.BUS.register(new UntagEventListener(this));
        MainBus.BUS.register(new DeathListener(this));
        // MainBus.BUS.register(new InvulnerableListener(this)); todo bring back potentially
    }

    private void registerTasks() {
        ITimerManager timerManager = getTimerManager();
        timerManager.register();
        new UntagTask(this).register();
    }

    private void registerBasePlaceholders() {
        BasePlaceholderExpansion placeholderExpansion = new BasePlaceholderExpansion(this);
        IPlaceholderManager placeholderManager = getPlaceholderManager();
        placeholderManager.registerPlaceholderExpansion(placeholderExpansion);
    }
}