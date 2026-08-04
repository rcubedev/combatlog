package combatlogx.expansion.bossbar;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.github.rcubedev.example.platform.IAdventure;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import combatlogx.expansion.bossbar.configuration.BossBarConfiguration;
import combatlogx.expansion.bossbar.configuration.BossBarLanguageFileConfiguration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.object.TimerUpdater;
import org.jetbrains.annotations.Nullable;

public final class BossBarUpdater implements TimerUpdater {

    private final @NotNull BossBarExpansion expansion;
    private final Map<UUID, BossBar> bossBarMap = new ConcurrentHashMap<>();;

    public BossBarUpdater(BossBarExpansion expansion) {
        this.expansion = expansion;
    }

    @Override
    public void update(@NotNull ServerPlayer player, long timeLeftMillis) {
        if (isDisabled(player)) {
            actualRemove(player);
            return;
        }

        Component title = getTitle(player, timeLeftMillis);
        if (Component.empty().equals(title)) {
            actualRemove(player);
            return;
        }

        float progress = getProgress(player, timeLeftMillis);
        BossBar.Color color = getBossBarColor();
        BossBar.Overlay overlay = getBossBarOverlay();

        BossBar bossBar = getBossBar(player, true);
        bossBar.progress(progress);
        bossBar.color(color);
        bossBar.overlay(overlay);
        bossBar.name(title);

        Audience audience = getAudience(player);
        audience.showBossBar(bossBar);
    }

    @Override
    public void remove(@NotNull ServerPlayer player) {
        update(player, 0L);

        ICombatLogX combatLogX = getCombatLogX();

        actualRemove(player);
        // TaskScheduler scheduler = combatLogX.getScheduler();
        // scheduler.schedule(new RunnableTaskDetails(combatLogX, TaskType.START_TICK, 1L, () -> actualRemove(player)));
    }

    private BossBarExpansion getExpansion() {
        return this.expansion;
    }

    private ICombatLogX getCombatLogX() {
        BossBarExpansion expansion = getExpansion();
        return expansion.getCombatLogX();
    }

    private LanguageManager<BossBarLanguageFileConfiguration> getLanguageManager() {
        BossBarExpansion expansion = getExpansion();
        return expansion.getLanguageManager();
    }

    private PlayerDataManager getPlayerDataManager() {
        ICombatLogX combatLogX = getCombatLogX();
        return combatLogX.getPlayerDataManager();
    }

    private ICombatManager getCombatManager() {
        ICombatLogX combatLogX = getCombatLogX();
        return combatLogX.getCombatManager();
    }

    private boolean isGlobalEnabled() {
        BossBarConfiguration configuration = getConfiguration();
        return configuration.enabled;
    }

    private boolean isDisabled(ServerPlayer player) {
        if (!isGlobalEnabled()) return true;

        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData data = playerDataManager.get(player);
        return data.getData().getBoolean("bossbar_disabled")/*? if >=1.21.10 {*/ /*.orElse(false) *//*?}*/;
    }

    @Contract("_, true -> !null")
    private @Nullable BossBar getBossBar(ServerPlayer player, boolean create) {
        UUID playerId = player.getUUID();
        if (this.bossBarMap.containsKey(playerId)) return this.bossBarMap.get(playerId);

        if (!create) return null;

        Component defaultTitle = Component.text("Default Title");
        BossBar defaultBossBar = BossBar.bossBar(defaultTitle, 1.0F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        this.bossBarMap.put(playerId, defaultBossBar);
        return defaultBossBar;
    }

    private Audience getAudience(ServerPlayer player) {
        return IAdventure.getInstance().audience(player);
    }

    private void actualRemove(ServerPlayer player) {
        BossBar bossBar = getBossBar(player, false);
        if (bossBar == null) return;

        Audience audience = getAudience(player);
        audience.hideBossBar(bossBar);

        UUID playerId = player.getUUID();
        this.bossBarMap.remove(playerId);
    }

    private BossBar.Color getBossBarColor() {
//        int majorVersion = VersionUtility.getMajorVersion();
//        int minorVersion = VersionUtility.getMinorVersion();
//        if (majorVersion == 1 && minorVersion < 9) return BossBar.Color.PURPLE;

        BossBarConfiguration configuration = getConfiguration();
        return configuration.color.getColor();
    }

    private BossBar.Overlay getBossBarOverlay() {
//        int minorVersion = VersionUtility.getMinorVersion();
//        if (minorVersion < 9) return BossBar.Overlay.PROGRESS;

        BossBarConfiguration configuration = getConfiguration();
        return configuration.style.getOverlay();
    }

    private float getProgress(ServerPlayer player, float timeLeftMillis) {
        ICombatManager combatManager = getCombatManager();
        long timerMaxSeconds = combatManager.getMaxTimerSeconds(player);
        float timerMaxMillis = TimeUnit.SECONDS.toMillis(timerMaxSeconds);

        float barPercentage = (timeLeftMillis / timerMaxMillis);
        if (barPercentage <= 0.0F) return 0.0F;

        return Math.min(barPercentage, 1.0F);
    }

    private Component getTitle(ServerPlayer player, long timeLeftMillis) {
        LanguageManager<BossBarLanguageFileConfiguration> languageManager = getLanguageManager();
        if (timeLeftMillis <= 0) {
            return languageManager.getMessage(player.createCommandSourceStack(), "expansion.boss-bar.ended", c -> c.ended);
        }

        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        IPlaceholderManager placeholderManager = combatLogX.getPlaceholderManager();
        Component message = languageManager.getMessage(player.createCommandSourceStack(), "expansion.boss-bar.timer", c -> c.timer);

        TextReplacementConfig replacementConfig = getBarsReplacement(player, timeLeftMillis);
        message = message.replaceText(replacementConfig);

        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation != null) {
            List<Entity> enemyList = tagInformation.getEnemies();
            Pattern placeholderPattern = Pattern.compile("\\{(\\S+)}");
            TextReplacementConfig.Builder builder = TextReplacementConfig.builder();
            builder.match(placeholderPattern);
            builder.replacement((matchResult, builderCopy) -> {
                String placeholder = matchResult.group(1);
                Component replacement = placeholderManager.getPlaceholderReplacementComponent(player,
                        enemyList, placeholder);
                return (replacement == null ? Component.text(placeholder) : replacement);
            });

            TextReplacementConfig replacement = builder.build();
            message = message.replaceText(replacement);
        }

        return message;
    }

    private TextReplacementConfig getBarsReplacement(ServerPlayer player, long timeLeftMillis) {
        TextReplacementConfig.Builder builder = TextReplacementConfig.builder();
        builder.matchLiteral("{bars}");
        builder.replacement(getBars(player, timeLeftMillis));
        return builder.build();
    }

    private BossBarConfiguration getConfiguration() {
        BossBarExpansion expansion = getExpansion();
        return expansion.getConfiguration();
    }

    private Component getBars(ServerPlayer player, long timeLeftMillis) {
        BossBarConfiguration configuration = getConfiguration();
        long scale = configuration.scale;

        //todo
        MiniMessage miniMessage = getLanguageManager().getMiniMessage();
        Component leftSymbol = configuration.getLeftSymbol(miniMessage);
        Component rightSymbol = configuration.getRightSymbol(miniMessage);

        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = plugin.getCombatManager();
        long timerMaxSeconds = combatManager.getMaxTimerSeconds(player);

        double timerMaxMillis = TimeUnit.SECONDS.toMillis(timerMaxSeconds);
        double scaleDouble = (double) scale;
        double timeLeftMillisDouble = (double) timeLeftMillis;

        double percent = clamp(timeLeftMillisDouble / timerMaxMillis);
        long leftBarsCount = Math.round(scaleDouble * percent);
        long rightBarsCount = (scale - leftBarsCount);

        TextComponent.Builder builder = Component.text();

        for (long i = 0; i < leftBarsCount; i++) {
            builder.append(leftSymbol);
        }

        for (long i = 0; i < rightBarsCount; i++) {
            builder.append(rightSymbol);
        }

        return builder.build();
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(value, 1.0D));
    }
}