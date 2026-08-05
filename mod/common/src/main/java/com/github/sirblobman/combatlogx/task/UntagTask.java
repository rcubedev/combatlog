package com.github.sirblobman.combatlogx.task;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractServerTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.object.UntagReason;
import com.github.sirblobman.combatlogx.listener.UntagEventListener;
import org.jetbrains.annotations.NotNull;

/**
 * This task is used to untag players from combat. It runs every 10 ticks.
 */
public final class UntagTask extends AbstractServerTaskDetails {
    private final ICombatLogX mod;

    public UntagTask(@NotNull ICombatLogX mod) {
        super(new TaskInfo(mod, TaskType.START_TICK)); // todo idk
        this.mod = mod;
    }

    public void register() {
        ICombatLogX mod = getCombatLogX();
        TaskScheduler scheduler = mod.getScheduler();

        TaskInfo info = getInfo();
        info.setDelay(5L);
        info.setPeriod(10L);
        scheduler.scheduleServer(this);
    }

    @Override
    public void accept(@NotNull MinecraftServer server) {
        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = plugin.getCombatManager();
        List<ServerPlayer> playerCombatList = combatManager.getPlayersInCombat(server);

        for (ServerPlayer player : playerCombatList) {
            TagInformation tagInformation = combatManager.getTagInformation(player);
            if (tagInformation != null && tagInformation.isExpired()) {
                combatManager.untag(player, UntagReason.EXPIRE);
                if (UntagEventListener.DISCONNECTED.contains(player)) {
                    ServerGamePacketListenerImpl connection = player.connection;
                    connection.disconnect(Component.empty());
                }
            }
        }
    }

    private @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }
}
