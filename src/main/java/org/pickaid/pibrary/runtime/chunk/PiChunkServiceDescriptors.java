package org.pickaid.pibrary.runtime.chunk;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiStateChunkService;

public final class PiChunkServiceDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiChunkServiceDescriptors() {
    }

    public static void bootstrap() {
        REGISTRY.ensureLoaded();
    }

    public static <T extends PiStateChunkService<?>> Optional<PiGeneratedChunkServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
        return REGISTRY.findGenerated(serviceType);
    }

    public static <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
        return REGISTRY.requireGenerated(serviceType);
    }

    public static Optional<PiGeneratedChunkServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    private static final class ServiceLoaderRegistry implements PiChunkServiceRegistry {
        private final Map<Class<?>, PiGeneratedChunkServiceDescriptor<?, ?>> byServiceType = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedChunkServiceDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedChunkServiceDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedChunkServiceDescriptor<?, ?> previousByType =
                    byServiceType.putIfAbsent(descriptor.serviceType(), descriptor);
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi chunk service descriptor for " + descriptor.serviceType().getName());
            }
            PiGeneratedChunkServiceDescriptor<?, ?> previousById = byId.putIfAbsent(descriptor.id(), descriptor);
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi chunk service descriptor id " + descriptor.id());
            }
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiChunkServiceProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedChunkServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateChunkService<?>> Optional<PiGeneratedChunkServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
            ensureLoaded();
            PiGeneratedChunkServiceDescriptor<?, ?> descriptor =
                    byServiceType.get(Objects.requireNonNull(serviceType, "serviceType"));
            return descriptor == null ? Optional.empty() : Optional.of(cast(descriptor));
        }

        private <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
            return findGenerated(serviceType).orElseThrow(() ->
                    new IllegalStateException("Missing Pi chunk service descriptor for " + serviceType.getName()));
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> cast(
                PiGeneratedChunkServiceDescriptor<?, ?> descriptor
        ) {
            return (PiGeneratedChunkServiceDescriptor<T, ?>) descriptor;
        }
    }
}
