package com.github.rcubedev.example.permission.node;

import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.type.PermissionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MappedPermissionNode<T, B> implements PermissionNode<T> {

    private final PermissionNode<B> node;
    private final PermissionType<T> permissionType;
    private final Function<B, T> transformer;

    public MappedPermissionNode(PermissionNode<B> node, PermissionType<T> permissionType, Function<B, T> transformer) {
        this.node = node;
        this.permissionType = permissionType;
        this.transformer = transformer;
    }

    @Override
    public String getPermission() {
        return getHeldNode().getPermission();
    }

    @Override
    public PermissionType<T> getType() {
        return permissionType;
    }

    @Override
    public void setInformation(Component readableName, Component description) {
        getHeldNode().setInformation(readableName, description);
    }

    @Override
    public @Nullable Component getReadableName() {
        return getHeldNode().getReadableName();
    }

    @Override
    public @Nullable Component getDescription() {
        return getHeldNode().getDescription();
    }

    @Override
    public Optional<T> resolve(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return getHeldNode().resolve(player, context).map(transformer::apply);
    }

    @Override
    public CompletableFuture<Optional<T>> resolve(UUID uuid, PermissionDynamicContext<?>... context) {
        return getHeldNode().resolve(uuid, context).thenApply(opt -> opt.map(transformer::apply));
    }

    @Override
    public T resolveFallback(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return transformer.apply(getHeldNode().resolveFallback(player, context));
    }

    @Override
    public CompletableFuture<T> resolveFallback(UUID uuid, PermissionDynamicContext<?>... context) {
        return getHeldNode().resolveFallback(uuid, context).thenApply(transformer::apply);
    }

    public final PermissionNode<B> getHeldNode() {
        return node;
    }
}
