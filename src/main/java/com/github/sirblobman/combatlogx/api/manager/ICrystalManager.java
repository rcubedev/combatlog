package com.github.sirblobman.combatlogx.api.manager;

import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICrystalManager extends ICombatLogXNeeded {
    @Nullable Player getPlacer(Entity crystal);

    void setPlacer(@NotNull Entity crystal, @NotNull Player player);

    void remove(@NotNull UUID crystalId);
}
