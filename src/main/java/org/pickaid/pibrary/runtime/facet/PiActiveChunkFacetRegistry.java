package org.pickaid.pibrary.runtime.facet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.facet.PiChunkFacetType;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;

public final class PiActiveChunkFacetRegistry {
    private static final Map<Class<?>, PiGeneratedChunkFacetDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedChunkFacetDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();
    private static volatile List<PiGeneratedChunkFacetDescriptor<?, ?>> SNAPSHOT = List.of();
    private static volatile boolean REGISTRATION_OPEN = true;

    private PiActiveChunkFacetRegistry() {
    }

    public static <T extends PiStateChunkFacet<?>> PiChunkFacetType<T> register(Class<T> facetClass) {
        return register(PiChunkFacetDescriptors.requireGenerated(facetClass));
    }

    public static synchronized <T extends PiStateChunkFacet<?>> PiChunkFacetType<T> register(PiGeneratedChunkFacetDescriptor<T, ?> descriptor) {
        if (!REGISTRATION_OPEN) {
            throw new IllegalStateException(
                    "Pi chunk facet registration is closed after Forge capability registration has started");
        }

        PiGeneratedChunkFacetDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.facetClass());
        if (existingByType != null) {
            return cast(existingByType);
        }

        PiGeneratedChunkFacetDescriptor<?, ?> existingById = BY_ID.get(descriptor.id());
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi chunk facet id " + descriptor.id());
        }

        BY_TYPE.put(descriptor.facetClass(), descriptor);
        BY_ID.put(descriptor.id(), descriptor);
        SNAPSHOT = List.copyOf(BY_TYPE.values());
        return descriptor;
    }

    public static List<PiGeneratedChunkFacetDescriptor<?, ?>> activeDescriptors() {
        return SNAPSHOT;
    }

    public static boolean isRegistered(Class<?> facetClass) {
        return BY_TYPE.containsKey(facetClass);
    }

    public static Optional<PiGeneratedChunkFacetDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateChunkFacet<?>> Optional<PiChunkFacetType<T>> find(Class<T> facetClass) {
        return Optional.ofNullable(BY_TYPE.get(facetClass)).map(PiActiveChunkFacetRegistry::cast);
    }

    public static <T extends PiStateChunkFacet<?>> PiChunkFacetType<T> require(Class<T> facetClass) {
        return find(facetClass).orElseThrow(() ->
                new IllegalStateException("Pi chunk facet " + facetClass.getName() + " is discovered but not registered"));
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
    private static <T extends PiStateChunkFacet<?>> PiChunkFacetType<T> cast(Object value) {
        return (PiChunkFacetType<T>) value;
    }
}
