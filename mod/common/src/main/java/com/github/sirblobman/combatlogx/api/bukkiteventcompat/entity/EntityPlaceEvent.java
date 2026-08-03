package com.github.sirblobman.combatlogx.api.bukkiteventcompat.entity;

import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Triggered when an entity is created in the world by a player "placing" an item
 * on a block.
 * <p>
 * Note that this event is currently only fired for four specific placements:
 * armor stands, boats, minecarts, and end crystals.
 */
public class EntityPlaceEvent extends EntityEvent {

    private final @Nullable ServerPlayer player;
    private final BlockPos block;
    private final Direction blockFace;
    private final InteractionHand hand;

    @ApiStatus.Internal
    public EntityPlaceEvent(@NotNull Entity entity, @Nullable ServerPlayer player, @NotNull BlockPos block,
                            @NotNull Direction blockFace, @NotNull InteractionHand hand) {
        super(entity);
        this.player = player;
        this.block = block;
        this.blockFace = blockFace;
        this.hand = hand;
    }

    @ApiStatus.Internal
    public EntityPlaceEvent(@NotNull Entity entity, @NotNull UseOnContext context) {
        this(entity, (ServerPlayer) context.getPlayer(), context.getClickedPos(), context.getClickedFace(),
                context.getHand()); // fixme
    }

    /**
     * Returns the player placing the entity
     *
     * @return the player placing the entity
     */
    public @Nullable ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * Returns the block that the entity was placed on
     *
     * @return the block that the entity was placed on
     */
    public @NotNull BlockPos getBlock() {
        return this.block;
    }

    /**
     * Returns the face of the block that the entity was placed on
     *
     * @return the face of the block that the entity was placed on
     */
    public @NotNull Direction getBlockFace() {
        return this.blockFace;
    }

    /**
     * Get the hand used to place the entity.
     *
     * @return the hand
     */
    public @NotNull InteractionHand getHand() {
        return this.hand;
    }
}