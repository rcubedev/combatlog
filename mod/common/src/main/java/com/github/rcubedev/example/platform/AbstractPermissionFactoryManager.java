package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.permission.type.PermissionType;
import com.github.rcubedev.example.permission.type.PermissionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AbstractPermissionFactoryManager implements IPermissionFactoryManager {

    private final Map<PermissionType<?>, IPermissionNodeFactory<?>> factories = new HashMap<>();
    private final Provider provider;
    private volatile boolean initialized = false;

    protected AbstractPermissionFactoryManager(Provider provider) {
        this.provider = provider;
        // PermissionTypes.SUPPORTED_TYPES.forEach(type -> factories.put(type, provider.createFactory(type)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IPermissionNodeFactory<T> getFactory(@NotNull PermissionType<T> type) {
        Objects.requireNonNull(type, "type cannot be null.");
        ensureInitialized();
        IPermissionNodeFactory<?> factory = factories.get(type);
        if (factory == null) throw new IllegalArgumentException(type + " is not a supported PermissionType.");
        return (IPermissionNodeFactory<T>) factory;
    }

    private void ensureInitialized() {
        if (this.initialized) return;

        synchronized (this.factories) {
            if (!this.initialized) {
                PermissionTypes.SUPPORTED_TYPES.forEach(type ->
                        this.factories.put(type, this.provider.createFactory(type)));
                this.initialized = true;
            }
        }
    }

    @FunctionalInterface
    protected interface Provider {
        <T> IPermissionNodeFactory<T> createFactory(PermissionType<T> type);
    }
}