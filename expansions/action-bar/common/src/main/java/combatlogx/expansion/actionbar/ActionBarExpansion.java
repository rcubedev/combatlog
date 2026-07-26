package combatlogx.expansion.actionbar;

import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.LanguageConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import combatlogx.expansion.actionbar.configuration.ActionBarConfiguration;
import combatlogx.expansion.actionbar.configuration.ActionBarLanguageFileConfiguration;
import folk.sisby.kaleido.api.KaleidoConfig;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ActionBarExpansion extends Expansion {

    public static final String MOD_ID = "clx_actionbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final ActionBarConfiguration configuration = ActionBarConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX-ActionBar", "config", ActionBarConfiguration.class);
    private final LanguageConfiguration languageConfiguration = LanguageConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX-ActionBar", "language", LanguageConfiguration.class);

    private final LanguageManager<ActionBarLanguageFileConfiguration> languageManager = new LanguageManager<>(
            getCombatLogX(), ActionBarLanguageFileConfiguration.class, languageConfiguration,
            "prefix",c -> c.prefix);

    public ActionBarExpansion(@NotNull ICombatLogX mod, @NotNull ExpansionMetadata metadata) {
        super(mod, metadata);
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void onLoad() {}

    @Override
    public void onEnable(@NotNull MinecraftServer server) {
        ICombatLogX combatLogX = getCombatLogX();

        reloadConfig();
        ITimerManager timerManager = combatLogX.getTimerManager();
        timerManager.addUpdaterTask(new ActionBarUpdater(this));
    }

    @Override
    public void onDisable(@NotNull MinecraftServer server) {}

    @Override
    public void reloadConfig() {
        com.github.sirblobman.combatlogx.CombatLogX.reload(getConfiguration());
        reloadLanguage();
    }

    private void reloadLanguage() {
        LanguageManager<ActionBarLanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.reloadLanguages();
    }

    public @NotNull ActionBarConfiguration getConfiguration() {
        return this.configuration;
    }

    public @NotNull LanguageManager<ActionBarLanguageFileConfiguration> getLanguageManager() {
        return this.languageManager;
    }
}
