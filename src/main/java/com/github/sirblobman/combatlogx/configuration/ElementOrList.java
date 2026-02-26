package com.github.sirblobman.combatlogx.configuration;

import java.util.List;

import folk.sisby.kaleido.lib.quiltconfig.api.values.ConfigSerializableObject;

public class ElementOrList<T> extends AbstractElementOrList<T, ElementOrList<T>> implements ConfigSerializableObject<Object> {

    public ElementOrList(T value, Class<T> type) {
        super(value, type);
    }

    public ElementOrList(List<T> values, Class<T> type) {
        super(values, type);
    }
}
