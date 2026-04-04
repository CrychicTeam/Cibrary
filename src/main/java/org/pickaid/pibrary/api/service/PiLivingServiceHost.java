package org.pickaid.pibrary.api.service;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface PiLivingServiceHost {
    @Nullable
    LivingEntity living();

    <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType);
}
