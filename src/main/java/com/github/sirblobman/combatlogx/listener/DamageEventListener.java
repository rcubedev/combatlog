package com.github.sirblobman.combatlogx.listener;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import com.github.sirblobman.combatlogx.datatracker.ILogoutRules;
import com.github.sirblobman.combatlogx.api.utility.EntityHelper;
import com.github.sirblobman.combatlogx.util.TagReason;

import static com.github.sirblobman.combatlogx.CombatLogX.config;
import static com.github.sirblobman.combatlogx.CombatLogX.debugInfo;

/**
 * Takes care of events.
 * We could use {@link ServerPlayer#onEnterCombat()}
 * and {@link ServerPlayer#onLeaveCombat()} but
 * since we want configurable combat timeout, we
 * have to use fabric events.
 */
public class DamageEventListener {


    /**
     * Marks attacker and target as "in combat state".
     *
     * @param attacker         player who attacked
     * @param _level           world
     * @param _interactionHand hand used to attack
     * @param target           targeted entity
     * @param _entityHitResult hit result
     * @return {@link InteractionResult#PASS}
     */
    public static InteractionResult onAttack(Player attacker, Level _level, InteractionHand _interactionHand, Entity target, @Nullable EntityHitResult _entityHitResult) {
        if (target instanceof ILogoutRules || !config.combatLog.playerHurtOnly) {
            long allowedDc = System.currentTimeMillis() + Math.round(config.combatLog.combatTimeout * 1000);

            // Mark target
            if (target instanceof ILogoutRules && !Permissions.check(target, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
                ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
            }

            // Mark attacker
            if (!Permissions.check(attacker, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
                ((ILogoutRules) attacker).al$setInCombatUntil(allowedDc);
            }
        }
        return InteractionResult.PASS;
    }

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
    public static void onHurt(ServerPlayer target, DamageSource damageSource) {
        debugInfo("Detected player hurt.");

        Entity damager = damageSource.getEntity();
        if (damager == null) {
            debugInfo("Damager is null, ignoring.");
            return;
        }

        // TODO :: optimize by caching the registry keys so it doesn't have to be looked up every time
        debugInfo("Damager Name + Type: " + damager.getName().getString() + " " + BuiltInRegistries.ENTITY_TYPE.getKey(damager.getType()).toString());
        debugInfo("Damaged Name + Type: " + target.getName().getString() + " " + BuiltInRegistries.ENTITY_TYPE.getKey(damager.getType()).toString());

        if (config.combatLog.linkPets) damager = EntityHelper.linkPet(damager);
        if (config.combatLog.linkProjectiles) damager = EntityHelper.linkProjectile(damager);
        if (config.combatLog.linkTnt) damager = EntityHelper.linkTNT(damager);

        long allowedDc = System.currentTimeMillis() + Math.round(config.combatLog.combatTimeout * 1000);

        if (damager instanceof ServerPlayer attacker) {
            if (!Permissions.check(attacker, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
                ((ILogoutRules) attacker).al$setInCombatUntil(allowedDc);
            }

            if (!Permissions.check(target, "antilogout.bypass.combat", config.combatLog.bypassPermissionLevel)) {
                ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
            }
        } else if (damageSource.getEntity() instanceof Player || !config.combatLog.playerHurtOnly) {
            ((ILogoutRules) target).al$setInCombatUntil(allowedDc);
        }
    }

    /**
     * Fishing
     *
     * @param player the player that cast the fishing rod
     * @param caughtEntity the entity caught by the fishing line
     */
    public static void onFishEntity(Player player, Entity caughtEntity) {}

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

    private void tag(Entity entity, Entity enemy, TagReason tagReason) {
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
    }
}
