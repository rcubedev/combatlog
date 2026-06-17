package com.github.rcubedev.example.permission.type;

import com.github.rcubedev.example.util.TriState;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Default permission types.
 * <p>
 * Types should be supported on all loaders. If a loader does not have explicit support for a type, use a {@link com.github.rcubedev.example.permission.node.MappedPermissionNode MappedPermissionNode}
 */
public final class PermissionTypes {
    private PermissionTypes() {}

    public static final PermissionType<TriState> TRISTATE = new PermissionType<>(TriState.class, "tristate");
    public static final PermissionType<Boolean> BOOLEAN = new PermissionType<>(Boolean.class, "boolean");
    public static final PermissionType<Integer> INTEGER = new PermissionType<>(Integer.class, "integer");
    public static final PermissionType<String> STRING = new PermissionType<>(String.class, "string");
    public static final PermissionType<Component> COMPONENT = new PermissionType<>(Component.class, "component");
    public static final List<PermissionType<?>> BASE_FABRIC_TYPES = List.of(TRISTATE);
    public static final List<PermissionType<?>> BASE_NEOFORGE_TYPES = List.of(BOOLEAN, INTEGER, STRING, COMPONENT);
    public static final List<PermissionType<?>> SUPPORTED_TYPES = List.of(TRISTATE, BOOLEAN, INTEGER, STRING, COMPONENT);
}