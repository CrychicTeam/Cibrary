package org.pickaid.pibrary.runtime.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.api.service.PiLivingEntityService;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

/**
 * Central registry and bootstrap entry point for generated living service descriptors.
 */
public final class PiLivingServiceDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiLivingServiceDescriptors() {
    }

    /**
     * Forces descriptor discovery through {@link ServiceLoader}.
     */
    public static void bootstrap() {
        ((ServiceLoaderRegistry) REGISTRY).ensureLoaded();
    }

    /**
     * Finds a generated descriptor by service type.
     *
     * @param serviceType service class
     * @param <T> service type
     * @return descriptor, if present
     */
    public static <T extends PiStateLivingEntityService<?>> Optional<PiGeneratedLivingServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
        return REGISTRY.findGenerated(serviceType);
    }

    /**
     * Requires a generated descriptor by service type.
     *
     * @param serviceType service class
     * @param <T> service type
     * @return descriptor
     */
    public static <T extends PiStateLivingEntityService<?>> PiGeneratedLivingServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
        return REGISTRY.requireGenerated(serviceType);
    }

    /**
     * Finds a generated descriptor by id.
     *
     * @param id descriptor id
     * @return descriptor, if present
     */
    public static Optional<PiGeneratedLivingServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    /**
     * Returns every loaded generated descriptor.
     *
     * @return immutable descriptor list
     */
    public static List<PiGeneratedLivingServiceDescriptor<?, ?>> generatedDescriptors() {
        return REGISTRY.generatedDescriptors();
    }

    /**
     * Executes an action for every generated service currently present on a living entity.
     *
     * @param living owning entity
     * @param action action applied to each present service
     */
    public static void forEachPresent(LivingEntity living, Consumer<PiLivingEntityService> action) {
        REGISTRY.forEachPresent(Objects.requireNonNull(living, "living"), Objects.requireNonNull(action, "action"));
    }

    private static final class ServiceLoaderRegistry implements PiLivingServiceRegistry {
        private final Map<Class<?>, PiGeneratedLivingServiceDescriptor<?, ?>> byServiceType = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedLivingServiceDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile List<PiGeneratedLivingServiceDescriptor<?, ?>> descriptors = List.of();
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
            descriptors = List.copyOf(byServiceType.values());
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
            return descriptors;
        }

        private void forEachPresent(LivingEntity living, Consumer<PiLivingEntityService> action) {
            ensureLoaded();
            for (PiGeneratedLivingServiceDescriptor<?, ?> descriptor : descriptors) {
                descriptor.find(living).ifPresent(action);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateLivingEntityService<?>> PiGeneratedLivingServiceDescriptor<T, ?> cast(PiGeneratedLivingServiceDescriptor<?, ?> descriptor) {
            return (PiGeneratedLivingServiceDescriptor<T, ?>) descriptor;
        }
    }
}
