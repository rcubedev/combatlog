package com.github.sirblobman.combatlogx.fabric;

import com.github.rcubedev.example.task.api.TaskType;
import com.github.rcubedev.example.task.impl.ModdedTaskScheduler;
import com.github.rcubedev.example.task.impl.TickContext;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.fabric.bukkiteventcompat.FabricEventHook;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CombatLogXFabric implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // IPlatformHelper.setInstance(new FabricPlatformHelper());
        // IAdventure.setInstance(new FabricAdventure());
        // IConfigurationHandler.setInstance(new FabricConfigurationHandler());
        // PermissionTypes.SUPPORTED_TYPES.forEach(t -> IPermissionNodeFactory.setInstance(new FabricPermissionNodeFactory<>(t)));
        // IPermissions.setInstance(new FabricPermissions());

        // IPlaceholderAPI.setInstance(FabricLoader.getInstance().isModLoaded("placeholder-api") ? new TextPlaceholderAPIIntegration()
        //         : new PlaceholderAPIFallback());

        new FabricEventHook().register();
        // fixme
        ServerTickEvents.START_SERVER_TICK.register(server -> ModdedTaskScheduler.getScheduler().fireTasks(TaskType.START_TICK, TickContext.ofServer(server)));
        ServerTickEvents.END_SERVER_TICK.register(server -> ModdedTaskScheduler.getScheduler().fireTasks(TaskType.END_TICK, TickContext.ofServer(server)));

        new CombatLogX().onInitializeServer();
    }
}
