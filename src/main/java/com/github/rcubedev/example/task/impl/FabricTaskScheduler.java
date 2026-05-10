package com.github.rcubedev.example.task.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.server.level.ServerPlayer;

import com.github.rcubedev.example.task.api.Task;
import com.github.rcubedev.example.task.api.TaskOwner;
import com.github.rcubedev.example.task.api.TaskScheduler;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractPlayerTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractRunnableTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractServerTaskDetails;
import com.github.rcubedev.example.task.api.details.abstracts.AbstractTaskDetails;
import com.github.rcubedev.example.task.api.info.TaskInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricTaskScheduler implements TaskScheduler {

    public static FabricTaskScheduler getScheduler() {
        return Holder.INSTANCE;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("TaskScheduler"); // todo maybe change
    // used COWAL for safety during writes since reading happens only on the main thread avoiding synch
    //  overhead & ensuring safe concurrent writes.
    private final Map<TaskType, CopyOnWriteArrayList<ScheduledEntry<?>>> taskQueues;
    private final ConcurrentHashMap<TaskOwner, AtomicInteger> ownerIdCounters = new ConcurrentHashMap<>();

    private FabricTaskScheduler() {
        this.taskQueues = new EnumMap<>(TaskType.class);
        for (TaskType type : TaskType.values()) this.taskQueues.put(type, new CopyOnWriteArrayList<>());
    }

    @Override
    public @NotNull Task schedule(@NotNull AbstractRunnableTaskDetails details) {
        TaskInfo info = details.getInfo();
        return enqueue(info.getType(), details, resolveDelay(info), resolvePeriod(info));
    }

    @Override
    public @NotNull Task scheduleServer(@NotNull AbstractServerTaskDetails details) {
        TaskInfo info = details.getInfo();
        return enqueue(info.getType(), details, resolveDelay(info), resolvePeriod(info));
    }

    @Override
    public @NotNull Task schedulePlayer(@NotNull AbstractPlayerTaskDetails details) {
        TaskInfo info = details.getInfo();
        return enqueue(info.getType(), details, resolveDelay(info), resolvePeriod(info));
    }

    @Override
    public void cancelTask(@NotNull TaskOwner owner, int id) {
        for (CopyOnWriteArrayList<ScheduledEntry<?>> list : taskQueues.values()) {
            for (ScheduledEntry<?> entry : list) {
                TaskImpl task = entry.getTask();
                if (task.getOwner() == owner && task.getId() == id) {
                    task.cancel();
                    return;
                }
            }
        }
    }

    @Override
    public void cancelTasks(@NotNull TaskOwner owner) {
        for (CopyOnWriteArrayList<ScheduledEntry<?>> list : taskQueues.values()) cancelInList(list, owner);
    }

    @Override
    public void cancelTasks(@NotNull TaskOwner owner, @NotNull TaskType type) {
        cancelInList(taskQueues.get(type), owner);
    }

    private <T extends AbstractTaskDetails> @NotNull Task enqueue(@NotNull TaskType type, @NotNull T details, long delay, long period) {
        TaskOwner owner = details.getInfo().getOwner();
        int id = nextIdFor(owner);
        TaskImpl task = new TaskImpl(owner, id);
        ScheduledEntry<T> entry = new ScheduledEntry<>(type, details, task, delay, period);
        taskQueues.get(type).add(entry);
        return task;
    }

    /**
     * Execute and prune a server-tick bucket.
     */
    public void fireTasks(@NotNull TaskType type, @NotNull TickContext ctx) {
        // fixme if dispatching server task from ServerPlayer it will dispatch repeating tasks too many times & too early,
        //  it does for each player instead of just once.
        CopyOnWriteArrayList<ScheduledEntry<?>> list = taskQueues.get(type);
        List<ScheduledEntry<?>> toRemove = new ArrayList<>();

        for (ScheduledEntry<?> entry : list) {
            if (entry.isCancelled()) {
                toRemove.add(entry);
                continue;
            }
            if (!entry.tick()) continue;

            try {
                AbstractTaskDetails details = entry.getDetails();
                switch (details) {
                    case AbstractPlayerTaskDetails playerTask -> {
                        for (ServerPlayer p : ctx.playerTargets()) playerTask.accept(p);
                    }
                    case AbstractRunnableTaskDetails runnableTask -> runnableTask.run();
                    case AbstractServerTaskDetails serverTask -> serverTask.accept(ctx.server());
                    default -> throw new IllegalArgumentException("Unknown task type: " + details);
                }
            } catch (Exception ex) {
                LOGGER.error("Exception while executing task in bucket {}", type, ex);
            }

            if (!entry.reset()) {
                toRemove.add(entry);
            }
        }

        list.removeAll(toRemove);
    }

    private static long resolveDelay(@NotNull TaskInfo details) {
        Long delay = details.getDelay();
        return delay != null ? Math.max(0L, delay) : 0L;
    }

    private static long resolvePeriod(@NotNull TaskInfo details) {
        Long period = details.getPeriod();
        return period != null && period > 0 ? period : 0L;
    }

    private int nextIdFor(@NotNull TaskOwner owner) {
        return ownerIdCounters.computeIfAbsent(owner, k -> new AtomicInteger(0)).incrementAndGet();
    }

    private void cancelInList(@NotNull CopyOnWriteArrayList<ScheduledEntry<?>> list, @NotNull TaskOwner owner) {
        for (ScheduledEntry<?> entry : list) {
            if (entry.getOwner() == owner) {
                entry.getTask().cancel();
            }
        }
    }

    private static class Holder {
        private static final FabricTaskScheduler INSTANCE = new FabricTaskScheduler();
    }
}
