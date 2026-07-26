package com.github.sirblobman.combatlogx.neoforge.api;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.Expansion;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionInitializer;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a CombatLogX expansion.
 * <p>
 * Any class found with this annotation applied will be loaded as an expansion
 * for the mod with the given {@linkplain #value() ID}.
 * <br>
 * The annotated class extend {@link Expansion} and provide a public constructor
 * accepting {@link ICombatLogX} and {@link ExpansionMetadata}.
 * <p>
 * For the initializer-based expansion API, see {@link ExpansionInitializer}.
 * @see ExpansionInitializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CLXExpansion {

    /**
     * The mod id associated with this expansion.
     *
     * @return the owning mod id
     */
    String value();
}
