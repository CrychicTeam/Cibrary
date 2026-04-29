package org.pickaid.pibrary.api.facet;

import net.minecraft.resources.ResourceLocation;

/**
 * Runtime descriptor for a living facet type.
 *
 * @param <T> facet type
 * @param <S> backing state type
 */
public interface PiLivingFacetDescriptor<T extends PiLivingEntityFacet, S> {
    /**
     * Returns the unique id of the described facet.
     *
     * @return facet id
     */
    ResourceLocation id();

    /**
     * Returns the facet implementation class.
     *
     * @return facet class
     */
    Class<T> facetClass();

    /**
     * Returns the backing state type.
     *
     * @return state class
     */
    Class<S> stateType();

    /**
     * Creates a new facet instance for the given context.
     *
     * @param context living facet context
     * @return new facet instance
     */
    T create(PiLivingFacetContext context);
}
