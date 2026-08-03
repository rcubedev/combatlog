package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.config.WrappedConfigAccessor;
import com.github.rcubedev.example.util.IService;
import folk.sisby.kaleido.api.WrappedConfig;

public interface IConfigurationHandler extends IService {

    /**
     * Configuration handler instance getter.
     *
     * @return The configuration handler instance.
     */
    static IConfigurationHandler getInstance() {
        return Holder.INSTANCE;
    }

    <T extends WrappedConfig> WrappedConfigAccessor getAccessor(T config);

    static class Holder {
        private static IConfigurationHandler INSTANCE = IService.createInstance(IConfigurationHandler.class);
    }
}
