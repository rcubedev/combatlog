package com.github.sirblobman.combatlogx.listener;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;


import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.configuration.PunishConfiguration;
import com.github.sirblobman.combatlogx.api.event.PlayerPunishEvent;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.object.UntagReason;

public final class PunishListener extends CombatListener {
    public PunishListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.NORMAL, ignoreCancelled = true)
    public void beforePunish(PlayerPunishEvent e) {
        UntagReason untagReason = e.getPunishReason();
        if (shouldPunishForReason(untagReason)) return;

        e.cancel();
    }

    private boolean shouldPunishForReason(UntagReason reason) {
        ICombatLogX mod = getCombatLogX();
        PunishConfiguration punishConfiguration = mod.getPunishConfiguration();

        if (reason.isExpire()) return punishConfiguration.onExpire;

        if (reason == UntagReason.KICK) return punishConfiguration.onKick;

        if (reason == UntagReason.QUIT) return punishConfiguration.onDisconnect;

        return false;
    }
}
