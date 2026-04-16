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

    private PiActiveLivingServiceRegistry() {
    }

    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> register(Class<T> serviceType) {
        return register(PiLivingServiceDescriptors.requireGenerated(serviceType));
    }

    public static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> register(PiGeneratedLivingServiceDescriptor<T, ?> descriptor) {
        PiGeneratedLivingServiceDescriptor<?, ?> existingByType = BY_TYPE.putIfAbsent(descriptor.serviceType(), descriptor);
        if (existingByType != null) {
            return cast(existingByType);
        }

        PiGeneratedLivingServiceDescriptor<?, ?> existingById = BY_ID.putIfAbsent(descriptor.id(), descriptor);
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi living service id " + descriptor.id());
        }

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

    static void clearForTests() {
        BY_TYPE.clear();
        BY_ID.clear();
        SNAPSHOT = List.of();
    }

    @SuppressWarnings("unchecked")
    private static <T extends PiStateLivingEntityService<?>> PiLivingServiceType<T> cast(Object value) {
        return (PiLivingServiceType<T>) value;
    }
}
