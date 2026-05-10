package com.github.sirblobman.combatlogx.api.manager;

import net.minecraft.server.level.ServerPlayer;

import com.github.sirblobman.combatlogx.api.ICombatLogXNeeded;
import com.github.sirblobman.combatlogx.api.object.CombatTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IForgiveManager extends ICombatLogXNeeded {
    boolean getToggleValue(@NotNull ServerPlayer player);

    void setToggle(@NotNull ServerPlayer player, boolean value);

    @Nullable CombatTag getActiveRequest(@NotNull ServerPlayer player);

    void setRequest(@NotNull ServerPlayer player, @NotNull CombatTag request);

    void removeRequest(@NotNull ServerPlayer player);
}
