package com.github.sirblobman.combatlogx.api.utility;

import java.util.function.Function;

public record Mapping<I, O>(String path, Function<I, O> mapper) {
}
