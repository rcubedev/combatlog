package com.github.rcubedev.example.permission.context;

import java.util.function.Function;

/**
 * Represents a key that can be used to build a {@link PermissionDynamicContext}.
 * <p>
 * Keys, along with their associated values, can be used to provide additional context for a permission handler
 * in determining whether to grant permission for an actor and a specific node.
 */
public record PermissionDynamicContextKey<T>(Class<T> typeToken, String name, Function<T, String> serializer) {
    public PermissionDynamicContext<T> createContext(T value) {
        return new PermissionDynamicContext<>(this, value);
    }
}
