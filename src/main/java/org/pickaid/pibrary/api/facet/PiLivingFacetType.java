package org.pickaid.pibrary.api.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;

public interface PiLivingFacetType<T extends PiStateLivingEntityFacet<?>> {
    ResourceLocation id();

    Class<T> facetClass();

    Capability<T> capability();

    boolean isRegistered();

    Optional<T> find(LivingEntity living);

    default T get(LivingEntity living) {
        Objects.requireNonNull(living, "living");
        return find(living).orElseThrow(() ->
                new IllegalStateException("Missing Pi living facet " + facetClass().getName() + " on " + living.getClass().getName()));
    }

    default boolean supports(LivingEntity living) {
        return living.getCapability(capability()).isPresent();
    }
}
