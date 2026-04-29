package org.pickaid.pibrary.runtime.facet;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetDescriptor;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiLivingFacetType;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;

/**
 * Generated runtime descriptor that binds a living facet class to its capability,
 * state type, and sync lifecycle helpers.
 *
 * @param <T> facet type
 * @param <S> backing state type
 */
public abstract class PiGeneratedLivingFacetDescriptor<T extends PiStateLivingEntityFacet<S>, S>
        implements PiLivingFacetDescriptor<T, S>, PiLivingFacetType<T> {
    private final ResourceLocation id;
    private final Class<T> facetClass;
    private final Class<S> stateType;

    protected PiGeneratedLivingFacetDescriptor(ResourceLocation id, Class<T> facetClass, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.facetClass = Objects.requireNonNull(facetClass, "facetClass");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final Class<T> facetClass() {
        return facetClass;
    }

    @Override
    public final Class<S> stateType() {
        return stateType;
    }

    /**
     * Returns the generated capability carrying the facet instance.
     *
     * @return facet capability
     */
    @Override
    public abstract Capability<T> capability();

    @Override
    public final boolean isRegistered() {
        return PiActiveLivingFacetRegistry.isRegistered(facetClass);
    }

    /**
     * Creates a capability provider with a fresh attached container.
     *
     * @param living owning entity, when available
     * @return serializable capability provider
     */
    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LivingEntity living) {
        return createProvider(living, new PiAttachedLivingFacetContainer(living));
    }

    /**
     * Creates a capability provider using the supplied container.
     *
     * @param living owning entity, when available
     * @param container living facet container
     * @return serializable capability provider
     */
    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LivingEntity living, PiLivingFacetContainer container) {
        return new PiLivingFacetInstanceProvider<>(living, this, container);
    }

    /**
     * Finds the attached facet on a living entity.
     *
     * @param living owning entity
     * @return attached facet, if present
     */
    @Override
    public Optional<T> find(LivingEntity living) {
        return living.getCapability(capability()).resolve();
    }

    /**
     * Requires the attached facet on a living entity.
     *
     * @param living owning entity
     * @return attached facet
     */
    public T require(LivingEntity living) {
        return get(living);
    }

    /**
     * Registers the generated capability class with Forge.
     *
     * @param event Forge capability registration event
     */
    public void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(facetClass);
    }

    /**
     * Returns whether the attached facet has visible dirty data for the given route.
     *
     * @param living owning entity
     * @param route sync route being flushed
     * @return {@code true} when visible dirty data exists
     */
    public boolean hasDirty(LivingEntity living, PiSyncRoute route) {
        return find(living).map(facet -> facet.hasDirty(route)).orElse(false);
    }

    /**
     * Builds a sync payload for the attached facet.
     *
     * @param living owning entity
     * @param kind full or delta sync
     * @param route target route
     * @return facet sync payload
     */
    public CompoundTag buildSyncPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return find(living)
                .map(facet -> facet.buildSyncPayload(kind, route))
                .orElseGet(CompoundTag::new);
    }

    /**
     * Applies a decoded sync payload and runs post-apply lifecycle hooks.
     *
     * @param living owning entity
     * @param kind full or delta sync
     * @param route route that delivered the payload
     * @param payload sync payload
     */
    public void applySyncPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route, CompoundTag payload) {
        find(living).ifPresent(facet -> {
            PiDecodeContext context = PiDecodeContext.strict();
            facet.applySyncPayload(kind, payload, context);
            PiLivingFacetLifecycles.onSyncApplied(facet, kind, route, payload, context);
        });
    }

    /**
     * Clears dirty flags visible to the given route.
     *
     * @param living owning entity
     * @param route flushed route
     */
    public void clearDirty(LivingEntity living, PiSyncRoute route) {
        find(living).ifPresent(facet -> facet.clearDirty(route));
    }

    /**
     * Copies facet state from a source entity into a target entity and runs clone hooks.
     *
     * @param source source entity
     * @param target target entity
     * @param wasDeath whether the clone was created by death/respawn
     */
    public void copy(LivingEntity source, LivingEntity target, boolean wasDeath) {
        Optional<T> sourceFacet = find(source);
        Optional<T> targetFacet = find(target);
        if (sourceFacet.isPresent() && targetFacet.isPresent()) {
            targetFacet.get().copyFrom(sourceFacet.get());
            PiLivingFacetLifecycles.onCloned(targetFacet.get(), source, wasDeath);
        }
    }

    /**
     * Creates a query context for resolving facets against one living entity.
     *
     * @param living owning entity, when available
     * @return living facet context
     */
    public final PiLivingFacetContext context(@Nullable LivingEntity living) {
        return context(living, new PiLivingFacetAccess(living));
    }

    /**
     * Creates a context using an explicit container.
     *
     * @param living owning entity, when available
     * @param container living facet container
     * @return living facet context
     */
    public final PiLivingFacetContext context(@Nullable LivingEntity living, PiLivingFacetContainer container) {
        return new PiLivingFacetContext(living, container);
    }
}
