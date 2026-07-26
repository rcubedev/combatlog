package com.github.sirblobman.combatlogx.neoforge;

import com.github.sirblobman.combatlogx.CombatLogX;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = CombatLogX.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class CombatLogXNeoForge {

    public CombatLogXNeoForge() {
//        CombatLogX.bootstrap();

//        IPlatformHelper.setInstance(new NeoForgePlatformHelper());
//        IAdventure.setInstance(new NeoForgeAdventure());
//        IConfigurationHandler.setInstance(new NeoForgeConfigurationHandler());
//        PermissionTypes.SUPPORTED_TYPES.forEach(t -> IPermissionNodeFactory.setInstance(new NeoForgePermissionNodeFactory<>(t)));
//        IPlaceholderAPI.setInstance(new PlaceholderAPIFallback());

        new CombatLogX().onLoad();
    }
}
