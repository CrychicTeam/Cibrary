package org.pickaid.pibrary.runtime.facet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.facet.PiLivingFacetType;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

public final class PiActiveLivingFacetRegistry {
    private static final Map<Class<?>, PiGeneratedLivingFacetDescriptor<?, ?>> BY_TYPE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PiGeneratedLivingFacetDescriptor<?, ?>> BY_ID = new ConcurrentHashMap<>();
    private static volatile List<PiGeneratedLivingFacetDescriptor<?, ?>> SNAPSHOT = List.of();
    private static volatile boolean REGISTRATION_OPEN = true;

    private PiActiveLivingFacetRegistry() {
    }

    public static <T extends PiStateLivingEntityFacet<?>> PiLivingFacetType<T> register(Class<T> facetClass) {
        return register(PiLivingFacetDescriptors.requireGenerated(facetClass));
    }

    public static synchronized <T extends PiStateLivingEntityFacet<?>> PiLivingFacetType<T> register(PiGeneratedLivingFacetDescriptor<T, ?> descriptor) {
        if (!REGISTRATION_OPEN) {
            throw new IllegalStateException(
                    "Pi living facet registration is closed after Forge capability registration has started");
        }

        PiGeneratedLivingFacetDescriptor<?, ?> existingByType = BY_TYPE.get(descriptor.facetClass());
        if (existingByType != null) {
            return cast(existingByType);
        }

        PiGeneratedLivingFacetDescriptor<?, ?> existingById = BY_ID.get(descriptor.id());
        if (existingById != null) {
            throw new IllegalStateException("Duplicate active Pi living facet id " + descriptor.id());
        }

        BY_TYPE.put(descriptor.facetClass(), descriptor);
        BY_ID.put(descriptor.id(), descriptor);
        SNAPSHOT = List.copyOf(BY_TYPE.values());
        return descriptor;
    }

    public static List<PiGeneratedLivingFacetDescriptor<?, ?>> activeDescriptors() {
        return SNAPSHOT;
    }

    public static boolean isRegistered(Class<?> facetClass) {
        return BY_TYPE.containsKey(facetClass);
    }

    public static Optional<PiGeneratedLivingFacetDescriptor<?, ?>> find(ResourceLocation id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static <T extends PiStateLivingEntityFacet<?>> Optional<PiLivingFacetType<T>> find(Class<T> facetClass) {
        return Optional.ofNullable(BY_TYPE.get(facetClass)).map(PiActiveLivingFacetRegistry::cast);
    }

    public static <T extends PiStateLivingEntityFacet<?>> PiLivingFacetType<T> require(Class<T> facetClass) {
        return find(facetClass).orElseThrow(() ->
                new IllegalStateException("Pi living facet " + facetClass.getName() + " is discovered but not registered"));
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
    private static <T extends PiStateLivingEntityFacet<?>> PiLivingFacetType<T> cast(Object value) {
        return (PiLivingFacetType<T>) value;
    }
}
