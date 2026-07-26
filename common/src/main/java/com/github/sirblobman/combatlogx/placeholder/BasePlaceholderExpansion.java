package com.github.sirblobman.combatlogx.placeholder;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.rcubedev.example.platform.IAdventure;
import com.github.rcubedev.example.platform.IPlatformHelper;import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.placeholder.PlaceholderHelper;
import com.github.sirblobman.combatlogx.api.utility.Mapping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.object.CombatTag;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.placeholder.IPlaceholderExpansion;

public final class BasePlaceholderExpansion implements IPlaceholderExpansion {

    private final ICombatLogX plugin;

    public BasePlaceholderExpansion(@NotNull ICombatLogX plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull ICombatLogX getCombatLogX() {
        return this.plugin;
    }

    @Override
    public @NotNull String getId() {
        return "combatlogx";
    }

    @Override
    public @Nullable Component getReplacement(@NotNull ServerPlayer player, @NotNull List<Entity> enemyList,
                                              @NotNull String placeholder) {
        switch (placeholder) {
            case "enemy_count":
                return getEnemyCount(player);
            case "in_combat":
                return getInCombat(player);
            case "player":
                return IAdventure.getInstance().asAdventure(player.getName());
            case "punishment_count":
                return getPunishmentCount(player);
            case "status":
                return getStatus(player);
            case "tag_count":
                return getTagCount(player);
            case "time_left":
                return getTimeLeft(player);
            case "time_left_decimal":
                return getTimeLeftDecimal(player);
            default:
                break;
        }

        if (placeholder.startsWith("time_left_")) {
            if (placeholder.startsWith("time_left_decimal_")) {
                String numberString = placeholder.substring("time_left_decimal_".length());
                try {
                    int index = (Integer.parseInt(numberString) - 1);
                    return getTimeLeftDecimalSpecific(player, index);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }

            String numberString = placeholder.substring("time_left_".length());
            try {
                int index = (Integer.parseInt(numberString) - 1);
                return getTimeLeftSpecific(player, index);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        if (placeholder.startsWith("current_enemy_")) {
            Entity currentEnemy = getSpecificEnemy(enemyList, 0);
            String enemyPlaceholder = placeholder.substring("current_enemy_".length());
            return getEnemyPlaceholder(player, currentEnemy, enemyPlaceholder);
        }

        if (placeholder.startsWith("specific_enemy_")) {
            String subPlaceholder = placeholder.substring("specific_enemy_".length());
            int nextUnderscore = subPlaceholder.indexOf('_');
            if (nextUnderscore == -1) {
                return null;
            }

            int enemyIndex;
            try {
                String enemyIdString = subPlaceholder.substring(0, nextUnderscore);
                enemyIndex = (Integer.parseInt(enemyIdString) - 1);
            } catch (NumberFormatException ex) {
                return null;
            }

            Entity specificEnemy = getSpecificEnemy(enemyList, enemyIndex);
            String enemyPlaceholder = subPlaceholder.substring(nextUnderscore + 1);
            return getEnemyPlaceholder(player, specificEnemy, enemyPlaceholder);
        }

        return null;
    }

    private @Nullable Component getEnemyPlaceholder(@NotNull ServerPlayer player, @Nullable Entity enemy,
                                                    @NotNull String placeholder) {
        if (enemy == null) {
            return getUnknownEnemy(player);
        }

        switch (placeholder) {
            case "name":
                return getEnemyName(enemy);
            case "type":
                return getEnemyType(enemy);
            case "display_name":
                return getEnemyDisplayName(enemy);
            case "health":
                return getEnemyHealth(player, enemy);
            case "health_rounded":
                return getEnemyHealthRounded(enemy);
            case "hearts":
                return getEnemyHearts(enemy);
            case "hearts_count":
                return getEnemyHeartsCount(enemy);
            case "world":
                return getEnemyWorld(enemy);
            case "x":
                return getEnemyX(enemy);
            case "y":
                return getEnemyY(enemy);
            case "z":
                return getEnemyZ(enemy);
            default:
                break;
        }

        if (IPlatformHelper.getInstance().isModLoaded("placeholder-api") && enemy instanceof ServerPlayer enemyPlayer) {
            return getEnemyPlaceholderAPI(enemyPlayer, placeholder);
        }

        return null;
    }

    private @Nullable Entity getSpecificEnemy(@NotNull List<Entity> enemyList, int index) {
        if (enemyList.isEmpty()) {
            return null;
        }

        int enemyListSize = enemyList.size();
        if (index >= enemyListSize) {
            return null;
        }

        return enemyList.get(index);
    }

    private @NotNull Component getEnemyCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null) {
            return Component.text(0);
        }

        List<UUID> enemyIdList = tagInformation.getEnemyIds();
        int enemyIdListSize = enemyIdList.size();
        return Component.text(enemyIdListSize);
    }

    private @NotNull Component getInCombat(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        boolean inCombat = combatManager.isInCombat(player);

        Mapping<LanguageFileConfiguration, String> mapping = inCombat
                ? new Mapping<>("inCombat", c -> c.placeholder.status.inCombat)
                : new Mapping<>("notInCombat",     c -> c.placeholder.status.notInCombat);
        String path = "placeholder.status." + mapping.path();

        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        return languageManager.getMessage(player.createCommandSourceStack(), path, mapping.mapper()::apply);
    }

    private @NotNull Component getPunishmentCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        PunishConfiguration punishConfiguration = combatLogX.getPunishConfiguration();
        if (punishConfiguration.enablePunishmentCounter) {
            IPunishManager punishManager = combatLogX.getPunishManager();
            long punishmentCount = punishManager.getPunishmentCount(player);
            return Component.text(punishmentCount);
        }

        return Component.text(0);
    }

    private @NotNull Component getStatus(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        boolean inCombat = combatManager.isInCombat(player);

        Mapping<LanguageFileConfiguration, String> mapping = inCombat
                ? new Mapping<>("fighting", c -> c.placeholder.status.fighting)
                : new Mapping<>("idle",     c -> c.placeholder.status.idle);
        String path = "placeholder.status." + mapping.path();

        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        return languageManager.getMessage(player.createCommandSourceStack(), path, mapping.mapper()::apply);
    }

    private @NotNull Component getTagCount(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            return Component.text(0);
        }

        List<CombatTag> tagList = tagInformation.getTags();
        int tagListSize = tagList.size();
        return Component.text(tagListSize);
    }

    private @NotNull Component getTimeLeft(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        Component zero = languageManager.getMessage(player.createCommandSourceStack(), "placeholder.timeLeftZero", c -> c.placeholder.timeLeftZero);

        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            return zero;
        }

        long expireMillis = tagInformation.getExpireMillisCombined();
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        long timeLeftMillis = Math.max(0L, subtractMillis);
        if (timeLeftMillis == 0L) {
            return zero;
        }

        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis);
        if (secondsLeft <= 0L) {
            return zero;
        }

        return Component.text(secondsLeft);
    }

    private @NotNull Component getTimeLeftSpecific(@NotNull ServerPlayer player, int index) {
        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        Component zero = languageManager.getMessage(player.createCommandSourceStack(), "placeholder.timeLeftZero", c -> c.placeholder.timeLeftZero);

        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            return zero;
        }

        List<CombatTag> tagList = tagInformation.getTags();
        int tagListSize = tagList.size();
        if (index < 0 || index >= tagListSize) {
            return zero;
        }

        CombatTag combatTag = tagList.get(index);
        long expireMillis = combatTag.getExpireMillis();
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        long timeLeftMillis = Math.max(0L, subtractMillis);
        if (timeLeftMillis == 0L) {
            return zero;
        }

        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis);
        if (secondsLeft <= 0L) {
            return zero;
        }

        return Component.text(secondsLeft);
    }

    private @NotNull Component getTimeLeftDecimal(@NotNull ServerPlayer player) {
        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        Component zero = languageManager.getMessage(player.createCommandSourceStack(), "placeholder.timeLeftZero", c -> c.placeholder.timeLeftZero);

        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            return zero;
        }

        long expireMillis = tagInformation.getExpireMillisCombined();
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        double timeLeftMillis = Math.max(0.0D, subtractMillis);
        if (timeLeftMillis <= 0.0D) {
            return zero;
        }

        double secondsLeft = (timeLeftMillis / 1_000.0D);
        if (secondsLeft <= 0.0D) {
            return zero;
        }

        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player.createCommandSourceStack());
        String timeLeftString = decimalFormat.format(secondsLeft);
        return Component.text(timeLeftString);
    }

    private @NotNull Component getTimeLeftDecimalSpecific(@NotNull ServerPlayer player, int index) {
        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        Component zero = languageManager.getMessage(player.createCommandSourceStack(), "placeholder.timeLeftZero", c -> c.placeholder.timeLeftZero);

        ICombatManager combatManager = combatLogX.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null || tagInformation.isExpired()) {
            return zero;
        }

        List<CombatTag> tagList = tagInformation.getTags();
        int tagListSize = tagList.size();
        if (index < 0 || index >= tagListSize) {
            return zero;
        }

        CombatTag combatTag = tagList.get(index);
        long expireMillis = combatTag.getExpireMillis();
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        double timeLeftMillis = Math.max(0.0D, subtractMillis);
        if (timeLeftMillis <= 0.0D) {
            return zero;
        }

        double secondsLeft = (timeLeftMillis / 1_000.0D);
        if (secondsLeft <= 0.0D) {
            return zero;
        }

        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player.createCommandSourceStack());
        String timeLeftString = decimalFormat.format(secondsLeft);
        return Component.text(timeLeftString);
    }

    private @NotNull Component getUnknownEnemy(@NotNull ServerPlayer player) {
        return PlaceholderHelper.getUnknownEnemy(plugin, player);
    }

    private @NotNull Component getEnemyName(@NotNull Entity entity) {
        ICombatLogX mod = getCombatLogX();
        net.minecraft.network.chat.Component vanilla = entity.getName();
        MinecraftServer server = entity.level().getServer();
        if (server == null) throw new IllegalStateException("entity does not have a server");
        return IAdventure.getInstance().asAdventure(vanilla);
    }

    @Deprecated(forRemoval = true)
    private @NotNull Component getEnemyDisplayName(@NotNull Entity enemy) {
        return getEnemyName(enemy);
    }

    private @NotNull Component getEnemyType(@NotNull Entity enemy) {
        EntityType<?> entityType = enemy.getType(); // todo this returns minecraft:zombie but bukkit uses an enum so theirs should be ZOMBIE
        String entityTypeName = EntityType.getKey(entityType).toString();
        return Component.text(entityTypeName);
    }

    private @NotNull Component getEnemyHealth(@NotNull ServerPlayer player, @NotNull Entity enemy) {
        double enemyHealth = 0.0D;
        if (enemy instanceof LivingEntity livingEntity) {
            enemyHealth = livingEntity.getHealth();
        }

        ICombatLogX combatLogX = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player.createCommandSourceStack());

        String healthString = decimalFormat.format(enemyHealth);
        return Component.text(healthString);
    }

    private @NotNull Component getEnemyHealthRounded(@NotNull Entity enemy) {
        double enemyHealth = 0.0D;
        if (enemy instanceof LivingEntity) {
            enemyHealth = ((LivingEntity) enemy).getHealth();
        }

        long round = Math.round(enemyHealth);
        return Component.text(round);
    }

    private @NotNull Component getEnemyHearts(@NotNull Entity enemy) {
        double enemyHealth = 0.0D;
        if (enemy instanceof LivingEntity) {
            enemyHealth = ((LivingEntity) enemy).getHealth();
        }

        double heartsDecimal = (enemyHealth / 2.0D);
        int hearts = (int) Math.round(Math.floor(heartsDecimal));
        if (hearts > 10) {
            return Component.text(hearts);
        }

        char symbol = '❤';
        char[] symbols = new char[hearts];
        Arrays.fill(symbols, symbol);

        String heartsString = new String(symbols);
        return Component.text(heartsString, NamedTextColor.RED);
    }

    private @NotNull Component getEnemyHeartsCount(@NotNull Entity enemy) {
        double enemyHealth = 0.0D;
        if (enemy instanceof LivingEntity) {
            enemyHealth = ((LivingEntity) enemy).getHealth();
        }

        double heartsDecimal = (enemyHealth / 2.0D);
        long hearts = Math.round(Math.floor(heartsDecimal));
        return Component.text(hearts);
    }

    private @NotNull Component getEnemyWorld(@NotNull Entity enemy) {
        Level level = enemy.level();
        String levelName = level.dimension().location().toString();
        return Component.text(levelName);
    }

    private static int locToBlock(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    private @NotNull Component getEnemyX(@NotNull Entity enemy) {
        Vec3 location = enemy.position();
        int blockX = locToBlock(location.x());
        return Component.text(blockX);
    }

    private @NotNull Component getEnemyY(@NotNull Entity enemy) {
        Vec3 location = enemy.position();
        int blockY = locToBlock(location.y());
        return Component.text(blockY);
    }

    private @NotNull Component getEnemyZ(@NotNull Entity enemy) {
        Vec3 location = enemy.position();
        int blockZ = locToBlock(location.z());
        return Component.text(blockZ);
    }

    private @NotNull Component getEnemyPlaceholderAPI(@NotNull ServerPlayer enemy, @NotNull String placeholder) {
        // fixme not sure if this is right
        String placeholderString = ("%" + placeholder + "%");
        placeholderString = placeholderString.replaceFirst("_", ":");

        if (placeholderString.indexOf(':') == -1) {
            ICombatLogX combatLogX = getCombatLogX();
            LanguageManager<LanguageFileConfiguration> languageManager = combatLogX.getLanguageManager();
            MiniMessage miniMessage = languageManager.getMiniMessage();
            return miniMessage.deserialize(placeholderString);
        }

        return PlaceholderHelper.replacePlaceholderAPI(enemy, Component.text(placeholderString));
    }
}
