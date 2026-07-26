package combatlogx.expansion.actionbar;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PlayerData;
import com.github.sirblobman.combatlogx.api.configuration.PlayerDataManager;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.object.TimerUpdater;

import combatlogx.expansion.actionbar.configuration.ActionBarConfiguration;
import combatlogx.expansion.actionbar.configuration.ActionBarLanguageFileConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class ActionBarUpdater implements TimerUpdater {

    private final @NotNull ActionBarExpansion expansion;

    public ActionBarUpdater(@NotNull ActionBarExpansion expansion) {
        this.expansion = expansion;
    }

    private ActionBarExpansion getExpansion() {
        return this.expansion;
    }

    private ActionBarConfiguration getConfiguration() {
        ActionBarExpansion expansion = getExpansion();
        return expansion.getConfiguration();
    }

    private ICombatLogX getCombatLogX() {
        ActionBarExpansion expansion = getExpansion();
        return expansion.getCombatLogX();
    }

    // fixme create own language manager instance
    private LanguageManager<ActionBarLanguageFileConfiguration> getLanguageManager() {
        ActionBarExpansion expansion = getExpansion();
        return expansion.getLanguageManager();
    }

    private PlayerDataManager getPlayerDataManager() {
        ICombatLogX combatLogX = getCombatLogX();
        return combatLogX.getPlayerDataManager();
    }

    @Override
    public void update(@NotNull ServerPlayer player, long timeLeftMillis) {
        if (isDisabled(player)) return;

        sendActionBar(player, timeLeftMillis);
    }

    @Override
    public void remove(@NotNull ServerPlayer player) {
        update(player, 0L);
    }

    private boolean isGlobalEnabled() {
        ActionBarConfiguration configuration = getConfiguration();
        return configuration.enabled;
    }

    private boolean isDisabled(ServerPlayer player) {
        if (!isGlobalEnabled()) return true;

        PlayerDataManager playerDataManager = getPlayerDataManager();
        PlayerData data = playerDataManager.get(player);
        return data.getData().getBoolean("actionbar_disabled")/*? if >=1.21.10 {*/ /*.orElse(false) *//*?}*/;
        //return !data.getBoolean("actionbar", true);
    }

    private void sendActionBar(ServerPlayer player, long timeLeftMillis) {
        LanguageManager<ActionBarLanguageFileConfiguration> languageManager = getLanguageManager();
        if (timeLeftMillis <= 0) {
            languageManager.sendActionBar(player.createCommandSourceStack(), "ended", c -> c.ended);
            return;
        }

        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        IPlaceholderManager placeholderManager = combatLogX.getPlaceholderManager();
        Component message = languageManager.getMessage(player.createCommandSourceStack(), "timer", c -> c.timer);

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

        languageManager.sendActionBar(player.createCommandSourceStack(), message);
    }

    private TextReplacementConfig getBarsReplacement(ServerPlayer player, long timeLeftMillis) {
        TextReplacementConfig.Builder builder = TextReplacementConfig.builder();
        builder.matchLiteral("{bars}");
        builder.replacement(getBars(player, timeLeftMillis));
        return builder.build();
    }

    private Component getBars(ServerPlayer player, long timeLeftMillis) {
        ActionBarConfiguration configuration = getConfiguration();
        long scale = configuration.scale;

        //todo
        MiniMessage miniMessage = MiniMessage.miniMessage();
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