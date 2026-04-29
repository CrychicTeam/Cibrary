package org.pickaid.pibrary.api.facet;

import net.minecraft.resources.ResourceLocation;

/**
 * Runtime descriptor for a chunk facet type.
 *
 * @param <T> facet type
 * @param <S> backing state type
 */
public interface PiChunkFacetDescriptor<T extends PiStateChunkFacet<S>, S> {
    ResourceLocation id();

    Class<T> facetClass();

    Class<S> stateType();

    T create(PiChunkFacetContext context);
}
