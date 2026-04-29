package org.pickaid.pibrary.api.facet;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.core.PibraryScope;

/**
 * Container abstraction used to resolve living facets and expose a shared scope.
 */
public interface PiLivingFacetContainer {
    /**
     * Returns the owning living entity when available.
     *
     * @return owning entity or {@code null}
     */
    @Nullable
    LivingEntity living();

    /**
     * Returns the container-level shared scope.
     *
     * @return shared scope
     */
    PibraryScope scope();

    /**
     * Resolves an attached living facet from this container.
     *
     * @param facetClass requested facet class
     * @param <T> facet type
     * @return resolved facet
     */
    <T extends PiStateLivingEntityFacet<?>> T get(Class<T> facetClass);
}
