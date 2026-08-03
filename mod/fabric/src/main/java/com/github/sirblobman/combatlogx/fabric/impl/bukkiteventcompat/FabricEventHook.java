package com.github.sirblobman.combatlogx.fabric.impl.bukkiteventcompat;

import com.github.rcubedev.example.event.server.lifecycle.ServerStartedEvent;
import com.github.rcubedev.example.event.server.lifecycle.ServerStartingEvent;
import com.github.rcubedev.example.event.server.lifecycle.ServerStoppedEvent;
import com.github.rcubedev.example.event.server.lifecycle.ServerStoppingEvent;
import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.EntityDeathEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerJoinEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerQuitEvent;
import com.github.sirblobman.combatlogx.api.bukkiteventcompat.PlayerRespawnEvent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

@SuppressWarnings("UnstableApiUsage")
public class FabricEventHook {

    public void register() {
        ServerPlayerEvents.JOIN.register(this::fireJoinEvent);
        ServerPlayerEvents.LEAVE.register(this::fireQuitEvent);
        ServerLivingEntityEvents.AFTER_DEATH.register(this::fireDeathEvent);
        ServerPlayerEvents.AFTER_RESPAWN.register(this::fireRespawnEvent);
        ServerLifecycleEvents.SERVER_STARTING.register(this::fireStartingEvent);
        ServerLifecycleEvents.SERVER_STARTED.register(this::fireStartedEvent);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::fireStoppingEvent);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::fireStoppedEvent);
        ServerTickEvents.START_SERVER_TICK.register(this::handleSchedulerServerPreTick);
        ServerTickEvents.END_SERVER_TICK.register(this::handleSchedulerServerPostTick);
    }

    public void fireJoinEvent(ServerPlayer player) {
        PlayerJoinEvent event = new PlayerJoinEvent(player);
        event.dispatch();
    }

    public void fireQuitEvent(ServerPlayer player) {
        PlayerQuitEvent event = new PlayerQuitEvent(player);
        event.dispatch();
    }

    public void fireDeathEvent(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer) return; // no-op; PlayerDeathEvent dispatched by mixin
        EntityDeathEvent event = new EntityDeathEvent(entity, source);
        event.dispatch();
    }

    public void fireRespawnEvent(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        PlayerRespawnEvent event = new PlayerRespawnEvent(oldPlayer);
        event.dispatch();
    }

    public void fireStartingEvent(MinecraftServer server) {
        ServerStartingEvent event = new ServerStartingEvent(server);
        event.dispatch();
    }

    public void fireStartedEvent(MinecraftServer server) {
        ServerStartedEvent event = new ServerStartedEvent(server);
        event.dispatch();
    }

    public void fireStoppingEvent(MinecraftServer server) {
        ServerStoppingEvent event = new ServerStoppingEvent(server);
        event.dispatch();
    }

    public void fireStoppedEvent(MinecraftServer server) {
        ServerStoppedEvent event = new ServerStoppedEvent(server);
        event.dispatch();
    }

    //todo: make tick events, move to common
    public void handleSchedulerServerPreTick(MinecraftServer server) {
        ModdedTaskScheduler scheduler = ModdedTaskScheduler.getScheduler();
        scheduler.fireTasks(TaskType.START_TICK, TickContext.ofServer(server));
    }

    public void handleSchedulerServerPostTick(MinecraftServer server) {
        ModdedTaskScheduler scheduler = ModdedTaskScheduler.getScheduler();
        scheduler.fireTasks(TaskType.END_TICK, TickContext.ofServer(server));
    }
}
