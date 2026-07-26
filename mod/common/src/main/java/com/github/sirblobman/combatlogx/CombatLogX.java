package com.github.sirblobman.combatlogx;

import com.github.rcubedev.example.event.api.Identity;
import com.github.rcubedev.example.event.api.buses.MainBus;
import com.github.rcubedev.example.config.WrappedConfigAccessor;
import com.github.rcubedev.example.event.api.spi.Subscription;
import com.github.rcubedev.example.event.server.lifecycle.ServerStartingEvent;
import com.github.rcubedev.example.event.server.lifecycle.ServerStoppingEvent;import com.github.rcubedev.example.platform.IConfigurationHandler;
import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.rcubedev.example.services.api.ServiceBootstrap;
import com.github.rcubedev.example.services.api.ServiceRegistry;
import com.github.rcubedev.example.services.impl.layer.ClassLoaderServiceLayer;
import com.github.rcubedev.example.services.impl.layer.ModuleLayerServiceLayer;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionFactory;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionManager;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import com.github.sirblobman.combatlogx.expansion.ExpansionRegistryImpl;

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
import com.github.sirblobman.combatlogx.platform.IExpansionLoader;
import com.github.sirblobman.combatlogx.task.TimerUpdateTask;
import com.github.sirblobman.combatlogx.task.UntagTask;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.ConfigImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.builders.ConfigBuilderImpl;

import com.github.sirblobman.combatlogx.listener.DamageEventListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class CombatLogX implements ICombatLogX {
    public static volatile @Nullable ICombatLogX INSTANCE; // todo needed for mixins / event hooks?

    // fixme uses internal service api as it isnt completed as of yet.
    public static final ServiceRegistry SERVICE_REGISTRY = ServiceBootstrap.bootstrap(
            CombatLogX.class.getModule().getLayer() != null ?
                new ModuleLayerServiceLayer("mod", CombatLogX.class.getModule().getLayer(), 100)
            : new ClassLoaderServiceLayer("mod", CombatLogX.class.getClassLoader(), 100));

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
    private final ExpansionManager expansionManager = new ExpansionManager(this);
    private final PlaceholderManager placeholderManager = new PlaceholderManager(this);
    private final DeathManager deathManager = new DeathManager(this);
    private final ForgiveManager forgiveManager = new ForgiveManager(this);
    private final CrystalManager crystalManager = new CrystalManager(this);

    //fixme
    private volatile @Nullable Subscription startingSubscription;
    private volatile @Nullable Subscription stoppingSubscription;

    // relies on config, must be late init
    private final PermissionHolder permissionHolder = new PermissionHolder(this);

    public void onLoad() {
        LanguageManager<LanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.loadDefaultLanguageFiles(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX", "language");
        //languageManager.reloadLanguages();

        ExpansionRegistryImpl registry = getExpansionRegistry();
        List<ExpansionFactory> modLoadedFactories = IExpansionLoader.getInstance().load();
        registry.registerExpansions(modLoadedFactories);

        //fixme
        this.startingSubscription = MainBus.BUS.register(ServerStartingEvent.class,
                e -> this.onEnable(e.getServer()), Identity.ofPublic());
        this.stoppingSubscription = MainBus.BUS.register(ServerStoppingEvent.class,
                e -> this.onDisable(e.getServer()), Identity.ofPublic());
    }

    // server starting event; neo/fabric
    public void onEnable(@NotNull MinecraftServer server) {
        Subscription sub = this.startingSubscription;
        this.startingSubscription = null;
        if (sub != null) sub.unsubscribe();

        onReload();

        LanguageManager<LanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.onEnable();

        //broadcastMessageOnLoad();

        registerCommands();
        registerListeners();
        registerTasks();
        registerExpansions(server);
        registerBasePlaceholders();

        //broadcastMessageOnEnable();

        // all loaded so should be safe to publish?
        INSTANCE = this;
    }

    public void onDisable(@NotNull MinecraftServer server) {
        Subscription sub = this.stoppingSubscription;
        this.stoppingSubscription = null;
        if (sub != null) sub.unsubscribe();
        sub = this.startingSubscription;
        this.startingSubscription = null;
        if (sub != null) sub.unsubscribe();

        untagAllPlayers(server);

        ExpansionManager expansionManager = getExpansionManager();
        expansionManager.disableExpansions(server);

        //broadcastMessageOnDisable();
    }

    // todo: gotta make something to call onReload
    @Override
    public void onReload() {
        CombatLogX.reload(getConfiguration());
        CombatLogX.reload(getCommandConfiguration());
        CombatLogX.reload(getPunishConfiguration());

        reloadLanguage();

        ExpansionManager expansionManager = getExpansionManager();
        expansionManager.reloadConfigs();
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public @NotNull ExpansionRegistryImpl getExpansionRegistry() {
        return ExpansionRegistryImpl.getInstance();
    }

    // todo stop the internal leakage
    @Override
    public @NotNull ExpansionManager getExpansionManager() {
        return this.expansionManager;
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

    // fixme again internal as task api not finished
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
    public static <T extends WrappedConfig> WrappedConfigAccessor getAccessor(T config) {
        return IConfigurationHandler.getInstance().getAccessor(config);
    }

    public static <T extends WrappedConfig> T reload(T config) {
        reload(getAccessor(config));
        return config;
    }

    // fixme jank. see QuiltMC/quilt-config#62
    private static void reload(WrappedConfigAccessor config) {
        try {
            Config wrapped = config.test$getWrapped();
            if (!(wrapped instanceof ConfigImpl wrappedImpl)) throw new IllegalStateException("Unexpected config type: " + wrapped.getClass().getName());
            ConfigBuilderImpl.doInitialSerialization(wrappedImpl);
        } catch (Throwable t) {
            LOGGER.error("An error occurred while reloading a config.", t);
        }
    }

    /*
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
    }*/

    private void reloadLanguage() {
        LanguageManager<LanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.reloadLanguages();
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

    private void registerExpansions(@NotNull MinecraftServer server) {
        ExpansionManager expansionManager = getExpansionManager();
        expansionManager.enableExpansions(server);
    }

    private void untagAllPlayers(@NotNull MinecraftServer server) {
        ICombatManager combatManager = getCombatManager();
        List<ServerPlayer> playerCombatList = combatManager.getPlayersInCombat(server);
        for (ServerPlayer player : playerCombatList) {
            combatManager.untag(player, UntagReason.EXPIRE);
        }
    }

    private void registerBasePlaceholders() {
        BasePlaceholderExpansion placeholderExpansion = new BasePlaceholderExpansion(this);
        IPlaceholderManager placeholderManager = getPlaceholderManager();
        placeholderManager.registerPlaceholderExpansion(placeholderExpansion);
    }
}