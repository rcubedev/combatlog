package com.github.sirblobman.combatlogx.listener;

import java.util.UUID;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityDamageByEntityEvent;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ICrystalManager;
import com.github.sirblobman.combatlogx.api.object.TagReason;
import com.github.sirblobman.combatlogx.api.object.TagType;
import org.jetbrains.annotations.NotNull;

public class EndCrystalListener extends CombatListener {

    public EndCrystalListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!getConfiguration().linkEndCrystal) return;

        Entity damaged = e.getEntity();
        if (!(damaged instanceof ServerPlayer player)) return;

        Entity damager = e.getDamager();
        if (!(damager instanceof EndCrystal crystal)) return;

        ICrystalManager crystalManager = getCombatLogX().getCrystalManager();
        ServerPlayer placer = crystalManager.getPlacer(crystal);

        if (placer != null) {
            checkTag(placer, player, TagReason.ATTACKER);
            checkTag(player, placer, TagReason.ATTACKED);
        }

        UUID damagerId = damager.getUUID();
        crystalManager.remove(damagerId);
    }

    private MainConfiguration getConfiguration() {
        ICombatLogX mod = getCombatLogX();
        return mod.getConfiguration();
    }

    private void checkTag(@NotNull ServerPlayer player, @NotNull ServerPlayer enemy, @NotNull TagReason tagReason) {
        ICombatManager combatManager = getCombatManager();
        combatManager.tag(player, enemy, TagType.PLAYER, tagReason);
    }
}
