package com.github.sirblobman.combatlogx.api.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;

import static com.github.sirblobman.combatlogx.CombatLogX.config;

public class EntityHelper {

    /**
     * If the entity is a pet, returns its owner.
     * Otherwise, returns the entity itself to allow easier handling.
     *
     * @param entity entity to check
     * @return owner if pet, otherwise the entity itself
     */
    public static Entity linkPet(Entity entity) {
        if (!(entity instanceof TamableAnimal tamableAnimal)) return entity;

        LivingEntity animalTamer = tamableAnimal.getOwner();
        if (animalTamer == null || animalTamer instanceof ServerPlayer) return entity;
        return animalTamer;
    }

    /**
     * If the entity is a projectile owned by a player, returns the projectile.
     * Otherwise, returns the entity itself to allow easier handling.
     *
     * @param entity entity to check
     * @return projectile if owned by player, otherwise the entity itself
     */
    public static Entity linkProjectile(Entity entity) {
        if (!(entity instanceof Projectile projectile) || !(projectile.getOwner() instanceof ServerPlayer shooter)) return entity;
        if (projectileIgnored(projectile)) return entity;
        return shooter;
    }

    /**
     * If the entity is a TNT primed by a player, returns the player.
     * Otherwise, returns the entity itself to allow easier handling.
     *
     * @param entity entity to check
     * @return player if TNT is primed by player, otherwise the entity itself
     */
    public static Entity linkTNT(Entity entity) {
        if (!(entity instanceof PrimedTnt tnt) || !(tnt.getOwner() instanceof ServerPlayer source)) return entity;
        return source;
    }

    private static boolean projectileIgnored(Projectile projectile) {
        // TODO :: optimize by caching the registry keys so it doesn't have to be looked up every time
        EntityType<?> type = projectile.getType();
        return config.combatLog.ignoredProjectiles.contains(EntityType.getKey(type).toString());
    }
}
