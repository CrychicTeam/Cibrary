package org.pickaid.pibrary.api.service;

import net.minecraft.resources.ResourceLocation;

/**
 * Runtime descriptor for a living service type.
 *
 * @param <T> service type
 * @param <S> backing state type
 */
public interface PiLivingServiceDescriptor<T extends PiLivingEntityService, S> {
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
     * Creates a new service instance for the given context.
     *
     * @param context living service context
     * @return new service instance
     */
    T create(PiLivingServiceContext context);
}
