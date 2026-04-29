package org.pickaid.pibrary.runtime.facet;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

/**
 * Capability provider wrapping one generated living facet instance.
 *
 * @param <T> facet type
 * @param <S> backing state type
 */
public final class PiLivingFacetInstanceProvider<T extends PiStateLivingEntityFacet<S>, S>
        implements ICapabilitySerializable<CompoundTag> {
    private final PiGeneratedLivingFacetDescriptor<T, S> descriptor;
    private final T facet;
    private final LazyOptional<T> instance;

    /**
     * Creates the provider, instantiates the facet, attaches it to the container when
     * possible, and runs attach lifecycle hooks.
     *
     * @param living owning entity, when available
     * @param descriptor generated facet descriptor
     * @param container living facet container
     */
    public PiLivingFacetInstanceProvider(
            @Nullable LivingEntity living,
            PiGeneratedLivingFacetDescriptor<T, S> descriptor,
            PiLivingFacetContainer container
    ) {
        this.descriptor = descriptor;
        this.facet = descriptor.create(descriptor.context(living, container));
        if (container instanceof PiAttachedLivingFacetContainer attachedContainer) {
            attachedContainer.attach(descriptor.facetClass(), facet);
        }
        PiLivingFacetLifecycles.onAttached(facet);
        this.instance = LazyOptional.of(() -> facet);
    }

    @Override
    public @NotNull <U> LazyOptional<U> getCapability(@NotNull Capability<U> capability, @Nullable Direction side) {
        return descriptor.capability().orEmpty(capability, instance);
    }

    @Override
    public CompoundTag serializeNBT() {
        return facet.savePersistentData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        facet.loadPersistentData(nbt, PiDecodeContext.strict());
    }

    /**
     * Invalidates the exposed lazy capability wrapper.
     */
    public void invalidate() {
        instance.invalidate();
    }

    /**
     * Returns the created facet instance.
     *
     * @return facet instance
     */
    public T facet() {
        return facet;
    }
}
