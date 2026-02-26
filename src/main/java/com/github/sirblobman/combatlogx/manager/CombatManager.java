package com.github.sirblobman.combatlogx.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.Nullable;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.util.TagInformation;
import com.github.sirblobman.combatlogx.util.TagReason;
import com.github.sirblobman.combatlogx.util.TagType;
import com.github.sirblobman.combatlogx.util.UntagReason;

import static com.github.sirblobman.combatlogx.CombatLogX.debugInfo;

public final class CombatManager implements ICombatManager {
    private final Map<UUID, TagInformation> combatMap;

    public CombatManager() {
        this.combatMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tag(Player player, @Nullable Entity enemy, TagType tagType,
                       TagReason tagReason) {
        int timerSeconds = getMaxTimerSeconds(player);
        long timerMillis = (timerSeconds * 1_000L);

        long systemMillis = System.currentTimeMillis();
        long endMillis = (systemMillis + timerMillis);
        return tag(player, enemy, tagType, tagReason, endMillis);
    }

    @Override
    public boolean tag(Player player, @Nullable Entity enemy, TagType tagType,
                       TagReason tagReason, long customEndMillis) {
        if (player.hasMetadata("NPC")) {
            debugInfo("player is an NPC and can't be tagged.");
            return false;
        }

        if (failsPreTagEvent(player, enemy, tagType, tagReason)) {
            debugInfo("The PlayerPreTagEvent was cancelled.");
            return false;
        }

        MainConfiguration configuration = plugin.getConfiguration();
        double minimumTps = configuration.getMinimumTps();
        if (minimumTps > 0.0D) {
            double tps = getServerTPS();
            if (tps < minimumTps) {
                debugInfo("Server TPS: " + tps);
                debugInfo("Minimum TPS: " + tps);
                debugInfo("The server tps is too low to tag players.");
                return false;
            }
        }

        boolean alreadyInCombat = isInCombat(player);
        debugInfo("Previous Combat Status: " + alreadyInCombat);

        if (alreadyInCombat) {
            PlayerReTagEvent event = new PlayerReTagEvent(player, enemy, tagType, tagReason, customEndMillis);
            pluginManager.callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            customEndMillis = event.getEndTime();
        } else {
            PlayerTagEvent event = new PlayerTagEvent(player, enemy, tagType, tagReason, customEndMillis);
            pluginManager.callEvent(event);

            customEndMillis = event.getEndTime();
            sendTagMessage(player, enemy, tagType, tagReason);
        }


        UUID playerId = player.getUniqueId();
        CombatTag combatTag = new CombatTag(enemy, tagType, tagReason, customEndMillis);
        TagInformation tagInformation = this.combatMap.computeIfAbsent(playerId, key -> new TagInformation(player));
        tagInformation.addTag(combatTag);

        String playerName = player.getName();
        debugInfo("Successfully put player '" + playerName + "' into combat.");
        return true;
    }

    @Override
    public void untag(Player player, UntagReason untagReason) {
        if (!isInCombat(player)) {
            return;
        }

        TagInformation tagInformation = getTagInformation(player);
        if (tagInformation == null) {
            return;
        }

        UUID playerId = player.getUUID();
        this.combatMap.remove(playerId);

        ITimerManager timerManager = plugin.getTimerManager();
        timerManager.remove(player);

        List<Entity> enemyList = tagInformation.getEnemies();
        for (Entity entity : enemyList) {
            PlayerEnemyRemoveEvent event = new PlayerEnemyRemoveEvent(player, untagReason, entity);
            pluginManager.callEvent(event);
        }

        PlayerUntagEvent event = new PlayerUntagEvent(player, untagReason, enemyList);
        pluginManager.callEvent(event);
    }

    @Override
    public void untag(Player player, Entity enemy, UntagReason untagReason) {
        if (!isInCombat(player)) {
            return;
        }

        TagInformation tagInformation = getTagInformation(player);
        if (tagInformation == null || !tagInformation.isEnemy(enemy)) {
            return;
        }

        tagInformation.removeEnemy(enemy);
        PlayerEnemyRemoveEvent event = new PlayerEnemyRemoveEvent(player, untagReason, enemy);
        pluginManager.callEvent(event);

        if (tagInformation.isExpired()) {
            untag(player, untagReason);
        }
    }

    @Override
    public boolean isInCombat(Player player) {
        TagInformation tagInformation = getTagInformation(player);
        return (tagInformation != null);
    }

    @Override
    public Set<UUID> getPlayerIdsInCombat() {
        Set<UUID> playerIdSet = this.combatMap.keySet();
        return Collections.unmodifiableSet(playerIdSet);
    }

    @Override
    public List<Player> getPlayersInCombat() {
        Set<UUID> playerIdSet = getPlayerIdsInCombat();
        List<Player> playerList = new ArrayList<>();

        for (UUID playerId : playerIdSet) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                playerList.add(player);
            }
        }

        return Collections.unmodifiableList(playerList);
    }

    @Override
    public TagInformation getTagInformation(Player player) {
        UUID playerId = player.getUniqueId();
        return this.combatMap.get(playerId);
    }

    @Override
    public int getMaxTimerSeconds(Player player) {

        return getGlobalTimerSeconds();
    }

    @Override
    public @Nullable String getBypassPermission() {
        return configuration.getBypassPermission();
    }

    @Override
    public boolean canBypass(Player player) {
        String bypassPermission = getBypassPermission();
        if (bypassPermission == null) {
            return false;
        }

        return Permissions.check(player, bypassPermission);
    }

    private int getGlobalTimerSeconds() {
        return configuration.getDefaultTimer();
    }

    private int getPermissionTimerSeconds(Player player) {
        int defaultTimer = getGlobalTimerSeconds();
        Set<PermissionAttachmentInfo> permissionAttachmentInfoSet = player.getEffectivePermissions();
        if (permissionAttachmentInfoSet.isEmpty()) {
            return defaultTimer;
        }

        Set<String> permissionNumberStrings = new HashSet<>();
        for (PermissionAttachmentInfo permissionAttachmentInfo : permissionAttachmentInfoSet) {
            if (!permissionAttachmentInfo.getValue()) {
                continue;
            }

            String permissionName = permissionAttachmentInfo.getPermission();
            if (permissionName.startsWith("combatlogx.timer.")) {
                String timerPart = permissionName.substring("combatlogx.timer.".length());
                permissionNumberStrings.add(timerPart);
            }
        }

        if (permissionNumberStrings.isEmpty()) {
            return defaultTimer;
        }

        int lowestTimer = Integer.MAX_VALUE;
        boolean foundValue = false;

        for (String permission : permissionNumberStrings) {
            try {
                int value = Integer.parseInt(permission);
                lowestTimer = Math.min(lowestTimer, value);
                foundValue = true;
            } catch (NumberFormatException ignored) {
                // Ignored Exception
            }
        }

        return (foundValue ? lowestTimer : defaultTimer);
    }

    private Component getUnknownEnemy(Player player) {
        ICombatLogX plugin = getCombatLogX();
        LanguageManager languageManager = plugin.getLanguageManager();
        return languageManager.getMessage(player, "placeholder.unknown-enemy");
    }

    private Component getEntityName(Player player, @Nullable Entity entity) {
        if (entity == null) {
            return getUnknownEnemy(player);
        }

        if (PaperChecker.hasNativeComponentSupport()) {
            Component customName = PaperHelper.getCustomName(entity);
            if (customName != null) {
                return customName;
            }
        }

        ICombatLogX plugin = getCombatLogX();
        MultiVersionHandler multiVersionHandler = plugin.getMultiVersionHandler();
        EntityHandler entityHandler = multiVersionHandler.getEntityHandler();

        String entityName = entityHandler.getName(entity);
        return Component.text(entityName);
    }

    private Component getEntityType(Player player, @Nullable Entity entity) {
        if (entity == null) {
            return getUnknownEnemy(player);
        }

        EntityType entityType = entity.getType();
        String entityTypeName = entityType.name();
        return Component.text(entityTypeName);
    }

    private boolean failsPreTagEvent(Player player, @Nullable Entity enemy, TagType tagType,
                                     TagReason tagReason) {
        PlayerPreTagEvent event = new PlayerPreTagEvent(player, enemy, tagType, tagReason);
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.callEvent(event);
        return event.isCancelled();
    }

    private void sendTagMessage(Player player, @Nullable Entity enemy, TagType tagType,
                                TagReason tagReason) {
        if (tagType == TagType.DAMAGE) {
            return;
        }

        Component enemyName = getEntityName(player, enemy);
        Component enemyType = getEntityType(player, enemy);
        String tagReasonString = tagReason.name().toLowerCase(Locale.US);
        String tagTypeString = tagType.name().toLowerCase(Locale.US);

        Replacer enemyNameReplacer = new ComponentReplacer("{enemy}", enemyName);
        Replacer enemyTypeReplacer = new ComponentReplacer("{mob_type}", enemyType);
        String languagePath = ("tagged." + tagReasonString + "." + tagTypeString);

        ICombatLogX plugin = getCombatLogX();
        LanguageManager languageManager = plugin.getLanguageManager();
        languageManager.sendModifiableMessageWithPrefix(player, languagePath, enemyNameReplacer, enemyTypeReplacer);
    }

    private double getServerTPS() {
        if (PaperChecker.isPaper()) {
            try {
                return PaperHelper.getServer1mTps();
            } catch (NoSuchMethodError ignored) {
                // Ignored Error
            }
        }

        ICombatLogX plugin = getCombatLogX();
        MultiVersionHandler multiVersionHandler = plugin.getMultiVersionHandler();
        ServerHandler serverHandler = multiVersionHandler.getServerHandler();
        return serverHandler.getServerTps1m();
    }
}