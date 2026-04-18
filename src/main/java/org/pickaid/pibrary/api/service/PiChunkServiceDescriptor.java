package org.pickaid.pibrary.api.service;

import net.minecraft.resources.ResourceLocation;

/**
 * Runtime descriptor for a chunk service type.
 *
 * @param <T> service type
 * @param <S> backing state type
 */
public interface PiChunkServiceDescriptor<T extends PiStateChunkService<S>, S> {
    /**
     * Returns the unique id of the described service.
     *
     * @return service id
     */
    ResourceLocation id();

    /**
     * Returns the service implementation type.
     *
     * @return service class
     */
    Class<T> serviceType();

    /**
     * Returns the backing state type.
     *
     * @return state class
     */
    Class<S> stateType();

    /**
     * Returns the storage id used by the runtime backend.
     *
     * @return storage id
     */
    String storageId();

    /**
     * Creates a new service instance for the given context.
     *
     * @param context chunk service context
     * @return new service instance
     */
    T create(PiChunkServiceContext context);
}
