package com.github.rcubedev.example.permission.node;

import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.type.PermissionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionNode<T> {

    String getPermission();

    PermissionType<T> getType();

    void setInformation(Component readableName, Component description);

    @Nullable Component getReadableName();

    @Nullable Component getDescription();

    Optional<T> resolve(ServerPlayer player, PermissionDynamicContext<?>... context);

    CompletableFuture<Optional<T>> resolve(UUID uuid, PermissionDynamicContext<?>... context);

    @Nullable T resolveFallback(ServerPlayer player, PermissionDynamicContext<?>... context);

    CompletableFuture<@Nullable T> resolveFallback(UUID uuid, PermissionDynamicContext<?>... context);

    static <T> DefaultResolver<T> nullDefaultResolver() {
        return (player, uuid, dynamicContext) -> null;
    }

    @FunctionalInterface
    static interface DefaultResolver<T> {
        /**
         * The default return.
         * <p>
         * Ignored for {@link com.github.rcubedev.example.util.TriState TriState}.{@link com.github.rcubedev.example.util.TriState#DEFAULT TriState#DEFAULT} will be returned instead.
         * @param player the {@link ServerPlayer}, null if offline check
         * @param uuid the UUID
         * @param dynamicContext the dynamic context used by the default resolver
         */
        T resolve(@Nullable ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... dynamicContext);
    }
}
