package com.github.sirblobman.combatlogx.task;

import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import com.github.rcubedev.example.task.api.info.EntityTaskInfo;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import org.jetbrains.annotations.NotNull;

public final class PlayerVulnerableTask extends AbstractRunnableTaskDetails {
    public PlayerVulnerableTask(@NotNull ICombatLogX mod, @NotNull ServerPlayer entity) {
        super(new EntityTaskInfo<>(mod, TaskType.START_TICK, entity)); // todo is this right
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull EntityTaskInfo<ServerPlayer> getInfo() {
        return (EntityTaskInfo<ServerPlayer>) super.getInfo();
    }

    @Override
    public void run() {
        EntityTaskInfo<ServerPlayer> info = getInfo();
        ServerPlayer entity = info.getEntity();
        if (entity != null) entity.invulnerableTime = 0;
    }
}
