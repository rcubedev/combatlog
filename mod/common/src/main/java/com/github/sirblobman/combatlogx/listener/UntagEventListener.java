package com.github.sirblobman.combatlogx.listener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.github.sirblobman.combatlogx.api.object.TagInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerKickEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerNPCReplaceEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerQuitEvent;
import com.github.sirblobman.combatlogx.api.configuration.CommandConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.LanguageFileConfiguration;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerDisconnectEvent;
import com.github.sirblobman.combatlogx.api.event.PlayerUntagEvent;
import com.github.sirblobman.combatlogx.api.language.ConfigGetter;
import com.github.sirblobman.combatlogx.api.language.LanguageManager;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.IDeathManager;
import com.github.sirblobman.combatlogx.api.manager.IPlaceholderManager;
import com.github.sirblobman.combatlogx.api.manager.IPunishManager;
import com.github.sirblobman.combatlogx.api.object.CombatTag;
import com.github.sirblobman.combatlogx.api.object.KillTime;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import org.jetbrains.annotations.NotNull;

public final class UntagEventListener extends CombatListener {

    /**
     * Set of players that have disconnected,
     * but are still present in the world.
     * todo migrate out of this class, maybe weakref but idk
     */
    public static final Set<ServerPlayer> DISCONNECTED = new HashSet<>();
    // fixme: temp impl in DeathListener. potentially store component that player died to persist on restart
    //  this has been impl'd in DeathListener
    //public static final Map<UUID, Consumer<ServerPlayer>> DEATH_MESSAGES = new HashMap<>();

    public UntagEventListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        ServerPlayer player = e.getPlayer();

        if (getCombatLogX().getPunishConfiguration().killTime == KillTime.KEEP_ONLINE) return;
        String reason = e.getReason();
        boolean ignored = isKickReasonIgnored(reason);
        UntagReason untagReason = (ignored ? UntagReason.EXPIRE : UntagReason.KICK);

        ICombatManager combatManager = getCombatManager();
        combatManager.untag(player, untagReason);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ServerPlayer player = e.getPlayer();
        if (getCombatLogX().getPunishConfiguration().killTime == KillTime.KEEP_ONLINE || !isInCombat(player)) return;

        ICombatManager combatManager = getCombatManager();
        // fixme this isnt great as if the NPC dies the quit event is fired and they are untagged under a QUIT which idk.
        combatManager.untag(player, UntagReason.QUIT); // fixme playerquitevent may be a kick
    }

    // todo: possibly make punish manager handle this?
    @SubscribeEvent(priority = Priority.MONITOR) // todo is that priority right
    public void onDisconnect(PlayerDisconnectEvent e) {
        ICombatLogX mod = getCombatLogX();
        if (mod.getPunishConfiguration().killTime != KillTime.KEEP_ONLINE) return;
        // CombatLogX.LOGGER.info("Handling disconnect event for player: {}, packet listener: {}, in combat: {}", e.getPlayer().getName(), e.getPacketListener().getClass().getName(), isInCombat(e.getPlayer()));
        ServerPlayer player = e.getPlayer();
        if (!isInCombat(player)) return;

        DISCONNECTED.add(player);
        ((ILogoutRules) player).clx$setFake();
        e.cancel();

        TagInformation tagInformation = mod.getCombatManager().getTagInformation(player);
        List<Entity> enemies = (tagInformation == null) ? List.of() : tagInformation.getEnemies();

        IPunishManager punishManager = mod.getPunishManager();
        punishManager.punish(player, UntagReason.QUIT, enemies, true);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onUntag(PlayerUntagEvent e) {
        ServerPlayer player = e.getPlayer();
        UntagReason untagReason = e.getUntagReason();
        // CombatLogX.LOGGER.info("Handling untag event for player: {}, untag reason: {}, in combat: {}, combat NPC: {}", e.getPlayer().getName().getString(), untagReason, isInCombat(e.getPlayer()), e.isFake());

        sendUntagMessage(player, untagReason);

        List<Entity> previousEnemies = e.getPreviousEnemies();
        runUntagCommands(player, previousEnemies);

        ICombatLogX mod = getCombatLogX();
        IPunishManager punishManager = mod.getPunishManager();
        if (e.isFake()) {
            ServerGamePacketListenerImpl connection = player.connection;
            connection.disconnect(Component.empty());
        } else punishManager.punish(player, untagReason, previousEnemies, false); // disconnected players have already been punished
    }

    private boolean isKickReasonIgnored(@NotNull String reason) {
        ICombatLogX mod = getCombatLogX();
        PunishConfiguration punishConfiguration = mod.getPunishConfiguration();
        return punishConfiguration.isKickIgnored(reason);
    }

    private void sendUntagMessage(ServerPlayer player, UntagReason untagReason) {
        if (!untagReason.isExpire()) return;

        ConfigGetter<LanguageFileConfiguration, String> getter;
        String path;
        switch (untagReason) {
            case EXPIRE -> {
                getter = c -> c.combatTimer.expire;
                path = "combatTimer.expire";
            }
            case SELF_DEATH -> {
                getter = c -> c.combatTimer.selfDeath;
                path = "combatTimer.selfDeath";
            }
            case ENEMY_DEATH -> {
                getter = c -> c.combatTimer.enemyDeath;
                path = "combatTimer.enemyDeath";
            }
            default -> {
                getLogger().warn("Unknown UntagReason: {}", untagReason);
                return;
            }
        }

        ICombatLogX mod = getCombatLogX();
        LanguageManager<LanguageFileConfiguration> languageManager = mod.getLanguageManager();
        languageManager.sendMessage(player.createCommandSourceStack(), path, getter); // todo modifiable loc messages
        // languageManager.sendModifiableMessageWithPrefix(player, languagePath);
    }

    private void runUntagCommands(ServerPlayer player, List<Entity> enemyList) {
        ICombatLogX mod = getCombatLogX();
        CommandConfiguration commandConfiguration = mod.getCommandConfiguration();
        List<String> untagCommandList = commandConfiguration.untagCommandList;
        if (untagCommandList.isEmpty()) return;

        IPlaceholderManager placeholderManager = mod.getPlaceholderManager();
        placeholderManager.runReplacedCommands(player, enemyList, untagCommandList);
    }

    @SubscribeEvent(ignoreCancelled = true)
    public void onNPCReplace(PlayerNPCReplaceEvent e) {
        ServerPlayer oldNpc = e.getOldNPC();
        ServerPlayer newPlayer = e.getNewPlayer();
        UUID playerId = newPlayer.getUUID();

        ICombatManager combatManager = getCombatManager();
        for (CombatTag tag : combatManager.getAllCombatTags()) {
            if (tag.getEnemy() == oldNpc || playerId.equals(tag.getEnemyId())) {
                tag.setEnemy(newPlayer);
            }
        }

        IDeathManager deathManager = getCombatLogX().getDeathManager();
        deathManager.transferState(oldNpc, newPlayer);
    }
}
