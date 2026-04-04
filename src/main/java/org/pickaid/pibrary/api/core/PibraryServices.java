package org.pickaid.pibrary.api.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PibraryServices implements PibraryServiceRegistry {
    public static final PibraryServices INSTANCE = new PibraryServices();

    private final Map<PibraryServiceKey<?>, Object> services = new ConcurrentHashMap<>();

    private PibraryServices() {
    }

    public static <T> void install(PibraryServiceKey<T> key, T service) {
        INSTANCE.register(key, service);
    }

    public static <T> Optional<T> findService(PibraryServiceKey<T> key) {
        return INSTANCE.find(key);
    }

    public static <T> T requireService(PibraryServiceKey<T> key) {
        return INSTANCE.require(key);
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
        if (service == null) {
            return Optional.empty();
        }

        return Optional.of(key.type().cast(service));
    }
}
