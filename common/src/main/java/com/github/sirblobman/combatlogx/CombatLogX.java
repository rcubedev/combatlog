package com.github.sirblobman.combatlogx;

import com.github.rcubedev.example.event.api.Identity;
import com.github.rcubedev.example.event.api.buses.MainBus;
import com.github.rcubedev.example.config.WrappedConfigAccessor;
import com.github.rcubedev.example.platform.IConfigurationHandler;
import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.rcubedev.example.services.api.ServiceBootstrap;
import com.github.rcubedev.example.services.api.ServiceRegistry;
import com.github.rcubedev.example.services.impl.layer.ClassLoaderServiceLayer;
import com.github.rcubedev.example.services.impl.layer.ModuleLayerServiceLayer;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
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
import com.github.sirblobman.combatlogx.placeholder.BasePlaceholderExpansion;
import com.github.sirblobman.combatlogx.task.TimerUpdateTask;
import com.github.sirblobman.combatlogx.task.UntagTask;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.ConfigImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.builders.ConfigBuilderImpl;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class CombatLogX implements ICombatLogX {
    public static volatile ICombatLogX INSTANCE; // todo
    // fixme uses internal & breaks on fabric as fabric does not have modulelayers
    public static final ServiceRegistry SERVICE_REGISTRY = ServiceBootstrap.bootstrap(
            CombatLogX.class.getModule().getLayer() != null ?
                new ModuleLayerServiceLayer("mod", CombatLogX.class.getModule().getLayer(), 100)
            : new ClassLoaderServiceLayer("mod", CombatLogX.class.getClassLoader(), 100)
            );
    public static final String MOD_ID = "antilogout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final MainConfiguration configuration = MainConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "config", MainConfiguration.class);
    private final CommandConfiguration commandConfiguration = CommandConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "commands", CommandConfiguration.class);
    private final PunishConfiguration punishConfiguration = PunishConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "punish", PunishConfiguration.class);
    private final LanguageConfiguration languageConfiguration = LanguageConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "language", LanguageConfiguration.class);
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
    // relies on config, must be late init
    private final PermissionHolder permissionHolder = new PermissionHolder(this);

    public void onInitializeServer() {
        INSTANCE = this;

        registerCommands();
        registerListeners();
        registerTasks();
        registerBasePlaceholders();

        LanguageManager<LanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.onInitialize();
        languageManager.loadDefaultLanguageFiles(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "language");
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

        LOGGER.info("AntiLogout initialized. TCL: {}. CL: {}", Thread.currentThread().getContextClassLoader(), CombatLogX.class.getClassLoader());
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
    public @NotNull PermissionHolder getPermissionHolder() {
        return this.permissionHolder;
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

    //fixme
    @Override
    public @NotNull TaskScheduler getScheduler() {
        return ModdedTaskScheduler.getScheduler();
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

    // fixme jank. in future use varhandle or methodhandle getter
    public static <T extends WrappedConfig> WrappedConfigAccessor create(T config) {
        return IConfigurationHandler.getInstance().getAccessor(config);
//        if (!(config instanceof folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig))
//            throw new IllegalStateException("Kaleido WrappedConfig does not extend QuiltConfig Wrapped Config.");
//        Config wrapped;
//        try {
//            Field field = folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig.class.getDeclaredField("wrapped");
//            field.trySetAccessible();
//            wrapped = (Config) field.get(config);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException("Failed to create WrappedConfigAccessor", e);
//        }
//        return new WrappedConfigAccessor() {
//            @Override
//            public Config test$getWrapped() {
//                return wrapped;
//            }
//        };
    }

    // fixme just pass a WrappedConfigAccessor
    public static void reload(WrappedConfigAccessor config) {
        try {
            Config wrapped = config.test$getWrapped();
            if (!(wrapped instanceof ConfigImpl wrappedImpl)) throw new IllegalStateException("Unexpected config type: " + wrapped.getClass().getName());
            ConfigBuilderImpl.doInitialSerialization(wrappedImpl);
        } catch (Throwable t) {
            LOGGER.error("An error occurred while reloading a config.", t);
        }
    }

    // fixme jank. see QuiltMC/quilt-config#62
    public static int reload(WrappedConfigAccessor config, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            Config wrapped = config.test$getWrapped();
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

    private void registerCommands() {
        // todo
    }

    private void registerListeners() {
        Identity id = Identity.of(MethodHandles.lookup());
        MainBus.BUS.register(new ConfigurationListener(this), id);
        MainBus.BUS.register(new DamageEventListener(this), id);
        MainBus.BUS.register(new PunishListener(this), id);
        MainBus.BUS.register(new UntagEventListener(this), id);
        MainBus.BUS.register(new DeathListener(this), id);
        // MainBus.BUS.register(new InvulnerableListener(this), id); todo bring back potentially
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