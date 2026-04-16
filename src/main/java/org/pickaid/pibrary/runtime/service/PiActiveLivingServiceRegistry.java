package org.pickaid.pibrary.runtime.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public final class PiActiveLivingServiceRegistry {
    private static final Map<Class<?>, PiGeneratedLivingServiceDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedLivingServiceDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();
    private static volatile List<PiGeneratedLivingServiceDescriptor<?, ?>> SNAPSHOT = List.of();
    private static volatile boolean REGISTRATION_OPEN = true;

    private PiActiveLivingServiceRegistry() {
    }

    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> register(Class<T> serviceType) {
        return register(PiLivingServiceDescriptors.requireGenerated(serviceType));
    }

    public static synchronized <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> register(PiGeneratedLivingServiceDescriptor<T, ?> descriptor) {
        if (!REGISTRATION_OPEN) {
            throw new IllegalStateException(
                    "Pi living service registration is closed after Forge capability registration has started");
        }

        PiGeneratedLivingServiceDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.serviceType());
        if (existingByType != null) {
            return cast(existingByType);
        }

        PiGeneratedLivingServiceDescriptor<?, ?> existingById = BY_ID.get(descriptor.id());
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi living service id " + descriptor.id());
        }

        BY_TYPE.put(descriptor.serviceType(), descriptor);
        BY_ID.put(descriptor.id(), descriptor);
        SNAPSHOT = List.copyOf(BY_TYPE.values());
        return descriptor;
    }

    public static List<PiGeneratedLivingServiceDescriptor<?, ?>> activeDescriptors() {
        return SNAPSHOT;
    }

    public static boolean isRegistered(Class<?> serviceType) {
        return BY_TYPE.containsKey(serviceType);
    }

    public static Optional<PiGeneratedLivingServiceDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateLivingEntityService<?>> Optional<PiLivingServiceType<T>> find(Class<T> serviceType) {
        return Optional.ofNullable(BY_TYPE.get(serviceType)).map(PiActiveLivingServiceRegistry::cast);
    }

    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> require(Class<T> serviceType) {
        return find(serviceType).orElseThrow(() ->
                new IllegalStateException("Pi living service " + serviceType.getName() + " is discovered but not registered"));
    }

    public static synchronized void closeRegistration() {
        REGISTRATION_OPEN = false;
    }

    static synchronized void clearForTests() {
        BY_TYPE.clear();
        BY_ID.clear();
        SNAPSHOT = List.of();
        REGISTRATION_OPEN = true;
    }

    @SuppressWarnings("unchecked")
    private static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> cast(Object value) {
        return (PiLivingServiceType<T>) value;
    }
}
