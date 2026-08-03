package com.github.sirblobman.combatlogx.api.manager;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface ICrystalManager extends ICombatLogXNeeded {
    @Nullable ServerPlayer getPlacer(EndCrystal crystal);

    void setPlacer(@NotNull EndCrystal crystal, @NotNull ServerPlayer player);

    void remove(@NotNull UUID crystalId);
}
