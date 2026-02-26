package com.github.sirblobman.combatlogx.configuration;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.utility.StringHelper;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ConfigSerializableObject;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for objects in the config system that can store either a single element of type {@link T} or a collection of elements.
 *
 * @implSpec Subclasses must provide constructors matching the signatures required by{@link #newInstance(T)} and {@link #newInstance(List)}
 * @param <T> the element type
 * @param <S> the subclass type
 */
public abstract class AbstractElementOrList<T, S extends AbstractElementOrList<T, S>> implements ConfigSerializableObject<Object> {

    private final Object value; // Can hold either T or a ValueList<T>
    private final Class<T> type;

    /**
     * Single-value constructor.
     * Initializes the element with a single value of type {@link T}.
     *
     * @param value the value to store
     * @param type the Class object of {@link T}
     */
    public AbstractElementOrList(T value, Class<T> type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Collection constructor.
     * Initializes the element with a list of values of type {@link T}.
     *
     * @param values the list of values to store
     * @param type the Class object of {@link T}
     */
    public AbstractElementOrList(List<@NotNull T> values, Class<T> type) {
        this(values.toArray((T[]) Array.newInstance(type, 0)), type);
    }

    // Internal ctor
    private AbstractElementOrList(T @NotNull [] values, Class<T> type) {
        if (values.length == 0) throw new IllegalArgumentException("values must not be empty, first element must be a minimal object to identify the type");
        this.value = ValueList.create(values[0], values);
        this.type = type;
    }

    /**
     * Returns the Class object of {@link T}.
     *
     * @return the type of the elements
     */
    protected Class<T> getType() {
        return this.type;
    }

    /**
     * Creates a new instance of {@link S} using the single-value constructor.
     * Subclasses must have a matching constructor.
     *
     * @param value the single value to pass to the constructor
     * @return a new instance of {@link S}
     * @throws IllegalStateException if no matching constructor exists
     * @implSpec This method uses reflection to create a new instance of the runtime subclass {@link S}.
     *           The subclass must have a constructor accepting {@link T} else {@link IllegalStateException} is thrown.
     */
    protected @NotNull S newInstance(T value) {
        try {
            return invokeMatchingConstructor(value);
        } catch (IllegalStateException firstEx) {
            try {
                logFallback(this.getClass(), value.getClass(), getType());
                return invokeMatchingConstructor(value, getType());
            } catch (IllegalStateException ignored) {
                throw firstEx;
            }
        }
    }

    /**
     * Creates a new instance of {@link S} using the collection constructor.
     * Subclasses must have a matching constructor.
     *
     * @param values the collection of values to pass to the constructor
     * @return a new instance of {@link S}
     * @throws IllegalStateException if no matching constructor exists
     * @implSpec This method uses reflection to create a new instance of the runtime subclass {@link S}.
     *           The subclass must have a constructor accepting {@link List<T>} else {@link IllegalStateException} is thrown.
     */
    protected @NotNull S newInstance(List<T> values) {
        try {
            return invokeMatchingConstructor(values);
        } catch (IllegalStateException firstEx) {
            try {
                logFallback(this.getClass(), List.class, getType());
                return invokeMatchingConstructor(values, getType());
            } catch (IllegalStateException ignored) {
                throw firstEx;
            }
        }
    }

    /**
     * Attempts to instantiate the runtime subclass using the best-matching public
     * constructor for the given arguments.
     * <p>
     * Constructor selection prefers the closest match in the class hierarchy
     * (exact matches over superclasses). Primitive parameters are matched against
     * their boxed equivalents.
     *
     * @param args the constructor arguments
     * @return a new instance of {@link S}
     * @throws IllegalStateException if no compatible constructor exists
     * @throws RuntimeException if constructor invocation fails
     */

    protected final @NotNull S invokeMatchingConstructor(Object... args) {
        Class<?> clazz = this.getClass();
        Constructor<?> bestCtor = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Constructor<?> ctor : clazz.getConstructors()) {
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (!parametersMatch(paramTypes, args)) continue;

            int distance = 0;
            for (int i = 0; i < paramTypes.length; i++) {
                Object arg = args[i];
                if (arg == null) continue; // null is ambiguous, count 0
                distance += getClassDistance(arg.getClass(), wrapPrimitive(paramTypes[i]));
            }

            if (distance < bestDistance) {
                bestDistance = distance;
                bestCtor = ctor;
            }
        }

        if (bestCtor != null) {
            try {
                return (S) bestCtor.newInstance(args);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to invoke constructor " + bestCtor, e);
            }
        }

        // If no constructor found, throw informative exception
        StringBuilder sb = new StringBuilder();
        sb.append("Subclass must have a constructor taking (");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i] == null ? "any type" : args[i].getClass().getSimpleName());
        }
        sb.append(")");
        throw new IllegalStateException(sb.toString());
    }

    /**
     * Determines whether this class explicitly allows reflective constructor
     * fallback without logging a warning.
     * <p>
     * Classes annotated with {@link AllowConstructorFallback} signal that using
     * a fallback constructor is intentional and should not be warned about.
     *
     * @return {@code true} if fallback constructor usage is allowed
     */
    protected boolean allowCtorFallback() {
        return this.getClass().isAnnotationPresent(AllowConstructorFallback.class);
    }

    @Override
    public S convertFrom(Object representation) {
        String strRepresentation = StringHelper.toStringExcludingNull(representation);
        if (getType().isInstance(strRepresentation)) return newInstance((T) strRepresentation);

        if (representation instanceof List<?> list) {
            // increased safety if T is non-final, don't just cast the list
            if (list.stream().allMatch(getType()::isInstance)) {
                List<T> typedList = list.stream().map(item -> (T) item).toList();
                return newInstance(typedList);
            }

            // If T is String, be more lenient by allowing primitives
            if (getType() == String.class) {
                List<T> strList = new ArrayList<>();
                for (Object item : list) {
                    String itemString = StringHelper.toStringExcludingNull(item);

                    // Handle boxed primitives and null-safe toString conversion
                    if (itemString == null) {
                        throw new IllegalArgumentException("ValueList contains non stringable elements: " + representation);
                    } else {
                        strList.add((T) itemString);
                    }
                }

                return newInstance(strList);
            }
        }
        throw new IllegalArgumentException("Invalid representation: " + representation);
    }

    @Override
    public Object getRepresentation() {
        return value;
    }

    @Override
    public S copy() {
        if (value instanceof ValueList<?> valueList) {
            return newInstance((ValueList<T>) valueList.copy()); // safe cast as constuctors set value as ValueList<T>
        }
        return newInstance((T) value);
    }

    @Override
    public String toString() {
        Object representation = getRepresentation();
        String strRepresentation = StringHelper.toString(representation);
        if (strRepresentation != null) return strRepresentation;
        else if (representation instanceof Collection<?> collection) return collectionToString(collection);
        throw new IllegalArgumentException("Invalid representation: " + representation);
    }

    // TODO :: Make Collection util class
    // Taken from AbstractCollection, used as ValueList doesn't extend AbstractCollection/AbstractList leading to bad output
    public <E> String collectionToString(Collection<E> list) {
        Iterator<E> it = list.iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == list ? "(this Collection)" : e);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    /** Computes "distance" from subclass to superclass; 0 = exact match, 1 = direct parent, etc. */
    private static int getClassDistance(Class<?> child, Class<?> parent) {
        if (child.equals(parent)) return 0;
        int distance = 0;
        Class<?> c = child;
        while (c != null && !c.equals(parent)) {
            c = c.getSuperclass();
            distance++;
        }
        return c == null ? Integer.MAX_VALUE : distance;
    }

    private static boolean parametersMatch(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Object arg = args[i];
            Class<?> param = wrapPrimitive(paramTypes[i]);

            // null can go into any non-primitive parameter
            if (arg == null) {
                if (paramTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!param.isAssignableFrom(arg.getClass())) return false;
        }
        return true;
    }

    private static Class<?> wrapPrimitive(Class<?> clazz) {
        if (!clazz.isPrimitive()) return clazz;

        if (clazz == int.class) return Integer.class;
        if (clazz == long.class) return Long.class;
        if (clazz == boolean.class) return Boolean.class;
        if (clazz == double.class) return Double.class;
        if (clazz == float.class) return Float.class;
        if (clazz == char.class) return Character.class;
        if (clazz == byte.class) return Byte.class;
        if (clazz == short.class) return Short.class;

        return clazz;
    }

    /**
     * Logs a warning when a reflective constructor fallback is used,
     * unless the class is annotated with {@link AllowConstructorFallback}.
     *
     * @param classCtor the class whose constructor is being invoked
     * @param ctorArgs the constructor argument types
     */
    private void logFallback(Class<?> classCtor, Class<?> @NotNull ... ctorArgs) {
        if (allowCtorFallback()) return;

        String argsStr = Arrays.stream(ctorArgs).map(Class::getSimpleName).collect(Collectors.joining(", "));

        CombatLogX.LOGGER.warn("(Config) Falling back to constructor '{}({})'. This is supported but not recommended; prefer defining an explicit constructor.",
                classCtor.getSimpleName(), argsStr);
    }
}
