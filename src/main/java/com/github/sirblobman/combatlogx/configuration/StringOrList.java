package com.github.sirblobman.combatlogx.configuration;

import java.util.List;

import folk.sisby.kaleido.lib.quiltconfig.api.values.ConfigSerializableObject;

public final class StringOrList extends ElementOrList<String> implements ConfigSerializableObject<Object> {

    public StringOrList(String value) {
        super(value, String.class);
    }

    public StringOrList(List<String> values) {
        super(values, String.class);
    }
}
