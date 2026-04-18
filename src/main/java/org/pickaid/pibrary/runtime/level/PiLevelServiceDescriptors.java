package org.pickaid.pibrary.runtime.level;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiStateLevelService;

public final class PiLevelServiceDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiLevelServiceDescriptors() {
    }

    public static void bootstrap() {
        REGISTRY.ensureLoaded();
    }

    public static <T extends PiStateLevelService<?>> Optional<PiGeneratedLevelServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
        return REGISTRY.findGenerated(serviceType);
    }

    public static <T extends PiStateLevelService<?>> PiGeneratedLevelServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
        return REGISTRY.requireGenerated(serviceType);
    }

    public static Optional<PiGeneratedLevelServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    private static final class ServiceLoaderRegistry implements PiLevelServiceRegistry {
        private final Map<Class<?>, PiGeneratedLevelServiceDescriptor<?, ?>> byServiceType = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedLevelServiceDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedLevelServiceDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedLevelServiceDescriptor<?, ?> previousByType = byServiceType.get(descriptor.serviceType());
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi level service descriptor for " + descriptor.serviceType().getName());
            }
            PiGeneratedLevelServiceDescriptor<?, ?> previousById = byId.get(descriptor.id());
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi level service descriptor id " + descriptor.id());
            }
            byServiceType.put(descriptor.serviceType(), descriptor);
            byId.put(descriptor.id(), descriptor);
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiLevelServiceProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedLevelServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateLevelService<?>> Optional<PiGeneratedLevelServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
            ensureLoaded();
            PiGeneratedLevelServiceDescriptor<?, ?> descriptor = byServiceType.get(Objects.requireNonNull(serviceType, "serviceType"));
            return descriptor == null ? Optional.empty() : Optional.of(cast(descriptor));
        }

        private <T extends PiStateLevelService<?>> PiGeneratedLevelServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
            return findGenerated(serviceType).orElseThrow(() ->
                    new IllegalStateException("Missing Pi level service descriptor for " + serviceType.getName()));
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateLevelService<?>> PiGeneratedLevelServiceDescriptor<T, ?> cast(PiGeneratedLevelServiceDescriptor<?, ?> descriptor) {
            return (PiGeneratedLevelServiceDescriptor<T, ?>) descriptor;
        }
    }
}
