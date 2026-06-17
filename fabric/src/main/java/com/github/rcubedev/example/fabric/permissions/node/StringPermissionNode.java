package com.github.rcubedev.example.fabric.permissions.node;

import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import me.lucko.fabric.api.permissions.v0.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StringPermissionNode implements PermissionNode<String> {
    private final String key;
    private final DefaultResolver<String> defaultResolver;
    private Component readableName; // not currently used anywhere, for parity with neo w/o ret null
    private Component description; // not currently used anywhere, for parity with neo w/o ret null

    public StringPermissionNode(String modId, String nodeName, DefaultResolver<String> defaultResolver) {
        this(modId + "." + nodeName, defaultResolver);
    }

    public StringPermissionNode(String key, DefaultResolver<String> defaultResolver) {
        this.key = key;
        this.defaultResolver = defaultResolver;
    }

    @Override
    public String getPermission() {
        return key;
    }

    @Override
    public PermissionType<String> getType() {
        return PermissionTypes.STRING;
    }

    @Override
    public void setInformation(Component readableName, Component description) {
        this.readableName = readableName;
        this.description = description;
    }

    @Nullable
    @Override
    public Component getReadableName() {
        return readableName;
    }

    @Nullable
    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public Optional<String> resolve(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return Options.get(player, getPermission());
    }

    @Override
    public CompletableFuture<Optional<String>> resolve(UUID uuid, PermissionDynamicContext<?>... context) {
        return Options.get(uuid, getPermission());
    }

    @Override
    public String resolveFallback(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return resolve(player, context).orElse(defaultResolver.resolve(player, player.getUUID(), context));
    }

    @Override
    public CompletableFuture<String> resolveFallback(UUID uuid, PermissionDynamicContext<?>... context) {
        return resolve(uuid, context).thenApply(opt -> opt.orElse(defaultResolver.resolve(null, uuid, context)));
    }
}
