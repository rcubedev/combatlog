package com.github.sirblobman.combatlogx.listener;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityDamageByEntityEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerFishEntityEvent;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.github.sirblobman.combatlogx.api.utility.EntityHelper;

/**
 * Takes care of events.
 * We could use {@link ServerPlayer#onEnterCombat()}
 * and {@link ServerPlayer#onLeaveCombat()} but
 * since we want configurable combat timeout, we
 * have to use fabric events.
 */
public class DamageEventListener extends CombatListener {

    public DamageEventListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    // /**
    //  * Marks attacker and target as "in combat state".
    //  *
    //  * @param attacker         player who attacked
    //  * @param _level           world
    //  * @param _interactionHand hand used to attack
    //  * @param target           targeted entity
    //  * @param _entityHitResult hit result
    //  * @return {@link InteractionResult#PASS}
    //  */
    // public static InteractionResult onAttack(Player attacker, Level _level, InteractionHand _interactionHand, Entity target, @Nullable EntityHitResult _entityHitResult) {
    //     if (target instanceof ILogoutRules || !config.combatLog.playerHurtOnly) {
    //         long allowedDc = System.currentTimeMillis() + Math.round(config.combatLog.combatTimeout * 1000);
    //
    //         // Mark target
    //         if (target instanceof ILogoutRules && !Permissions.check(target, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
    //             ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
    //         }
    //
    //         // Mark attacker
    //         if (!Permissions.check(attacker, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
    //             ((ILogoutRules) attacker).al$setInCombatUntil(allowedDc);
    //         }
    //     }
    //     return InteractionResult.PASS;
    // }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        printDebug("Detected EntityDamageByEntityEvent.");

        Entity target = e.getEntity();
        Entity damager = getDamager(e);
        if (damager == null) {
            printDebug("Damager is null, ignoring.");
            return;
        }

        // todo: optimize by caching the registry keys so it doesn't have to be looked up every time?
        printDebug("Damager Name + Type: " + getName(damager) + " " + EntityType.getKey(damager.getType()));
        printDebug("Damaged Name + Type: " + getName(target) + " " + EntityType.getKey(target.getType()));

        checkTag(damager, target, TagReason.ATTACKER);
        checkTag(target, damager, TagReason.ATTACKED);
    }

    /**
     * Fishing
     */
    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onFishEntity(PlayerFishEntityEvent event) {
        if (!getConfiguration().linkFishingRod) return;
        ServerPlayer player = event.getPlayer();
        Entity caughtEntity = event.getCaught();
        checkTag(player, caughtEntity, TagReason.ATTACKER);
    }

    // /**
    //  * Disconnects afk player on death.
    //  *
    //  * @param deadEntity    entity that died
    //  * @param damageSource damage source of death
    //  */
    // public static void onDeath(LivingEntity deadEntity, DamageSource damageSource) {
    //     if (deadEntity instanceof ILogoutRules player) {
    //         if (player.al$isFake()) {
    //             // Remove player from online players
    //             ((ServerPlayer) player).connection.onDisconnect(new DisconnectionDetails(Component.empty()));
    //         } else if (damageSource.getEntity() instanceof ILogoutRules attacker) {
    //             // attacker.al$setInCombatUntil(0); // FIXME :: Not safe as if player is killing two players and one dies, the player can logout in fight w/ the other - possibly just block join or force kill
    //         }
    //     }
    // }

    // // fixme move out from here this is just for msg handling if player relogs
    // /**
    //  * Sends death message to player if they died while disconnected,
    //  * but still present in the world.
    //  *
    //  * @param listener packet listener
    //  * @param _sender  packet sender
    //  * @param _server  minecraft server
    //  */
    // public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender _sender, MinecraftServer _server) {
    //     final Component deathMessage = ILogoutRules.SKIPPED_DEATH_MESSAGES.get(listener.player.getUUID());
    //     if (deathMessage != null) {
    //         listener.player.displayClientMessage(deathMessage, false);
    //         listener.send(new ClientboundPlayerCombatKillPacket(listener.player.getId(), deathMessage));
    //         ILogoutRules.SKIPPED_DEATH_MESSAGES.remove(listener.player.getUUID());
    //     }
    // }

    private MainConfiguration getConfiguration() {
        ICombatLogX mod = getCombatLogX();
        return mod.getConfiguration();
    }

    private Entity getDamager(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        return getDamager(entity);
    }

    @Contract("null -> null")
    private Entity getDamager(Entity entity) {
        if (entity == null) return null;

        ICombatLogX mod = getCombatLogX();
        MainConfiguration configuration = getConfiguration();

        if (configuration.linkProjectiles) entity = EntityHelper.linkProjectile(mod, entity);
        if (configuration.linkPets) entity = EntityHelper.linkPet(entity);
        if (configuration.linkTNT) entity = EntityHelper.linkTNT(entity);
        if (configuration.linkEndCrystal && entity instanceof EndCrystal crystal) {
            ICombatLogX combatLogX = getCombatLogX();
            ICrystalManager crystalManager = combatLogX.getCrystalManager();

            ServerPlayer player = crystalManager.getPlacer(crystal);
            if (player != null) entity = player;
        }

        return entity;
    }


    private void checkTag(Entity entity, Entity enemy, TagReason tagReason) {
        ICombatLogX mod = getCombatLogX();
        ICombatManager combatManager = getCombatManager();
        mod.printDebug("Checking if the entity '" + entity.getName().getString() + "' should be tagged " +
                "for reason '" + tagReason + "' by enemy '" + enemy.getName().getString() + "'.");

        if (!(entity instanceof ServerPlayer playerEntity)) {
            mod.printDebug("Entity was not a player.");
            return;
        }

        if (!(enemy instanceof ServerPlayer playerEnemy)) {
            mod.printDebug("Enemy was not a player.");
            return;
        }

        mod.printDebug("Triggering tag for player " + playerEntity.getName().getString() + " with enemy "
                + playerEnemy.getName().getString() + "...");
        boolean tag = combatManager.tag(playerEntity, playerEnemy, TagType.PLAYER, tagReason);
        mod.printDebug("CombatTag Status: " + tag);
    }

    private String getName(@NotNull Entity entity) {
        return entity.getName().getString();
    }
}
