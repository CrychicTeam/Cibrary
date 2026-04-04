package org.pickaid.pibrary.api.service;

import net.minecraft.resources.ResourceLocation;

public interface PiLivingServiceDescriptor<T extends PiLivingEntityService, S> {
    ResourceLocation id();

    Class<T> serviceType();

    Class<S> stateType();

    T create(PiLivingServiceContext context);
}
