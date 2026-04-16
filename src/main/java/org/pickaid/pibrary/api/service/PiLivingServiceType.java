package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;

public interface PiLivingServiceType<T extends PiStateLivingEntityService<?>> {
    ResourceLocation id();

    Class<T> serviceType();

    Capability<T> capability();

    boolean isRegistered();

    Optional<T> find(LivingEntity living);

    default T get(LivingEntity living) {
        Objects.requireNonNull(living, "living");
        return find(living).orElseThrow(() ->
                new IllegalStateException("Missing Pi living service " + serviceType().getName() + " on " + living.getClass().getName()));
    }

    default boolean supports(LivingEntity living) {
        return living.getCapability(capability()).isPresent();
    }
}
