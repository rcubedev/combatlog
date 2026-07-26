package com.github.rcubedev.example.fabric.permissions.node;

import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import com.github.rcubedev.example.util.TriState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TriStatePermissionNode implements PermissionNode<TriState> {
    private final String permission;
    private Component readableName; // not currently used anywhere, for parity with neo w/o ret null
    private Component description; // not currently used anywhere, for parity with neo w/o ret null

    public TriStatePermissionNode(String modId, String nodeName) {
        this(modId + "." + nodeName);
    }

    public TriStatePermissionNode(String permission) {
        this.permission = permission;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public PermissionType<TriState> getType() {
        return PermissionTypes.TRISTATE;
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
    public Optional<TriState> resolve(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return Optional.of(resolveFallback(player, context));
    }

    @Override
    public CompletableFuture<Optional<TriState>> resolve(UUID uuid, PermissionDynamicContext<?>... context) {
        return resolveFallback(uuid, context).thenApply(Optional::of);
    }

//    fixme add this context fallback idk
    @Override
    public TriState resolveFallback(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return fromFabric(Permissions.getPermissionValue(player, getPermission()));
    }

    @Override
    public CompletableFuture<TriState> resolveFallback(UUID uuid, PermissionDynamicContext<?>... context) {
        return Permissions.getPermissionValue(uuid, getPermission()).thenApply(TriStatePermissionNode::fromFabric);
    }

    @Contract(pure = true)
    private static TriState fromFabric(@NotNull net.fabricmc.fabric.api.util.TriState triState) {
        return switch (triState) {
            case TRUE -> TriState.TRUE;
            case FALSE -> TriState.FALSE;
            case DEFAULT -> TriState.DEFAULT;
        };
    }
}
