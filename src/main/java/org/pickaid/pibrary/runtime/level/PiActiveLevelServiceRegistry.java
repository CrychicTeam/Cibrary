package org.pickaid.pibrary.runtime.level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiStateLevelService;

public final class PiActiveLevelServiceRegistry {
    private static final Map<Class<?>, PiGeneratedLevelServiceDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedLevelServiceDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();

    private PiActiveLevelServiceRegistry() {
    }

    public static <T extends PiStateLevelService<?>> PiLevelServiceType<T> register(Class<T> serviceType) {
        return register(PiLevelServiceDescriptors.requireGenerated(serviceType));
    }

    public static synchronized <T extends PiStateLevelService<?>> PiLevelServiceType<T> register(PiGeneratedLevelServiceDescriptor<T, ?> descriptor) {
        PiGeneratedLevelServiceDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.serviceType());
        if (existingByType != null) {
            return cast(existingByType);
        }
        PiGeneratedLevelServiceDescriptor<?, ?> existingById = BY_ID.putIfAbsent(descriptor.id(), descriptor);
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi level service id " + descriptor.id());
        }
        BY_TYPE.put(descriptor.serviceType(), descriptor);
        return descriptor;
    }

    public static boolean isRegistered(Class<?> serviceType) {
        return BY_TYPE.containsKey(serviceType);
    }

    public static Optional<PiGeneratedLevelServiceDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateLevelService<?>> Optional<PiLevelServiceType<T>> find(Class<T> serviceType) {
        return Optional.ofNullable(BY_TYPE.get(serviceType)).map(PiActiveLevelServiceRegistry::cast);
    }

    public static <T extends PiStateLevelService<?>> PiLevelServiceType<T> require(Class<T> serviceType) {
        return find(serviceType).orElseThrow(() ->
                new IllegalStateException("Pi level service " + serviceType.getName() + " is discovered but not registered"));
    }

    static synchronized void clearForTests() {
        BY_TYPE.clear();
        BY_ID.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T extends PiStateLevelService<?>> PiLevelServiceType<T> cast(Object value) {
        return (PiLevelServiceType<T>) value;
    }
}
