package com.github.rcubedev.example.neoforge.permissions.node;

import com.github.rcubedev.example.compat.luckperms.LPIntegration;
import com.github.rcubedev.example.permission.context.PermissionDynamicContext;
import com.github.rcubedev.example.permission.context.PermissionDynamicContextKey;
import com.github.rcubedev.example.permission.node.PermissionNode;
import com.github.rcubedev.example.permission.type.PermissionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NeoForgePermissionNode<T> implements PermissionNode<T> {
    public static final PermissionDynamicContextKey<Boolean> RESOLVE_DEFAULT_KEY = new PermissionDynamicContextKey<>(Boolean.class, "INTERNAL:use_default_resolver", Object::toString);

    private final net.neoforged.neoforge.server.permission.nodes.PermissionNode<T> node;
    private final PermissionType<T> permissionType;

    public NeoForgePermissionNode(String modId, String nodeName, PermissionType<T> permissionType, DefaultResolver<T> defaultResolver, PermissionDynamicContextKey<?>... dynamics) {
        PermissionDynamicContextKey<?>[] addedDynamics = new PermissionDynamicContextKey<?>[dynamics.length + 1];
        System.arraycopy(dynamics, 0, addedDynamics, 1, dynamics.length);
        addedDynamics[0] = RESOLVE_DEFAULT_KEY;

        this.node = new net.neoforged.neoforge.server.permission.nodes.PermissionNode<>(
                modId,
                nodeName,
                toLoaderType(permissionType),
                (player, uuid, ctx) -> {
                    PermissionDynamicContext<?>[] context = fromLoaderType(ctx);
                    for (PermissionDynamicContext<?> c : context) {
                        if (RESOLVE_DEFAULT_KEY.equals(c.getDynamic())) {
                            @SuppressWarnings("unchecked")
                            PermissionDynamicContext<Boolean> resolve = (PermissionDynamicContext<Boolean>) c;
                            // breaks NeoForge API contract, but we don't need to call default resolver.
                            //  and it only returns null when resolved by this wrapper when requesting to
                            //  resolve no default
                            if (!resolve.getValue()) return null; // todo this will stop signal key from invoking? do we even need signal key anymore?
                            break;
                        }
                    }
                    return defaultResolver.resolve(player, uuid, context);
                }, toLoaderType(addedDynamics));
        this.permissionType = permissionType;
    }

    private T callResolver(Resolver<T> resolver, boolean useDefaultResolver, PermissionDynamicContext<?>... context) {
        final int prependSpace = 1;
        PermissionDynamicContext<?>[] addedContext = new PermissionDynamicContext<?>[context.length + prependSpace];
        System.arraycopy(context, 0, addedContext, prependSpace, context.length);

        PermissionDynamicContext<Boolean> resolve = RESOLVE_DEFAULT_KEY.createContext(useDefaultResolver);
        addedContext[0] = resolve;
        return resolver.apply(addedContext);
    }

    private Optional<T> resolveNoDefault(Resolver<T> resolver, PermissionDynamicContext<?>... context) {
        T result = callResolver(resolver, false, context);
        return Optional.ofNullable(result);
    }

    @Override
    public String getPermission() {
        return getHeldNode().getNodeName();
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
    @Nullable
    public Component getReadableName() {
        return getHeldNode().getReadableName();
    }

    @Override
    @Nullable
    public Component getDescription() {
        return getHeldNode().getDescription();
    }

    @Override
    public Optional<T> resolve(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return resolveNoDefault(c ->
                PermissionAPI.getPermission(player, getHeldNode(), toLoaderType(c)), context);
    }

    @Override
    public CompletableFuture<Optional<T>> resolve(UUID uuid, PermissionDynamicContext<?>... context) {
        Optional<T> result = resolveNoDefault(c ->
                PermissionAPI.getOfflinePermission(uuid, getHeldNode(), toLoaderType(c)), context);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public T resolveFallback(ServerPlayer player, PermissionDynamicContext<?>... context) {
        return callResolver(c -> PermissionAPI.getPermission(player, getHeldNode(), toLoaderType(c)),
                true, context);
        // return PermissionAPI.getPermission(player, getHeldNode(), toLoaderType(context));
    }

    @Override
    public CompletableFuture<T> resolveFallback(UUID uuid, PermissionDynamicContext<?>... context) {
        if (ModList.get().isLoaded("luckperms")) LPIntegration.ensureUserLoaded(uuid, null);
        // return CompletableFuture.completedFuture(PermissionAPI.getOfflinePermission(uuid, getHeldNode(), toLoaderType(context)));
        T result = callResolver(c ->
                        PermissionAPI.getOfflinePermission(uuid, getHeldNode(), toLoaderType(c)),
                true, context);
        return CompletableFuture.completedFuture(result);
    }

    public net.neoforged.neoforge.server.permission.nodes.PermissionNode<T> getHeldNode() {
        return node;
    }

    @SuppressWarnings("unchecked")
    private static <T> net.neoforged.neoforge.server.permission.nodes.PermissionType<T> toLoaderType(PermissionType<T> permissionType) {
        net.neoforged.neoforge.server.permission.nodes.PermissionType<?> loaderType = PermissionTypes.getTypeByName(permissionType.typeName());

        if (loaderType == null) throw new IllegalArgumentException("Unsupported permission type for NeoForge: " + permissionType.typeName());

        return (net.neoforged.neoforge.server.permission.nodes.PermissionType<T>) loaderType;
    }

    private static <T> PermissionDynamicContextKey<T> fromLoaderType(net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<T> dynamic) {
        return new PermissionDynamicContextKey<>(dynamic.typeToken(), dynamic.name(), dynamic.serializer());
    }

    private static PermissionDynamicContextKey<?>[] fromLoaderType(net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<?>... dynamics) {
        PermissionDynamicContextKey<?>[] newDynamics = new PermissionDynamicContextKey[dynamics.length];
        for (int i = 0; i < dynamics.length; i++) {
            newDynamics[i] = fromLoaderType(dynamics[i]);
        }
        return newDynamics;
    }

    private static <T> PermissionDynamicContext<T> fromLoaderType(net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext<T> dynamic) {
        PermissionDynamicContextKey<T> key = fromLoaderType(dynamic.getDynamic());
        return key.createContext(dynamic.getValue());
    }

    private static PermissionDynamicContext<?>[] fromLoaderType(net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext<?>... dynamics) {
        PermissionDynamicContext<?>[] newDynamics = new PermissionDynamicContext<?>[dynamics.length];
        for (int i = 0; i < dynamics.length; i++) {
            newDynamics[i] = fromLoaderType(dynamics[i]);
        }
        return newDynamics;
    }

    private static <T> net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<T> toLoaderType(PermissionDynamicContextKey<T> dynamic) {
        return new net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<>(dynamic.typeToken(), dynamic.name(), dynamic.serializer());
    }

    private static net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<?>[] toLoaderType(PermissionDynamicContextKey<?>... dynamics) {
        net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<?>[] loaderDynamics = new net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey[dynamics.length];
        for (int i = 0; i < dynamics.length; i++) {
            loaderDynamics[i] = toLoaderType(dynamics[i]);
        }
        return loaderDynamics;
    }

    private static <T> net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext<T> toLoaderType(PermissionDynamicContext<T> dynamic) {
        net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey<T> key = toLoaderType(dynamic.getDynamic());
        return key.createContext(dynamic.getValue());
    }

    private static net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext<?>[] toLoaderType(PermissionDynamicContext<?>... dynamics) {
        net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext<?>[] newDynamics = new net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext[dynamics.length];
        for (int i = 0; i < dynamics.length; i++) {
            newDynamics[i] = toLoaderType(dynamics[i]);
        }
        return newDynamics;
    }

    @FunctionalInterface
    public interface Resolver<T> {
        T apply(@NotNull PermissionDynamicContext<?>... dynamics);
    }
}
