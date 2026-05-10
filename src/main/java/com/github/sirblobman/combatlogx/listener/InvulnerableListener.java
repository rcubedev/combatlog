package com.github.sirblobman.combatlogx.listener;

import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.event.api.Priority;
import com.github.rcubedev.example.event.api.SubscribeEvent;
import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerJoinEvent;
import com.github.sirblobman.combatlogx.api.configuration.MainConfiguration;
import com.github.sirblobman.combatlogx.api.listener.CombatListener;
import com.github.sirblobman.combatlogx.task.PlayerVulnerableTask;
import org.jetbrains.annotations.NotNull;

// todo do i impl this? it removes respawn immunity etc.
//  for now it won't be utilised. (also will need to register it on the eventbus)
public final class InvulnerableListener extends CombatListener {
    public InvulnerableListener(@NotNull ICombatLogX mod) {
        super(mod);
    }

    @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        checkEvent(e);
    }

    // fixme commented for now for compilation.
    // @SubscribeEvent(priority = Priority.MONITOR, ignoreCancelled = true)
    // // fixme idk how this should be done
    // public void onJoin(PlayerTeleportEvent e) {
    //     checkEvent(e);
    // }

    private void checkEvent(PlayerEvent e) {
        if (isDisabled()) return;

        ServerPlayer player = e.getPlayer();
        setVulnerableLater(player);
    }

    private MainConfiguration getConfiguration() {
        ICombatLogX mod = getCombatLogX();
        return mod.getConfiguration();
    }

    private boolean isDisabled() {
        return true; // fixme for now always disabled
        // MainConfiguration configuration = getConfiguration();
        // return !configuration.removeNoDamageCooldown;
    }

    private void setVulnerableLater(ServerPlayer player) {
        PlayerVulnerableTask task = new PlayerVulnerableTask(getCombatLogX(), player);
        task.getInfo().setDelay(2L);

        TaskScheduler scheduler = getCombatLogX().getScheduler();
        scheduler.schedule(task);
    }
}
