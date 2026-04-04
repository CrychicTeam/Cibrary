package org.pickaid.pibrary.runtime.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public final class PiLivingServiceDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiLivingServiceDescriptors() {
    }

    public static void bootstrap() {
        ((ServiceLoaderRegistry) REGISTRY).ensureLoaded();
    }

    public static <T extends PiStateLivingEntityService<?>> Optional<PiGeneratedLivingServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
        return REGISTRY.findGenerated(serviceType);
    }

    public static <T extends PiStateLivingEntityService<?>> PiGeneratedLivingServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
        return REGISTRY.requireGenerated(serviceType);
    }

    public static Optional<PiGeneratedLivingServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    public static List<PiGeneratedLivingServiceDescriptor<?, ?>> generatedDescriptors() {
        return REGISTRY.generatedDescriptors();
    }

    private static final class ServiceLoaderRegistry implements PiLivingServiceRegistry {
        private final Map<Class<?>, PiGeneratedLivingServiceDescriptor<?, ?>> byServiceType = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedLivingServiceDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedLivingServiceDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedLivingServiceDescriptor<?, ?> previousByType = byServiceType.putIfAbsent(descriptor.serviceType(), descriptor);
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi living service descriptor for " + descriptor.serviceType().getName());
            }
            PiGeneratedLivingServiceDescriptor<?, ?> previousById = byId.putIfAbsent(descriptor.id(), descriptor);
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi living service descriptor id " + descriptor.id());
            }
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiLivingServiceProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedLivingServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateLivingEntityService<?>> Optional<PiGeneratedLivingServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
            ensureLoaded();
            PiGeneratedLivingServiceDescriptor<?, ?> descriptor = byServiceType.get(Objects.requireNonNull(serviceType, "serviceType"));
            if (descriptor == null) {
                return Optional.empty();
            }
            return Optional.of(cast(descriptor));
        }

        private <T extends PiStateLivingEntityService<?>> PiGeneratedLivingServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
            return findGenerated(serviceType).orElseThrow(() ->
                    new IllegalStateException("Missing Pi living service descriptor for " + serviceType.getName()));
        }

        private List<PiGeneratedLivingServiceDescriptor<?, ?>> generatedDescriptors() {
            ensureLoaded();
            return List.copyOf(byServiceType.values());
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateLivingEntityService<?>> PiGeneratedLivingServiceDescriptor<T, ?> cast(PiGeneratedLivingServiceDescriptor<?, ?> descriptor) {
            return (PiGeneratedLivingServiceDescriptor<T, ?>) descriptor;
        }
    }
}
