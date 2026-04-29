package org.pickaid.pibrary.runtime.facet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.api.facet.PiLivingEntityFacet;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Central registry and bootstrap entry point for generated living facet descriptors.
 */
public final class PiLivingFacetDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiLivingFacetDescriptors() {
    }

    /**
     * Forces descriptor discovery through {@link ServiceLoader}.
     */
    public static void bootstrap() {
        ((ServiceLoaderRegistry) REGISTRY).ensureLoaded();
    }

    /**
     * Finds a generated descriptor by facet class.
     *
     * @param facetClass facet class
     * @param <T> facet type
     * @return descriptor, if present
     */
    public static <T extends PiStateLivingEntityFacet<?>> Optional<PiGeneratedLivingFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
        return REGISTRY.findGenerated(facetClass);
    }

    /**
     * Requires a generated descriptor by facet class.
     *
     * @param facetClass facet class
     * @param <T> facet type
     * @return descriptor
     */
    public static <T extends PiStateLivingEntityFacet<?>> PiGeneratedLivingFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
        return REGISTRY.requireGenerated(facetClass);
    }

    /**
     * Finds a generated descriptor by id.
     *
     * @param id descriptor id
     * @return descriptor, if present
     */
    public static Optional<PiGeneratedLivingFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    /**
     * Returns every loaded generated descriptor.
     *
     * @return immutable descriptor list
     */
    public static List<PiGeneratedLivingFacetDescriptor<?, ?>> generatedDescriptors() {
        return REGISTRY.generatedDescriptors();
    }

    /**
     * Executes an action for every generated facet currently present on a living entity.
     *
     * @param living owning entity
     * @param action action applied to each present facet
     */
    public static void forEachPresent(LivingEntity living, Consumer<PiLivingEntityFacet> action) {
        REGISTRY.forEachPresent(Objects.requireNonNull(living, "living"), Objects.requireNonNull(action, "action"));
    }

    private static final class ServiceLoaderRegistry implements PiLivingFacetRegistry {
        private final Map<Class<?>, PiGeneratedLivingFacetDescriptor<?, ?>> byFacetClass = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedLivingFacetDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile List<PiGeneratedLivingFacetDescriptor<?, ?>> descriptors = List.of();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedLivingFacetDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedLivingFacetDescriptor<?, ?> previousByType = byFacetClass.putIfAbsent(descriptor.facetClass(), descriptor);
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi living facet descriptor for " + descriptor.facetClass().getName());
            }
            PiGeneratedLivingFacetDescriptor<?, ?> previousById = byId.putIfAbsent(descriptor.id(), descriptor);
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi living facet descriptor id " + descriptor.id());
            }
            descriptors = List.copyOf(byFacetClass.values());
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiLivingFacetProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedLivingFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateLivingEntityFacet<?>> Optional<PiGeneratedLivingFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
            ensureLoaded();
            PiGeneratedLivingFacetDescriptor<?, ?> descriptor = byFacetClass.get(Objects.requireNonNull(facetClass, "facetClass"));
            if (descriptor == null) {
                return Optional.empty();
            }
            return Optional.of(cast(descriptor));
        }

        private <T extends PiStateLivingEntityFacet<?>> PiGeneratedLivingFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
            return findGenerated(facetClass).orElseThrow(() ->
                    new IllegalStateException("Missing Pi living facet descriptor for " + facetClass.getName()));
        }

        private List<PiGeneratedLivingFacetDescriptor<?, ?>> generatedDescriptors() {
            ensureLoaded();
            return descriptors;
        }

        private void forEachPresent(LivingEntity living, Consumer<PiLivingEntityFacet> action) {
            ensureLoaded();
            for (PiGeneratedLivingFacetDescriptor<?, ?> descriptor : descriptors) {
                descriptor.find(living).ifPresent(action);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateLivingEntityFacet<?>> PiGeneratedLivingFacetDescriptor<T, ?> cast(PiGeneratedLivingFacetDescriptor<?, ?> descriptor) {
            return (PiGeneratedLivingFacetDescriptor<T, ?>) descriptor;
        }
    }
}
