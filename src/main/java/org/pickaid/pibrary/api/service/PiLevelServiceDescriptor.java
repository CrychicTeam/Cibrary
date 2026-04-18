package org.pickaid.pibrary.api.service;

import net.minecraft.resources.ResourceLocation;

public interface PiLevelServiceDescriptor<T extends PiStateLevelService<S>, S> {
    ResourceLocation id();

    Class<T> serviceType();

    Class<S> stateType();

    String storageId();

    T create(PiLevelServiceContext context);
}
