package com.github.rcubedev.example.event.impl;

import com.github.rcubedev.example.event.api.Event;

public interface UnboundProcessor<T, E extends Event> {
    void process(T target, E event);
}
