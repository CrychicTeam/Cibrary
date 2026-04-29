package org.pickaid.pibrary.runtime.facet;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.facet.PiLevelFacetType;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;

public final class PiActiveLevelFacetRegistry {
    private static final Map<Class<?>, PiGeneratedLevelFacetDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedLevelFacetDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();

    private PiActiveLevelFacetRegistry() {
    }

    public static <T extends PiStateLevelFacet<?>> PiLevelFacetType<T> register(Class<T> facetClass) {
        return register(PiLevelFacetDescriptors.requireGenerated(facetClass));
    }

    public static synchronized <T extends PiStateLevelFacet<?>> PiLevelFacetType<T> register(PiGeneratedLevelFacetDescriptor<T, ?> descriptor) {
        PiGeneratedLevelFacetDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.facetClass());
        if (existingByType != null) {
            return cast(existingByType);
        }
        PiGeneratedLevelFacetDescriptor<?, ?> existingById = BY_ID.putIfAbsent(descriptor.id(), descriptor);
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi level facet id " + descriptor.id());
        }
        BY_TYPE.put(descriptor.facetClass(), descriptor);
        return descriptor;
    }

    public static boolean isRegistered(Class<?> facetClass) {
        return BY_TYPE.containsKey(facetClass);
    }

    public static Optional<PiGeneratedLevelFacetDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateLevelFacet<?>> Optional<PiLevelFacetType<T>> find(Class<T> facetClass) {
        return Optional.ofNullable(BY_TYPE.get(facetClass)).map(PiActiveLevelFacetRegistry::cast);
    }

    public static <T extends PiStateLevelFacet<?>> PiLevelFacetType<T> require(Class<T> facetClass) {
        return find(facetClass).orElseThrow(() ->
                new IllegalStateException("Pi level facet " + facetClass.getName() + " is discovered but not registered"));
    }

    static synchronized void clearForTests() {
        BY_TYPE.clear();
        BY_ID.clear();
    }

    @SuppressWarnings("unchecked")
    private static <T extends PiStateLevelFacet<?>> PiLevelFacetType<T> cast(Object value) {
        return (PiLevelFacetType<T>) value;
    }
}
