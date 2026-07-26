package com.github.rcubedev.example.fabric.permissions.node.factory;

import com.github.rcubedev.example.fabric.permissions.node.StringPermissionNode;
import com.github.rcubedev.example.fabric.permissions.node.TriStatePermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNodeFactory;
import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import com.github.rcubedev.example.platform.IPermissionNodeFactory;
import com.github.rcubedev.example.util.TriState;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Fabric server-side implementation of the permission node factory.
 */
public class FabricPermissionNodeFactory<T> implements IPermissionNodeFactory<T> {

    private static final Map<PermissionType<?>, PermissionNodeFactory<?>> FACTORIES;

    static {
        // fixme parse the str input in future via minimsg
        FACTORIES = Map.ofEntries(
                basicFactory(PermissionTypes.TRISTATE),
                basicFactory(PermissionTypes.STRING),
                IPermissionNodeFactory.mappedFactory(PermissionTypes.INTEGER, PermissionTypes.STRING,
                        str -> str == null ? null : Integer.parseInt(str),
                        IPermissionNodeFactory::toStringOrNull),
                IPermissionNodeFactory.mappedFactory(PermissionTypes.COMPONENT, PermissionTypes.STRING,
                        str -> str == null ? null : Component.literal(str), // fixme parse the str input in future via minimsg
                        IPermissionNodeFactory::toStringOrNull),
                IPermissionNodeFactory.mappedFactory(PermissionTypes.BOOLEAN, PermissionTypes.TRISTATE,
                        TriState::toBoolean, TriState::fromBoolean)
        );
    }

    private final PermissionType<T> permissionType;
    private final PermissionNodeFactory<T> factory;

    @SuppressWarnings("unchecked")
    public FabricPermissionNodeFactory(PermissionType<T> permissionType) {
        this.permissionType = permissionType;
        if (!PermissionTypes.SUPPORTED_TYPES.contains(permissionType)) throw new IllegalArgumentException("Unsupported permission type: " + permissionType.typeName());
        PermissionNodeFactory<?> rawFactory = FACTORIES.get(permissionType);
        if (rawFactory == null) throw new IllegalStateException("No factory registered for supported type: " + permissionType.typeName());
        this.factory = (PermissionNodeFactory<T>) rawFactory;
    }

    @SuppressWarnings("unchecked")
    private static <T> Map.Entry<PermissionType<T>, PermissionNodeFactory<T>> basicFactory(PermissionType<T> permissionType) {
        if (PermissionTypes.TRISTATE.equals(permissionType)) return Map.entry(permissionType, (modId, nodeName, defaultResolver) -> (PermissionNode<T>) new TriStatePermissionNode(modId, nodeName));
        if (PermissionTypes.STRING.equals(permissionType)) return Map.entry(permissionType, (modId, nodeName, defaultResolver) -> (PermissionNode<T>) new StringPermissionNode(modId, nodeName, (PermissionNode.DefaultResolver<String>) defaultResolver));
        if (PermissionTypes.BASE_FABRIC_TYPES.contains(permissionType)) throw new IllegalStateException("Base permission type is not supported on Fabric: " + permissionType.typeName());
        throw new IllegalArgumentException("Unsupported permission type for Fabric: " + permissionType.typeName());
    }

    @Override
    public PermissionType<T> permissionType() {
        return permissionType;
    }

    @Override
    public PermissionNode<T> create(String modId, String nodeName, PermissionNode.DefaultResolver<T> defaultResolver) {
        return factory.create(modId, nodeName, defaultResolver);
    }
}
