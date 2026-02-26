package com.github.sirblobman.combatlogx.api.manager;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.sirblobman.combatlogx.api.IAntiLogoutNeeded;

public interface ICrystalManager extends IAntiLogoutNeeded {
    @Nullable Player getPlacer(@NotNull Entity crystal);

    void setPlacer(@NotNull Entity crystal, @NotNull Player player);

    void remove(@NotNull UUID crystalId);
}
