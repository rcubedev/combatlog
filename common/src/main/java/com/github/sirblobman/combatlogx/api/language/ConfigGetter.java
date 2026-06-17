package com.github.sirblobman.combatlogx.api.language;

import java.util.function.Function;

public interface ConfigGetter<T extends ILanguage, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        return get(t);
    }

    R get(T t);
}
