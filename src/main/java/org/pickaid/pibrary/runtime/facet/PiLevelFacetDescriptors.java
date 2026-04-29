package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;

public final class PiLevelFacetDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiLevelFacetDescriptors() {
    }

    public static void bootstrap() {
        REGISTRY.ensureLoaded();
    }

    public static <T extends PiStateLevelFacet<?>> Optional<PiGeneratedLevelFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
        return REGISTRY.findGenerated(facetClass);
    }

    public static <T extends PiStateLevelFacet<?>> PiGeneratedLevelFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
        return REGISTRY.requireGenerated(facetClass);
    }

    public static Optional<PiGeneratedLevelFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    private static final class ServiceLoaderRegistry implements PiLevelFacetRegistry {
        private final Map<Class<?>, PiGeneratedLevelFacetDescriptor<?, ?>> byFacetClass = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedLevelFacetDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedLevelFacetDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedLevelFacetDescriptor<?, ?> previousByType = byFacetClass.get(descriptor.facetClass());
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi level facet descriptor for " + descriptor.facetClass().getName());
            }
            PiGeneratedLevelFacetDescriptor<?, ?> previousById = byId.get(descriptor.id());
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi level facet descriptor id " + descriptor.id());
            }
            byFacetClass.put(descriptor.facetClass(), descriptor);
            byId.put(descriptor.id(), descriptor);
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiLevelFacetProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedLevelFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateLevelFacet<?>> Optional<PiGeneratedLevelFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
            ensureLoaded();
            PiGeneratedLevelFacetDescriptor<?, ?> descriptor = byFacetClass.get(Objects.requireNonNull(facetClass, "facetClass"));
            return descriptor == null ? Optional.empty() : Optional.of(cast(descriptor));
        }

        private <T extends PiStateLevelFacet<?>> PiGeneratedLevelFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
            return findGenerated(facetClass).orElseThrow(() ->
                    new IllegalStateException("Missing Pi level facet descriptor for " + facetClass.getName()));
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateLevelFacet<?>> PiGeneratedLevelFacetDescriptor<T, ?> cast(PiGeneratedLevelFacetDescriptor<?, ?> descriptor) {
            return (PiGeneratedLevelFacetDescriptor<T, ?>) descriptor;
        }
    }
}
