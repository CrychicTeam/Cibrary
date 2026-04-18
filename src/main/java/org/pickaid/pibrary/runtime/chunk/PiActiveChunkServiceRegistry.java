package org.pickaid.pibrary.runtime.chunk;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiStateChunkService;

public final class PiActiveChunkServiceRegistry {
    private static final Map<Class<?>, PiGeneratedChunkServiceDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedChunkServiceDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();
    private static volatile List<PiGeneratedChunkServiceDescriptor<?, ?>> SNAPSHOT = List.of();
    private static volatile boolean REGISTRATION_OPEN = true;

    private PiActiveChunkServiceRegistry() {
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceType<T> register(Class<T> serviceType) {
        return register(PiChunkServiceDescriptors.requireGenerated(serviceType));
    }

    public static synchronized <T extends PiStateChunkService<?>> PiChunkServiceType<T> register(
            PiGeneratedChunkServiceDescriptor<T, ?> descriptor
    ) {
        if (!REGISTRATION_OPEN) {
            throw new IllegalStateException(
                    "Pi chunk service registration is closed after Forge capability registration has started");
        }

        PiGeneratedChunkServiceDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.serviceType());
        if (existingByType != null) {
            return cast(existingByType);
        }

        PiGeneratedChunkServiceDescriptor<?, ?> existingById = BY_ID.get(descriptor.id());
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi chunk service id " + descriptor.id());
        }

        BY_TYPE.put(descriptor.serviceType(), descriptor);
        BY_ID.put(descriptor.id(), descriptor);
        SNAPSHOT = List.copyOf(BY_TYPE.values());
        return descriptor;
    }

    public static List<PiGeneratedChunkServiceDescriptor<?, ?>> activeDescriptors() {
        return SNAPSHOT;
    }

    public static boolean isRegistered(Class<?> serviceType) {
        return BY_TYPE.containsKey(serviceType);
    }

    public static Optional<PiGeneratedChunkServiceDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateChunkService<?>> Optional<PiChunkServiceType<T>> find(Class<T> serviceType) {
        return Optional.ofNullable(BY_TYPE.get(serviceType)).map(PiActiveChunkServiceRegistry::cast);
    }

    public static <T extends PiStateChunkService<?>> PiChunkServiceType<T> require(Class<T> serviceType) {
        return find(serviceType).orElseThrow(() ->
                new IllegalStateException("Pi chunk service " + serviceType.getName() + " is discovered but not registered"));
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
    private static <T extends PiStateChunkService<?>> PiChunkServiceType<T> cast(Object value) {
        return (PiChunkServiceType<T>) value;
    }
}
