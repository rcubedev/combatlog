package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.permission.node.MappedPermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.node.PermissionNodeFactory;
import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import com.github.rcubedev.example.util.IService;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory interface to create permission node instances without depending on loader-specific code.
 * @param <T> Provide to restrict implementing factory to a specific type
 */
public interface IPermissionNodeFactory<T> {

    static <T, B> Map.Entry<PermissionType<T>, PermissionNodeFactory<T>> mappedFactory(PermissionType<T> permissionType, PermissionType<B> basePermissionType, Function<B, T> transformer, Function<T, B> inverseTransformer) {
        return Map.entry(permissionType, (modId, nodeName, defaultResolver) -> new MappedPermissionNode<>(
                IPermissionFactoryManager.getInstance().getFactory(basePermissionType).create(modId, nodeName, (player, uuid, ctx) -> inverseTransformer.apply(defaultResolver.resolve(player, uuid, ctx))),
                permissionType,
                transformer));
    }

    static @Nullable String toStringOrNull(@Nullable Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * Override to null if factory can create multiple types, else return the class
     * @return the permission return type this factory can create
     */
    PermissionType<T> permissionType();

    /**
     * Creates a permission node.
     *
     * @param modId the modId of the permission node
     * @param nodeName the name of the permission node
     * @param defaultResolver the default resolver for NeoForge
     * @return the {@link PermissionNode}
     */
    PermissionNode<T> create(String modId, String nodeName, PermissionNode.DefaultResolver<T> defaultResolver);

    /**
     * Creates a permission node. On NeoForge, the default resolver will fallback to null/false
     *
     * @param modId the modId of the permission node
     * @param nodeName the name of the permission node
     * @param permissionType the permission type
     * @return the {@link PermissionNode}
     */
    default PermissionNode<T> create(String modId, String nodeName) {
        return create(modId, nodeName, PermissionNode.nullDefaultResolver());
    }

    /**
     * Creates a permission node.
     *
     * @param nodeName the name of the permission node
     * @param defaultResolver the default resolver for NeoForge
     * @return the {@link PermissionNode}
     */
    default PermissionNode<T> create(ResourceLocation nodeName, PermissionNode.DefaultResolver<T> defaultResolver) {
        return create(nodeName.getNamespace(), nodeName.getPath(), defaultResolver);
    }

    /**
     * Creates a permission node. On NeoForge, the default resolver will fallback to null/false
     *
     * @param nodeName the name of the permission node
     * @return the {@link PermissionNode}
     */
    default PermissionNode<T> create(ResourceLocation nodeName) {
        return create(nodeName.getNamespace(), nodeName.getPath());
    }
}
