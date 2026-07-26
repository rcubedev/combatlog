package com.github.sirblobman.combatlogx.fabric;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.fabric.impl.bukkiteventcompat.FabricEventHook;
import net.fabricmc.api.DedicatedServerModInitializer;

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
        new CombatLogX().onLoad();
        //new CombatLogX().onEnable();
    }
}
