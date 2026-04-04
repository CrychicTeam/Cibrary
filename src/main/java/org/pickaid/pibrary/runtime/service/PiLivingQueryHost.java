package org.pickaid.pibrary.runtime.service;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public final class PiLivingQueryHost implements PiLivingServiceHost {
    private final @Nullable LivingEntity living;

    public PiLivingQueryHost(@Nullable LivingEntity living) {
        this.living = living;
    }

    @Override
    public @Nullable LivingEntity living() {
        return living;
    }

    @Override
    public <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType) {
        if (living == null) {
            throw new IllegalStateException("Detached Pi living query host cannot resolve " + serviceType.getName());
        }
        return PiLivingServiceDescriptors.requireGenerated(serviceType).require(living);
    }
}
