package com.github.rcubedev.example.event.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * A handle used to temporarily disable the recursion guard.
 * Must be used within a try-with-resources block.
 */
@ApiStatus.NonExtendable
public interface RecursionBypass extends AutoCloseable {
    @Override
    void close();
}