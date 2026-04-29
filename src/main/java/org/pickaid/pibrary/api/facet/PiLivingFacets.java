package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.facet.PiActiveLivingFacetRegistry;
import org.pickaid.pibrary.runtime.facet.PiLivingFacetAccess;

/**
 * Entry points for registering and resolving living-entity facets.
 */
public final class PiLivingFacets {
    private PiLivingFacets() {
    }

    /**
     * Creates a container view for the given living entity.
     *
     * @param living target living entity
     * @return facet container
     */
    public static PiLivingFacetContainer container(LivingEntity living) {
        return new PiLivingFacetAccess(Objects.requireNonNull(living, "living"));
    }

    /**
     * Creates a registration binding for the given living facet class.
     *
     * @param facetClass discovered living facet class
     * @param <T> facet type
     * @return registration binding
     */
    public static <T extends PiStateLivingEntityFacet<?>> PiLivingFacetBinding<T> bind(Class<T> facetClass) {
        return new PiLivingFacetBinding<>(facetClass);
    }

    /**
     * Creates a container view when the living entity is present.
     *
     * @param living target living entity
     * @return optional facet container
     */
    public static Optional<PiLivingFacetContainer> findContainer(@Nullable LivingEntity living) {
        return living == null ? Optional.empty() : Optional.of(container(living));
    }

    /**
     * Requires a container view for the given living entity.
     *
     * @param living target living entity
     * @return facet container
     */
    public static PiLivingFacetContainer requireContainer(LivingEntity living) {
        return container(living);
    }

    /**
     * Finds an attached living facet by generated descriptor type.
     *
     * @param living owning entity
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return attached facet, if present
     */
    public static <T extends PiStateLivingEntityFacet<?>> Optional<T> find(LivingEntity living, Class<T> facetClass) {
        return PiActiveLivingFacetRegistry.find(facetClass).flatMap(descriptor -> descriptor.find(living));
    }

    /**
     * Returns the active typed handle for a registered living facet.
     *
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return registered living facet type handle
     */
    public static <T extends PiStateLivingEntityFacet<?>> PiLivingFacetType<T> type(Class<T> facetClass) {
        return PiActiveLivingFacetRegistry.require(facetClass);
    }

    /**
     * Requires an attached living facet by registered descriptor type.
     *
     * @param living owning entity
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return attached facet
     */
    public static <T extends PiStateLivingEntityFacet<?>> T require(LivingEntity living, Class<T> facetClass) {
        return type(facetClass).get(living);
    }
}
