package com.github.rcubedev.example.util;

import com.github.sirblobman.combatlogx.CombatLogX;

/**
 * A generic service manager that can be shared across different helper interfaces.
 */
public interface IService {

    static <T extends IService> T createInstance(Class<T> clazz) {
        return CombatLogX.SERVICE_REGISTRY.require(clazz).get();
    }
}
