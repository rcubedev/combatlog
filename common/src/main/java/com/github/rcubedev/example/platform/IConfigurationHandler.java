package com.github.rcubedev.example.platform;

import com.github.rcubedev.example.config.WrappedConfigAccessor;
import com.github.rcubedev.example.util.IService;
import folk.sisby.kaleido.api.WrappedConfig;

import java.util.concurrent.CompletableFuture;

public interface IConfigurationHandler extends IService {

    /**
     * Configuration handler instance getter.
     * Blocks until the configuration handler instance is available.
     *
     * @return The configuration handler instance.
     */
    static IConfigurationHandler getInstance() {
        return IService.createInstance(IConfigurationHandler.class);
    }

    <T extends WrappedConfig> WrappedConfigAccessor getAccessor(T config);
}
