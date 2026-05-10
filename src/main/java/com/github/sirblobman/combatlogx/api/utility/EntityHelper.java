package com.github.sirblobman.combatlogx.api.utility;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import org.jetbrains.annotations.NotNull;

public class EntityHelper {

    /**
     * If the entity is a pet, returns its owner.
     * Otherwise, returns the entity itself to allow easier handling.
     *
     * @param entity entity to check
     * @return owner if pet, otherwise the entity itself
     */
    public static @NotNull Entity linkPet(@NotNull Entity entity) {
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
    public static @NotNull Entity linkProjectile(@NotNull ICombatLogX mod, @NotNull Entity entity) {
        if (!(entity instanceof Projectile projectile)) return entity;
        if (isProjectileIgnored(mod, projectile)) return entity;

        Entity shooter = projectile.getOwner();
        if (shooter == null) return entity;
        return shooter;
    }

    /**
     * If the entity is a TNT primed by a player, returns the player.
     * Otherwise, returns the entity itself to allow easier handling.
     *
     * @param entity entity to check
     * @return player if TNT is primed by player, otherwise the entity itself
     */
    public static @NotNull Entity linkTNT(@NotNull Entity entity) {
        if (!(entity instanceof PrimedTnt tnt) || !(tnt.getOwner() instanceof ServerPlayer source)) return entity;
        return source;
    }

    private static boolean isProjectileIgnored(@NotNull ICombatLogX mod, @NotNull Projectile projectile) {
        // TODO: maybe optimize by caching the registry keys so it doesn't have to be looked up every time
        EntityType<?> type = projectile.getType();
        MainConfiguration configuration = mod.getConfiguration();
        return configuration.ignoredProjectiles.contains(EntityType.getKey(type).toString()); // fixme use the mod?
    }
}
