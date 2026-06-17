package com.github.rcubedev.example.util;

import com.github.rcubedev.example.services.api.spi.Eager;
import com.github.sirblobman.combatlogx.CombatLogX;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A generic service manager that can be shared across different helper interfaces.
 */
public interface IService {

    // fixme this does not cache!!!!
    static <T extends IService> T createInstance(Class<T> clazz) {
        return CombatLogX.SERVICE_REGISTRY.require(clazz).get();
    }

    /**
     * Singleton instance getter.
     * Blocks until the instance is available.
     * Extending interfaces should wrap this method.
     *
     * @return The singleton instance.
     */
    static <T extends IService> T getInstance(CompletableFuture<T> instanceFuture) {
        try {
            return instanceFuture.get(2, TimeUnit.SECONDS); // Blocks until the instance is set, throws aftet 2s
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to get singleton instance (exception: " + e.getClass().getSimpleName() + ", future done: " + instanceFuture.isDone() + ")", e);
        }
    }

    /**
     * Sets the singleton instance.
     * Extending interfaces should wrap this method.
     *
     * @param helper The singleton instance.
     */
    static <T extends IService> void setInstance(CompletableFuture<T> instanceFuture, T helper) {
        if (instanceFuture.isDone()) {
            throw new IllegalStateException("Singleton instance is already set");
        }
        instanceFuture.complete(helper);  // Completes the future with the provided instance
    }
}
