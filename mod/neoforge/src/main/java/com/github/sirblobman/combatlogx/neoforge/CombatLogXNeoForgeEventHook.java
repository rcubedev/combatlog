package com.github.sirblobman.combatlogx.neoforge;

import com.github.rcubedev.example.neoforge.permissions.node.NeoForgePermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.PermissionHolder;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionManager;
import com.github.sirblobman.combatlogx.expansion.ExpansionRegistryImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CombatLogX.MOD_ID, value = Dist.DEDICATED_SERVER)
public class CombatLogXNeoForgeEventHook {

    @SubscribeEvent
    public static void registerPermissions(PermissionGatherEvent.Nodes e) {
        ICombatLogX combatLogX = CombatLogX.INSTANCE;
        if (combatLogX == null) throw new IllegalStateException("CombatLogX not initialized");

        PermissionHolder holder = combatLogX.getPermissionHolder();
        e.addNodes(convert(holder.nodes()));
    }

    @SubscribeEvent
    public static void loadExpansions(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            ICombatLogX combatLogX = CombatLogX.INSTANCE;
            if (combatLogX == null) throw new IllegalStateException("CombatLogX not initialized");

            ExpansionRegistryImpl registry = ExpansionRegistryImpl.getInstance();
            ExpansionManager expansionManager = combatLogX.getExpansionManager();
            expansionManager.loadExpansions(registry);
        });
    }

    private static Iterable<net.neoforged.neoforge.server.permission.nodes.PermissionNode<?>> convert(Iterable<? extends PermissionNode<?>> nodes) {
        List<net.neoforged.neoforge.server.permission.nodes.PermissionNode<?>> converted = new ArrayList<>();
        for (PermissionNode<?> node : nodes) converted.add(convert(node));
        return converted;
    }

    private static <T> net.neoforged.neoforge.server.permission.nodes.PermissionNode<T> convert(PermissionNode<T> node) {
        return ((NeoForgePermissionNode<T>) node).getHeldNode();
    }
}
