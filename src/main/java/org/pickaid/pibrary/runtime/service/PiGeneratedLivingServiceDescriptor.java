package org.pickaid.pibrary.runtime.service;

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
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceDescriptor;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

/**
 * Generated runtime descriptor that binds a living service type to its capability,
 * state type, and sync lifecycle helpers.
 *
 * @param <T> service type
 * @param <S> backing state type
 */
public abstract class PiGeneratedLivingServiceDescriptor<T extends PiStateLivingEntityService<S>, S>
        implements PiLivingServiceDescriptor<T, S>, PiLivingServiceType<T> {
    private final ResourceLocation id;
    private final Class<T> serviceType;
    private final Class<S> stateType;

    protected PiGeneratedLivingServiceDescriptor(ResourceLocation id, Class<T> serviceType, Class<S> stateType) {
        this.id = Objects.requireNonNull(id, "id");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.stateType = Objects.requireNonNull(stateType, "stateType");
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final Class<T> serviceType() {
        return serviceType;
    }

    @Override
    public final Class<S> stateType() {
        return stateType;
    }

    /**
     * Returns the generated capability carrying the service instance.
     *
     * @return service capability
     */
    @Override
    public abstract Capability<T> capability();

    @Override
    public final boolean isRegistered() {
        return PiActiveLivingServiceRegistry.isRegistered(serviceType);
    }

    /**
     * Creates a capability provider with a fresh attached host.
     *
     * @param living owning entity, when available
     * @return serializable capability provider
     */
    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LivingEntity living) {
        return createProvider(living, new PiAttachedLivingHost(living));
    }

    /**
     * Creates a capability provider using the supplied host.
     *
     * @param living owning entity, when available
     * @param host living service host
     * @return serializable capability provider
     */
    public ICapabilitySerializable<CompoundTag> createProvider(@Nullable LivingEntity living, PiLivingServiceHost host) {
        return new PiLivingServiceInstanceProvider<>(living, this, host);
    }

    /**
     * Finds the attached service on a living entity.
     *
     * @param living owning entity
     * @return attached service, if present
     */
    @Override
    public Optional<T> find(LivingEntity living) {
        return living.getCapability(capability()).resolve();
    }

    /**
     * Requires the attached service on a living entity.
     *
     * @param living owning entity
     * @return attached service
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
        event.register(serviceType);
    }

    /**
     * Returns whether the attached service has visible dirty data for the given route.
     *
     * @param living owning entity
     * @param route sync route being flushed
     * @return {@code true} when visible dirty data exists
     */
    public boolean hasDirty(LivingEntity living, PiSyncRoute route) {
        return find(living).map(service -> service.hasDirty(route)).orElse(false);
    }

    /**
     * Builds a sync payload for the attached service.
     *
     * @param living owning entity
     * @param kind full or delta sync
     * @param route target route
     * @return service sync payload
     */
    public CompoundTag buildSyncPayload(LivingEntity living, PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return find(living)
                .map(service -> service.buildSyncPayload(kind, route))
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
        find(living).ifPresent(service -> {
            PiDecodeContext context = PiDecodeContext.strict();
            service.applySyncPayload(kind, payload, context);
            PiLivingServiceLifecycles.onSyncApplied(service, kind, route, payload, context);
        });
    }

    /**
     * Clears dirty flags visible to the given route.
     *
     * @param living owning entity
     * @param route flushed route
     */
    public void clearDirty(LivingEntity living, PiSyncRoute route) {
        find(living).ifPresent(service -> service.clearDirty(route));
    }

    /**
     * Copies service state from a source entity into a target entity and runs clone hooks.
     *
     * @param source source entity
     * @param target target entity
     * @param wasDeath whether the clone was created by death/respawn
     */
    public void copy(LivingEntity source, LivingEntity target, boolean wasDeath) {
        Optional<T> sourceService = find(source);
        Optional<T> targetService = find(target);
        if (sourceService.isPresent() && targetService.isPresent()) {
            targetService.get().copyFrom(sourceService.get());
            PiLivingServiceLifecycles.onCloned(targetService.get(), source, wasDeath);
        }
    }

    /**
     * Creates a query context for author-facing lookups.
     *
     * @param living owning entity, when available
     * @return living service context
     */
    public final PiLivingServiceContext context(@Nullable LivingEntity living) {
        return context(living, new PiLivingQueryHost(living));
    }

    /**
     * Creates a context using an explicit host.
     *
     * @param living owning entity, when available
     * @param host living service host
     * @return living service context
     */
    public final PiLivingServiceContext context(@Nullable LivingEntity living, PiLivingServiceHost host) {
        return new PiLivingServiceContext(living, host);
    }
}
