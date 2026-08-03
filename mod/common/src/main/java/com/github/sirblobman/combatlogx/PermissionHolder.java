package com.github.sirblobman.combatlogx;

import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import com.github.rcubedev.example.platform.IPermissionFactoryManager;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PermissionHolder {
    private final @Nullable PermissionNode<Boolean> bypass;
    private final PermissionNode<Integer> timer;
    private final List<PermissionNode<?>> nodes;

    public PermissionHolder(ICombatLogX mod) {
        // fixme config reloading entirely breaks this idea. will need to mark config side as requires restart
        String bypassPermission = mod.getConfiguration().bypassPermission;
        if (!bypassPermission.isEmpty()) {
            this.bypass = IPermissionFactoryManager.getInstance().getFactory(PermissionTypes.BOOLEAN)
                    .create(parsePermission(bypassPermission));
            this.bypass.setInformation(
                    Component.nullToEmpty("CombatLogX Bypass Permission"),
                    Component.nullToEmpty("Whether the user can bypass combat tagging")
            );
        } else bypass = null;

        this.timer = IPermissionFactoryManager.getInstance().getFactory(PermissionTypes.INTEGER).create(
                ResourceLocation.fromNamespaceAndPath("combatlogx", "timer")
        );
        this.timer.setInformation(
                Component.nullToEmpty("CombatLogX Timer"),
                Component.nullToEmpty("The combat timer max, in seconds.")
        );

        List<PermissionNode<?>> nodes = new ArrayList<>();
        if (bypass != null) nodes.add(bypass);
        nodes.add(timer);

        this.nodes = List.copyOf(nodes);
    }

    private static ResourceLocation parsePermission(String permission) {
        int separator = permission.indexOf('.');

        return ResourceLocation.fromNamespaceAndPath(
                separator == -1 ? "combatlogx" : permission.substring(0, separator),
                separator == -1 ? permission : permission.substring(separator + 1)
        );
    }

    public PermissionNode<Boolean> getBypass() {
        return bypass;
    }

    public PermissionNode<Integer> getTimer() {
        return timer;
    }

    public List<PermissionNode<?>> nodes() {
        return this.nodes;
    }
}
