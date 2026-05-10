package com.github.sirblobman.combatlogx.task;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractServerTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.combatlogx.api.manager.ITimerManager;
import com.github.sirblobman.combatlogx.api.object.TagInformation;
import com.github.sirblobman.combatlogx.api.object.TimerUpdater;
import org.jetbrains.annotations.NotNull;

public final class TimerUpdateTask extends AbstractServerTaskDetails implements ITimerManager {
    private final ICombatLogX mod;
    private final Set<TimerUpdater> timerUpdaterSet;

    public TimerUpdateTask(@NotNull ICombatLogX mod) {
        super(new TaskInfo(mod, TaskType.START_TICK));
        this.mod = mod;
        this.timerUpdaterSet = new HashSet<>();
    }

    @Override
    public @NotNull ICombatLogX getCombatLogX() {
        return this.mod;
    }

    @Override
    public @NotNull Set<TimerUpdater> getTimerUpdaters() {
        return Collections.unmodifiableSet(this.timerUpdaterSet);
    }

    @Override
    public void addUpdaterTask(@NotNull TimerUpdater task) {
        this.timerUpdaterSet.add(task);
    }

    @Override
    public void accept(@NotNull MinecraftServer server) {
        ICombatManager combatManager = this.mod.getCombatManager();
        List<ServerPlayer> playerCombatList = combatManager.getPlayersInCombat(server);
        playerCombatList.forEach(this::update);
    }

    @Override
    public void remove(@NotNull ServerPlayer player) {
        Set<TimerUpdater> timerUpdaterSet = getTimerUpdaters();
        for (TimerUpdater timerUpdater : timerUpdaterSet) {
            timerUpdater.remove(player);
        }
    }

    public void register() {
        ICombatLogX plugin = getCombatLogX();
        TaskScheduler scheduler = plugin.getScheduler();

        TaskInfo info = getInfo();
        info.setDelay(5L);
        info.setPeriod(10L);
        scheduler.scheduleServer(this);
    }

    private void update(@NotNull ServerPlayer player) {
        ICombatLogX plugin = getCombatLogX();
        ICombatManager combatManager = plugin.getCombatManager();
        TagInformation tagInformation = combatManager.getTagInformation(player);
        if (tagInformation == null) {
            return;
        }

        if (tagInformation.isExpired()) {
            return;
        }

        long timeLeftMillis = tagInformation.getMillisLeftCombined();
        Set<TimerUpdater> timerUpdaterSet = getTimerUpdaters();
        for (TimerUpdater timerUpdater : timerUpdaterSet) {
            timerUpdater.update(player, timeLeftMillis);
        }
    }
}
