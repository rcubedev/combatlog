package com.github.sirblobman.combatlogx.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.platform.IAdventure;
import com.github.rcubedev.example.platform.IPlatformHelper;
import com.github.sirblobman.combatlogx.PermissionHolder;
import com.github.sirblobman.combatlogx.VersionUtil;import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.language.ConfigGetter;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.language.replacer.ComponentReplacer;
import com.github.sirblobman.combatlogx.api.language.replacer.Replacer;
import com.github.sirblobman.combatlogx.api.utility.Mapping;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.google.common.math.Stats;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.event.PlayerEnemyRemoveEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerReTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import com.github.sirblobman.combatlogx.api.object.CombatTag;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;
import com.github.sirblobman.combatlogx.api.object.TimerType;
import com.github.sirblobman.combatlogx.api.object.UntagReason;

public final class CombatManager extends Manager implements ICombatManager {
    private final Map<UUID, TagInformation> combatMap;
    private final Set<CombatTag> allCombatTags;
    // private final MinecraftServer server;

    public CombatManager(@NotNull ICombatLogX mod/*, @NotNull MinecraftServer server*/) {
        super(mod);
        // this.server = server;
        this.combatMap = new ConcurrentHashMap<>();
        this.allCombatTags = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @Override
    public boolean tag(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                       @NotNull TagReason tagReason) {
        int timerSeconds = getMaxTimerSeconds(player);
        long timerMillis = (timerSeconds * 1_000L);

        long systemMillis = System.currentTimeMillis();
        long endMillis = (systemMillis + timerMillis);
        return tag(player, enemy, tagType, tagReason, endMillis);
    }

    @Override
    public boolean tag(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                       @NotNull TagReason tagReason, long customEndMillis) {
        ICombatLogX mod = getCombatLogX();
        if (isNPC(player)) { // fixme is this wanted? some mods may want fake players to act as players not NPCs.
            mod.printDebug("player is an NPC and can't be tagged.");
            return false;
        }

        if (failsPreTagEvent(player, enemy, tagType, tagReason)) {
            mod.printDebug("The PlayerPreTagEvent was cancelled.");
            return false;
        }

        double minimumTps = mod.getConfiguration().minimumServerTPS;
        if (minimumTps > 0.0D) {
            double tps = getServerTPS(VersionUtil.getServer(player)); // todo maybe make it per world. see getServerTPS for more info.
            if (tps < minimumTps) {
                mod.printDebug("Server TPS: " + tps);
                mod.printDebug("Minimum TPS: " + tps);
                mod.printDebug("The server tps is too low to tag players.");
                return false;
            }
        }

        boolean alreadyInCombat = isInCombat(player);
        mod.printDebug("Previous Combat Status: " + alreadyInCombat);

        if (alreadyInCombat) {
            PlayerReTagEvent event = new PlayerReTagEvent(player, enemy, tagType, tagReason, customEndMillis);
            event.dispatch();
            if (event.isCancelled()) return false;

            customEndMillis = event.getEndTime();
        } else {
            PlayerTagEvent event = new PlayerTagEvent(player, enemy, tagType, tagReason, customEndMillis);
            event.dispatch();

            customEndMillis = event.getEndTime();
            sendTagMessage(player, enemy, tagType, tagReason);
        }

        UUID playerId = player.getUUID();
        CombatTag combatTag = new CombatTag(enemy, tagType, tagReason, customEndMillis).register(mod);
        TagInformation tagInformation = this.combatMap.computeIfAbsent(playerId, key -> new TagInformation(player));
        tagInformation.addTag(combatTag);

        String playerName = player.getName().getString();
        mod.printDebug("Successfully put player '" + playerName + "' into combat.");
        return true;
    }

    @Override
    public void untag(@NotNull ServerPlayer player, @NotNull UntagReason untagReason) {
        if (!isInCombat(player)) return;

        TagInformation tagInformation = getTagInformation(player);
        if (tagInformation == null) return;

        UUID playerId = player.getUUID();
        this.combatMap.remove(playerId);

        ICombatLogX mod = getCombatLogX();
        ITimerManager timerManager = mod.getTimerManager();
        timerManager.remove(player);

        List<Entity> enemyList = tagInformation.getEnemies();
        for (Entity entity : enemyList) {
            PlayerEnemyRemoveEvent event = new PlayerEnemyRemoveEvent(player, untagReason, entity);
            event.dispatch();
        }

        PlayerUntagEvent event = new PlayerUntagEvent(player, untagReason, enemyList);
        event.dispatch();
    }

    @Override
    public void untag(@NotNull ServerPlayer player, @NotNull Entity enemy, @NotNull UntagReason untagReason) {
        if (!isInCombat(player)) return;

        TagInformation tagInformation = getTagInformation(player);
        if (tagInformation == null || !tagInformation.isEnemy(enemy)) return;

        tagInformation.removeEnemy(enemy);
        PlayerEnemyRemoveEvent event = new PlayerEnemyRemoveEvent(player, untagReason, enemy);
        event.dispatch();

        if (tagInformation.isExpired()) {
            untag(player, untagReason);
        }
    }

    @Override
    public boolean isInCombat(@NotNull ServerPlayer player) {
        TagInformation tagInformation = getTagInformation(player);
        return (tagInformation != null);
    }

    @Override
    public @NotNull Set<UUID> getPlayerIdsInCombat() {
        Set<UUID> playerIdSet = this.combatMap.keySet();
        return Collections.unmodifiableSet(playerIdSet);
    }

    // previously captured server on creation
    @Override
    public @NotNull List<ServerPlayer> getPlayersInCombat(@NotNull MinecraftServer server) {
        Set<UUID> playerIdSet = getPlayerIdsInCombat();
        List<ServerPlayer> playerList = new ArrayList<>();

        for (UUID playerId : playerIdSet) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) playerList.add(player);
        }

        return Collections.unmodifiableList(playerList);
    }

    @Override
    public @Nullable TagInformation getTagInformation(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        return this.combatMap.get(playerId);
    }

    @Override
    public int getMaxTimerSeconds(@NotNull ServerPlayer player) {
        ICombatLogX mod = getCombatLogX();
        MainConfiguration configuration = mod.getConfiguration();
        TimerType timerType = configuration.timer.type;

        if (timerType == TimerType.PERMISSION) return getPermissionTimerSeconds(player);

        return getGlobalTimerSeconds();
    }

    @Override
    public @Nullable PermissionNode<Boolean> getBypassPermission() {
        ICombatLogX mod = getCombatLogX();
        PermissionHolder permissionHolder = mod.getPermissionHolder();
        return permissionHolder.getBypass();
    }

    @Override
    public boolean canBypass(@NotNull ServerPlayer player) {
        PermissionNode<Boolean> bypassPermission = getBypassPermission();
        if (bypassPermission == null) return false;

        return bypassPermission.resolve(player).orElse(false);
    }

    @Override
    public boolean isNPC(@NotNull ServerPlayer player) {
        if (((ILogoutRules) player).al$isFake()) return false; // fixme jank
        return IPlatformHelper.getInstance().isNPCInst(player);
    }

    private int getGlobalTimerSeconds() {
        ICombatLogX combatLogX = getCombatLogX();
        MainConfiguration configuration = combatLogX.getConfiguration();
        return configuration.timer.defaultTimer;
    }

    private int getPermissionTimerSeconds(@NotNull ServerPlayer player) {
        int defaultTimer = getGlobalTimerSeconds();
        return getCombatLogX().getPermissionHolder().getTimer().resolve(player).orElse(defaultTimer);
    }

    private @NotNull Component getUnknownEnemy(@NotNull ServerPlayer player) {
        ICombatLogX mod = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        return languageManager.getMessage(player.createCommandSourceStack(), "placeholder.unknownEnemy", c -> c.placeholder.unknownEnemy);
    }

    private @NotNull Component getEntityName(@NotNull ServerPlayer player, @Nullable Entity entity) {
        if (entity == null) return getUnknownEnemy(player);
        return IAdventure.getInstance().asAdventure(entity.getName());
    }

    private @NotNull Component getEntityType(@NotNull ServerPlayer player, @Nullable Entity entity) {
        if (entity == null) return getUnknownEnemy(player);

        EntityType<?> entityType = entity.getType();
        String entityTypeName = EntityType.getKey(entityType).toString();
        return Component.text(entityTypeName);
    }

    private boolean failsPreTagEvent(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                                     @NotNull TagReason tagReason) {
        PlayerPreTagEvent event = new PlayerPreTagEvent(player, enemy, tagType, tagReason);
        event.dispatch();
        return event.isCancelled();
    }

    private void sendTagMessage(@NotNull ServerPlayer player, @Nullable Entity enemy, @NotNull TagType tagType,
                                @NotNull TagReason tagReason) {
        if (tagType == TagType.DAMAGE) return;

        Component enemyName = getEntityName(player, enemy);
        Component enemyType = getEntityType(player, enemy); // todo i think this should be changed from minecraft:zombie -> ZOMBIE for example; i think thats how it is in CLX but needs checking.

        Replacer enemyNameReplacer = new ComponentReplacer("{enemy}", enemyName);
        Replacer enemyTypeReplacer = new ComponentReplacer("{mob_type}", enemyType);

        Mapping<LanguageFileConfiguration, LanguageFileConfiguration.TaggedSection.TaggedTypeSection> reasonMap = switch (tagReason) {
            case ATTACKED -> new Mapping<>("tagged.attacked", c -> c.tagged.attacked);
            case ATTACKER -> new Mapping<>("tagged.attacker", c -> c.tagged.attacker);
            default       -> new Mapping<>("tagged.unknown", c -> c.tagged.unknown);
        };

        Mapping<LanguageFileConfiguration.TaggedSection.TaggedTypeSection, String> typeMap = switch (tagType) {
            case PLAYER     -> new Mapping<>("player", s -> s.player);
            case MOB        -> new Mapping<>("mob", s -> s.mob);
            case MYTHIC_MOB -> new Mapping<>("mythicMob", s -> s.mythicMob);
            default         -> new Mapping<>("unknown", s -> s.unknown);
        };

        String path = reasonMap.path() + "." + typeMap.path();
        ConfigGetter<LanguageFileConfiguration, String> getter = reasonMap.mapper().andThen(typeMap.mapper())::apply;

        ICombatLogX mod = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        languageManager.sendMessageWithPrefix(player.createCommandSourceStack(), path, getter, enemyNameReplacer, enemyTypeReplacer);
        // languageManager.sendModifiableMessageWithPrefix(player, languagePath, enemyNameReplacer, enemyTypeReplacer); // todo for modifiable message loc
    }

    // todo maybe make it per dimension? see neoforge tpscommand
    private double getServerTPS(@NotNull MinecraftServer server) {
        long[] times = server.getTickTimesNanos();
        TickRateManager tickRateManager = server.tickRateManager();
        double tickTime = Stats.meanOf(times) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
        return TimeUtil.MILLISECONDS_PER_SECOND / Math.max(tickTime, tickRateManager.millisecondsPerTick());
    }

    @Override
    public Collection<CombatTag> getAllCombatTags() {
        return this.allCombatTags;
    }
}
