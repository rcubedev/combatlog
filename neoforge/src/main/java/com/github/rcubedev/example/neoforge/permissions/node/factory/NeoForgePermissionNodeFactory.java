package com.github.rcubedev.example.neoforge.permissions.node.factory;

import com.github.rcubedev.example.neoforge.permissions.node.NeoForgePermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNodeFactory;
import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import com.github.rcubedev.example.platform.IPermissionNodeFactory;
import com.github.rcubedev.example.util.TriState;

import java.util.Map;

/**
 * NeoForge server-side implementation of the permission node factory.
 */
public class NeoForgePermissionNodeFactory<T> implements IPermissionNodeFactory<T> {

    private static final Map<PermissionType<?>, PermissionNodeFactory<?>> FACTORIES;

    static {
        FACTORIES = Map.ofEntries(
                basicFactory(PermissionTypes.BOOLEAN),
                basicFactory(PermissionTypes.INTEGER),
                basicFactory(PermissionTypes.STRING),
                basicFactory(PermissionTypes.COMPONENT),
                IPermissionNodeFactory.mappedFactory(PermissionTypes.TRISTATE, PermissionTypes.BOOLEAN,
                        TriState::fromBoolean, TriState::toBoolean)
        );
    }

    private final PermissionType<T> permissionType;
    private final PermissionNodeFactory<T> factory;

    @SuppressWarnings("unchecked")
    public NeoForgePermissionNodeFactory(PermissionType<T> permissionType) {
        this.permissionType = permissionType;
        if (!PermissionTypes.SUPPORTED_TYPES.contains(permissionType)) throw new IllegalArgumentException("Unsupported permission type: " + permissionType.typeName());
        PermissionNodeFactory<?> rawFactory = FACTORIES.get(permissionType);
        if (rawFactory == null) throw new IllegalStateException("No factory registered for supported type: " + permissionType.typeName());
        this.factory = (PermissionNodeFactory<T>) rawFactory;
    }

    private static <T> Map.Entry<PermissionType<T>, PermissionNodeFactory<T>> basicFactory(PermissionType<T> permissionType) {
        return Map.entry(permissionType, (modId, nodeName, defaultResolver) -> new NeoForgePermissionNode<>(modId, nodeName, permissionType, defaultResolver));
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
