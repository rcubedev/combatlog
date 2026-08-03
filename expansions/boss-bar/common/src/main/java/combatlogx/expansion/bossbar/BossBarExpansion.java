package combatlogx.expansion.bossbar;

import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.LanguageConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import combatlogx.expansion.bossbar.configuration.BossBarConfiguration;
import combatlogx.expansion.bossbar.configuration.BossBarLanguageFileConfiguration;
import folk.sisby.kaleido.api.KaleidoConfig;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BossBarExpansion extends Expansion {

    public static final String MOD_ID = "clx_bossbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final BossBarConfiguration configuration = BossBarConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX-BossBar", "config", BossBarConfiguration.class);
    private final LanguageConfiguration languageConfiguration = LanguageConfiguration.createToml(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX-BossBar", "language", LanguageConfiguration.class);

    private final LanguageManager<BossBarLanguageFileConfiguration> languageManager = new LanguageManager<>(
            getCombatLogX(), BossBarLanguageFileConfiguration.class, languageConfiguration,
            "prefix",c -> "FIXME?"); // FIXME

    public BossBarExpansion(@NotNull ICombatLogX mod, @NotNull ExpansionMetadata metadata) {
        super(mod, metadata);
    }

    @Override
    public @NotNull Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void onLoad() {
        LanguageManager<BossBarLanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.loadDefaultLanguageFiles(IPlatformHelper.getInstance().getConfigDir(), "CombatLogX-BossBar", "language");
    }

    @Override
    public void onEnable(@NotNull MinecraftServer server) {
        ICombatLogX combatLogX = getCombatLogX();

        LanguageManager<BossBarLanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.onEnable();

        reloadConfig();

        ITimerManager timerManager = combatLogX.getTimerManager();
        timerManager.addUpdaterTask(new BossBarUpdater(this));
    }

    @Override
    public void onDisable(@NotNull MinecraftServer server) {}

    @Override
    public void reloadConfig() {
        com.github.sirblobman.combatlogx.CombatLogX.reload(getConfiguration());
        reloadLanguage();
    }

    private void reloadLanguage() {
        LanguageManager<BossBarLanguageFileConfiguration> languageManager = getLanguageManager();
        languageManager.reloadLanguages();
    }

    public @NotNull BossBarConfiguration getConfiguration() {
        return this.configuration;
    }

    public @NotNull LanguageManager<BossBarLanguageFileConfiguration> getLanguageManager() {
        return this.languageManager;
    }
}
