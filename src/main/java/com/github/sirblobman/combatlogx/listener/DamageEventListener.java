package com.github.sirblobman.combatlogx.listener;

import java.util.UUID;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.github.sirblobman.combatlogx.api.utility.EntityHelper;

import static com.github.sirblobman.combatlogx.CombatLogX.config;
import static com.github.sirblobman.combatlogx.CombatLogX.debugInfo;

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

    /**
     * Disconnects afk player on death.
     *
     * @param deadEntity    entity that died
     * @param damageSource damage source of death
     */
    public static void onDeath(LivingEntity deadEntity, DamageSource damageSource) {
        if (deadEntity instanceof ILogoutRules player) {
            if (player.al$isFake()) {
                // Remove player from online players
                ((ServerPlayer) player).connection.onDisconnect(new DisconnectionDetails(Component.empty()));
            } else if (damageSource.getEntity() instanceof ILogoutRules attacker) {
                // attacker.al$setInCombatUntil(0); // FIXME :: Not safe as if player is killing two players and one dies, the player can logout in fight w/ the other
            }
        }
    }

    /**
     * Marks player as "in combat state" if
     * enabled for that damage source.
     * If damage source is a projectile, shot by
     * a player, then that player is also marked.
     *
     * @param target       player who was hurt
     * @param damageSource damage source
     */
    public void onHurt(Entity target, DamageSource damageSource) {
        debugInfo("Detected entity hurt.");
        onHurtCrystalCheck(target, damageSource);

        Entity damager = getDamager(damageSource.getEntity());
        if (damager == null) {
            debugInfo("Damager is null, ignoring.");
            return;
        }

        // TODO :: optimize by caching the registry keys so it doesn't have to be looked up every time?
        debugInfo("Damager Name + Type: " + damager.getName().getString() + " " + BuiltInRegistries.ENTITY_TYPE.getKey(damager.getType()));
        debugInfo("Damaged Name + Type: " + target.getName().getString() + " " + BuiltInRegistries.ENTITY_TYPE.getKey(damager.getType()));

        checkTag(damager, target, TagReason.ATTACKER);
        checkTag(target, damager, TagReason.ATTACKED);
        // long allowedDc = System.currentTimeMillis() + Math.round(config.combatLog.combatTimeout * 1000);
        //
        // if (damager instanceof ServerPlayer attacker) {
        //     if (!Permissions.check(attacker, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
        //         ((ILogoutRules) attacker).al$setInCombatUntil(allowedDc);
        //     }
        //
        //     if (!Permissions.check(target, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
        //         ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
        //     }
        // } else if (damageSource.getEntity() instanceof Player || !config.combatLog.playerHurtOnly) {
        //     ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
        // }
    }

    public void onHurtCrystalCheck(Entity target, DamageSource damageSource) {
        ICombatLogX mod = getCombatLogX();
        if (!config.combatLog.linkEndCrystals) return;

        if (!(target instanceof Player player)) return;

        Entity damager = damageSource.getEntity();
        if (damager == null) return;
        if (!(damager instanceof EndCrystal)) return;

        ICrystalManager crystalManager = mod.getCrystalManager();
        Player placer = crystalManager.getPlacer(damager);

        if (placer != null) {
            crystalCheckTag(placer, player, TagReason.ATTACKER);
            crystalCheckTag(player, placer, TagReason.ATTACKED);
        }

        UUID damagerId = damager.getUniqueId();
        crystalManager.remove(damagerId);
    }

    /**
     * Fishing
     *
     * @param player the player that cast the fishing rod
     * @param caughtEntity the entity caught by the fishing line
     */
    public void onFishEntity(Player player, Entity caughtEntity) {
        if (!config.combatLog.linkFishingRod) return;
        checkTag(player, caughtEntity, TagReason.ATTACKER);
    }

    // fixme move out from here this is just for msg handling if player relogs
    /**
     * Sends death message to player if they died while disconnected,
     * but still present in the world.
     *
     * @param listener packet listener
     * @param _sender  packet sender
     * @param _server  minecraft server
     */
    public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender _sender, MinecraftServer _server) {
        final Component deathMessage = ILogoutRules.SKIPPED_DEATH_MESSAGES.get(listener.player.getUUID());
        if (deathMessage != null) {
            listener.player.displayClientMessage(deathMessage, false);
            listener.send(new ClientboundPlayerCombatKillPacket(listener.player.getId(), deathMessage));
            ILogoutRules.SKIPPED_DEATH_MESSAGES.remove(listener.player.getUUID());
        }
    }

    @Contract("null -> null")
    private Entity getDamager(Entity entity) {
        if (entity == null) {
            return null;
        }

        ICombatLogX mod = getCombatLogX();

        if (config.combatLog.linkProjectiles) entity = EntityHelper.linkProjectile(mod, entity);
        if (config.combatLog.linkPets) entity = EntityHelper.linkPet(entity);
        if (config.combatLog.linkTnt) entity = EntityHelper.linkTNT(entity);
        if (config.combatLog.linkEndCrystals) {
            ICombatLogX combatLogX = getCombatLogX();
            ICrystalManager crystalManager = combatLogX.getCrystalManager();

            Player player = crystalManager.getPlacer(entity);
            if (player != null) {
                entity = player;
            }
        }

        return entity;
    }


    private void checkTag(Entity entity, Entity enemy, TagReason tagReason) {
        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = getCombatManager();
        debugInfo("Checking if the entity '" + entity.getName().getString() + "' should be tagged " +
                "for reason '" + tagReason + "' by enemy '" + enemy.getName().getString() + "'.");

        if (!(entity instanceof Player playerEntity)) {
            debugInfo("Entity was not a player.");
            return;
        }

        if (!(enemy instanceof Player playerEnemy)) {
            debugInfo("Enemy was not a player.");
            return;
        }

        debugInfo("Triggering tag for player " + playerEntity.getName().getString() + " with enemy "
                + playerEnemy.getName().getString() + "...");
        boolean tag = combatManager.tag(playerEntity, playerEnemy, TagType.PLAYER, tagReason);
        debugInfo("CombatTag Status: " + tag);
    }

    private void crystalCheckTag(@NotNull Player player, @NotNull Player enemy, @NotNull TagReason tagReason) {
        ICombatManager combatManager = getCombatManager();
        combatManager.tag(player, enemy, TagType.PLAYER, tagReason);
    }
}
