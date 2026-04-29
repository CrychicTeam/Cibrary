package org.pickaid.pibrary.api.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default hierarchical implementation backing the global Pibrary service graph.
 */
public final class PibraryServices implements PibraryServiceContext {
    private static final PibraryServices ROOT = new PibraryServices(null);

    private final PibraryServices parent;
    private final Map<PibraryServiceKey<?>, Object> services = new ConcurrentHashMap<>();

    private PibraryServices(PibraryServices parent) {
        this.parent = parent;
    }

    /**
     * Returns the shared root context used by static service accessors.
     *
     * @return global root context
     */
    public static PibraryServices root() {
        return ROOT;
    }

    /**
     * Creates an isolated root service context with no parent.
     *
     * @return standalone service context
     */
    public static PibraryServices create() {
        return new PibraryServices(null);
    }

    /**
     * Registers a service on the shared root context.
     *
     * @param key typed service key
     * @param service service instance or {@code null} to remove
     * @param <T> service contract type
     */
    public static <T> void install(PibraryServiceKey<T> key, T service) {
        ROOT.register(key, service);
    }

    /**
     * Resolves a service from the shared root context.
     *
     * @param key typed service key
     * @param <T> service contract type
     * @return resolved service, if present
     */
    public static <T> Optional<T> findService(PibraryServiceKey<T> key) {
        return ROOT.find(key);
    }

    /**
     * Resolves a service from the shared root context or fails if absent.
     *
     * @param key typed service key
     * @param <T> service contract type
     * @return resolved service
     */
    public static <T> T requireService(PibraryServiceKey<T> key) {
        return ROOT.require(key);
    }

    @Override
    public <T> void register(PibraryServiceKey<T> key, T service) {
        if (service == null) {
            services.remove(key);
            return;
        }

        if (!key.type().isInstance(service)) {
            throw new IllegalArgumentException("Service " + service + " is not a " + key.type().getName());
        }

        services.put(key, service);
    }

    @Override
    public <T> Optional<T> find(PibraryServiceKey<T> key) {
        Object service = services.get(key);
        if (service != null) {
            return Optional.of(key.type().cast(service));
        }
        return parent == null ? Optional.empty() : parent.find(key);
    }

    @Override
    public PibraryServices child() {
        return new PibraryServices(this);
    }
}
