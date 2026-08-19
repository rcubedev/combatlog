package com.github.rcubedev.example.neoforge;

import com.github.rcubedev.example.platform.IConfigurationHandler;
import com.github.rcubedev.utils.config.WrappedConfigAccessor;
import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class NeoForgeConfigurationHandler implements IConfigurationHandler {

    private static final MethodHandle ACCESSOR;

    static {
        if (!folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig.class.isAssignableFrom(WrappedConfig.class))
            throw new IllegalStateException("Kaleido WrappedConfig does not extend QuiltConfig WrappedConfig");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig> clazz = folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig.class;
        try {
            Field wrapped = clazz.getDeclaredField("wrapped");
            if (!wrapped.trySetAccessible()) {
                lookup = MethodHandles.privateLookupIn(folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig.class, lookup);
            }
            ACCESSOR = lookup.unreflectGetter(wrapped);
            // ACCESSOR = MethodHandles.explicitCastArguments(accessor, accessor.type().changeParameterType(0, WrappedConfig.class));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("QuiltConfig Wrapped Config does not have expected internal field", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access Quilt Config WrappedConfig internal field", e);
        }
    }

    @Override
    public <T extends WrappedConfig> WrappedConfigAccessor getAccessor(T config) {
        return () -> {
            try {
                return (Config) ACCESSOR.invokeExact((folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig) config);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}
