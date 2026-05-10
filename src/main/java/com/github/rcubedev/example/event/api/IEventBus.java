package com.github.rcubedev.example.event.api;

import com.github.rcubedev.example.event.impl.EventBusRegistry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Interface for event buses typed to a specific {@link Event} subclass {@link B}.
 * <p>
 * Create a named bus by extending {@link EventBus}:
 * <pre>
 * {@code
 * // The bus marker — all events on this bus extend this
 * public abstract class MainBusEvent extends Event {}
 *
 * // The singleton bus
 * public final class MainEventBus extends EventBus<MainBusEvent> {
 *     public static final MainEventBus INSTANCE = new MainEventBus();
 *     private MainEventBus() { super(MainBusEvent.class); }
 * }
 *
 * // An event on this bus
 * public class PlayerLoginEvent extends MainBusEvent {}
 * }
 * </pre>
 *
 * @param <B> The base event type this bus accepts
 */
public interface IEventBus<B extends Event> {

    /**
     * Post an event to this bus.
     * <p>
     * Listeners are invoked synchronously in order of priority.
     *
     * @param event The event to dispatch
     * @throws IllegalStateException if the recursion depth exceeds the safety limit of this bus.<br>
     *                               Use {@link #openBypass()} to handle intentional deep recursion.
     */
    <E extends B> void post(E event);

    /**
     * Opens a scope where the recursion guard is disabled for the current thread.
     * <p>
     * <b>Warning:</b> This handle <b>must</b> be closed (ideally via try-with-resources) to prevent state leakage.<br>
     * Failure to close it will leave the recursion guard disabled for the remainder
     * of the thread's lifecycle, risking unhandled StackOverflowErrors.
     * <p>
     * Example usage:
     * <pre>{@code
     * try (RecursionBypass ignored = bus.openBypass()) {
     *     bus.post(new DeepNestedEvent());
     * }
     * }</pre>
     *
     * @return a handle that restores the guard state when closed
     */
    default RecursionBypass openBypass() {
        return openBypassTo(Integer.MAX_VALUE / 2);
    }

    /**
     * Opens a scope that extends the recursion budget for the current thread.
     * <p>
     * <b>Warning:</b> This handle <b>must</b> be closed (ideally via try-with-resources) to prevent state leakage.<br>
     * Failure to close it will leave the recursion guard disabled for the remainder
     * of the thread's lifecycle, risking unhandled StackOverflowErrors.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Grants 50 additional levels of recursion before the guard trips
     * try (RecursionBypass ignored = bus.openBypassTo(50)) {
     *     bus.post(new ComplexFeedbackEvent());
     * }
     * }</pre>
     *
     * @param extraBudget The number of additional recursive calls to allow (must be positive).
     * @return a handle that restores the guard state when closed
     */
    RecursionBypass openBypassTo(int extraBudget);

    /**
     * Register a direct {@link EventProcessor} for the given event type at {@link Priority#NORMAL}.
     *
     * @param eventType The class of the event to listen for
     * @param listener  The processor to invoke
     * @param <E>       The specific event type
     */
    default <E extends B> void register(Class<E> eventType, EventProcessor<E> listener) {
        register(eventType, Priority.NORMAL, listener);
    }

    /**
     * Register a direct {@link EventProcessor} for the given event type at a specific priority.
     *
     * @param eventType The class of the event to listen for
     * @param priority  The priority of this listener
     * @param listener  The processor to invoke
     * @param <E>       The specific event type
     */
    <E extends B> void register(Class<E> eventType, Priority priority, EventProcessor<E> listener);

    /**
     * Register a direct {@link EventProcessor} for the base bus type {@link B} at a specific priority.
     *
     * @param priority  The priority of this listener
     * @param listener  The processor to invoke
     */
    default void register(EventProcessor<B> listener, Priority priority) {
        register(getBusType(), priority, listener);
    }

    /**
     * Register a direct {@link EventProcessor} for the base bus type {@link B} at {@link Priority#NORMAL}.
     *
     * @param listener  The processor to invoke
     */
    default void register(EventProcessor<B> listener) {
        register(getBusType(), listener);
    }

    /**
     * Register a listener instance or {@link Class} with {@link SubscribeEvent @SubscribeEvent} methods.<br>
     * Only methods whose parameter type is a subtype of {@link B} will be registered.
     *
     * @param target Listener instance or {@link Class} for static methods
     * @throws IllegalArgumentException if no valid {@link SubscribeEvent @SubscribeEvent} methods are found.
     */
    void register(Object target);

    /**
     * Get the base event type this bus accepts.
     *
     * @return The base event class
     */
    Class<B> getBusType();

    /**
     * Internal. Used for testing.<br>
     * Reset all listeners on this bus.
     */
    @ApiStatus.Internal
    void resetListeners();
}