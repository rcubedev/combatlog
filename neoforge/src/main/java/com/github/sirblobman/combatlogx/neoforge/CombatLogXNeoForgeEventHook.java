package com.github.sirblobman.combatlogx.neoforge;

import com.github.rcubedev.example.neoforge.permissions.node.NeoForgePermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.PermissionHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CombatLogX.MOD_ID, value = Dist.DEDICATED_SERVER)
public class CombatLogXNeoForgeEventHook {

    @SubscribeEvent
    public static void registerPermissions(PermissionGatherEvent.Nodes e) {
        PermissionHolder holder = CombatLogX.INSTANCE.getPermissionHolder();
        e.addNodes(convert(holder.nodes()));
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
