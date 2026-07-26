package com.github.rcubedev.example.fabric;

import com.github.rcubedev.example.config.WrappedConfigAccessor;
import com.github.rcubedev.example.platform.IConfigurationHandler;
import com.github.sirblobman.combatlogx.mixin.WrappedConfigAccessorImpl;
import folk.sisby.kaleido.api.WrappedConfig;

public class FabricConfigurationHandler implements IConfigurationHandler {

    @Override
    public <T extends WrappedConfig> WrappedConfigAccessor getAccessor(T config) {
//        return WrappedConfigAccessorImpl.create(config);
        return create(config);
    }

    static WrappedConfigAccessor create(folk.sisby.kaleido.api.WrappedConfig config) {
        if (!folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig.class.isAssignableFrom(folk.sisby.kaleido.api.WrappedConfig.class))
            throw new IllegalStateException("Kaleido WrappedConfig does not extend QuiltConfig Wrapped Config");
        return (WrappedConfigAccessorImpl) config;
    }
}
