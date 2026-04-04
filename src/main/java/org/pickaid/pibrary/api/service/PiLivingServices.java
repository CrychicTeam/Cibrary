package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.runtime.service.PiLivingQueryHost;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;

public final class PiLivingServices {
    private PiLivingServices() {
    }

    public static PiLivingServiceHost host(LivingEntity living) {
        return new PiLivingQueryHost(Objects.requireNonNull(living, "living"));
    }

    public static Optional<PiLivingServiceHost> findHost(@Nullable LivingEntity living) {
        return living == null ? Optional.empty() : Optional.of(host(living));
    }

    public static PiLivingServiceHost requireHost(LivingEntity living) {
        return host(living);
    }

    public static <T extends PiStateLivingEntityService<?>> Optional<T> find(LivingEntity living, Class<T> serviceType) {
        return PiLivingServiceDescriptors.findGenerated(serviceType).flatMap(descriptor -> descriptor.find(living));
    }

    public static <T extends PiStateLivingEntityService<?>> T require(LivingEntity living, Class<T> serviceType) {
        return PiLivingServiceDescriptors.requireGenerated(serviceType).require(living);
    }
}
