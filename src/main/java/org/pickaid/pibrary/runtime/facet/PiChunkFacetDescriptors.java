package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;

public final class PiChunkFacetDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiChunkFacetDescriptors() {
    }

    public static void bootstrap() {
        REGISTRY.ensureLoaded();
    }

    public static <T extends PiStateChunkFacet<?>> Optional<PiGeneratedChunkFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
        return REGISTRY.findGenerated(facetClass);
    }

    public static <T extends PiStateChunkFacet<?>> PiGeneratedChunkFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
        return REGISTRY.requireGenerated(facetClass);
    }

    public static Optional<PiGeneratedChunkFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    private static final class ServiceLoaderRegistry implements PiChunkFacetRegistry {
        private final Map<Class<?>, PiGeneratedChunkFacetDescriptor<?, ?>> byFacetClass = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedChunkFacetDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedChunkFacetDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedChunkFacetDescriptor<?, ?> previousByType = byFacetClass.putIfAbsent(descriptor.facetClass(), descriptor);
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate generated Pi chunk facet class " + descriptor.facetClass().getName());
            }
            PiGeneratedChunkFacetDescriptor<?, ?> previousById = byId.putIfAbsent(descriptor.id(), descriptor);
            if (previousById != null) {
                throw new IllegalStateException("Duplicate generated Pi chunk facet id " + descriptor.id());
            }
        }

        private void ensureLoaded() {
            if (loaded) {
                return;
            }
            synchronized (this) {
                if (loaded) {
                    return;
                }
                ServiceLoader.load(PiChunkFacetProvider.class).forEach(provider -> provider.register(this));
                loaded = true;
            }
        }

        private Optional<PiGeneratedChunkFacetDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateChunkFacet<?>> Optional<PiGeneratedChunkFacetDescriptor<T, ?>> findGenerated(Class<T> facetClass) {
            ensureLoaded();
            PiGeneratedChunkFacetDescriptor<?, ?> descriptor = byFacetClass.get(Objects.requireNonNull(facetClass, "facetClass"));
            return descriptor == null ? Optional.empty() : Optional.of(cast(descriptor));
        }

        private <T extends PiStateChunkFacet<?>> PiGeneratedChunkFacetDescriptor<T, ?> requireGenerated(Class<T> facetClass) {
            return findGenerated(facetClass).orElseThrow(() ->
                    new IllegalStateException("No generated Pi chunk facet descriptor for " + facetClass.getName()));
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateChunkFacet<?>> PiGeneratedChunkFacetDescriptor<T, ?> cast(PiGeneratedChunkFacetDescriptor<?, ?> descriptor) {
            return (PiGeneratedChunkFacetDescriptor<T, ?>) descriptor;
        }
    }
}
