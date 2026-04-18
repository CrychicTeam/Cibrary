package org.pickaid.pibrary.api.service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pinet.api.sync.model.PiSyncTarget;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.runtime.state.PiHostState;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;
import org.pickaid.pibrary.runtime.sync.PiSyncRouteVisibility;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiProjection;

/**
 * Lightweight state holder for level-scoped services.
 *
 * <p>This base exposes persistence and sync helpers, but phase-1 runtime only guarantees
 * persisted storage. Level services may build or apply sync payloads when a higher layer
 * chooses to transport them, but no default runtime path auto-syncs level services.</p>
 *
 * @param <S> backing state type
 */
public abstract class PiStateLevelService<S> {
    private final PiLevelServiceContext context;
    private final PiHostState<S> host;

    @SuppressWarnings("unchecked")
    protected PiStateLevelService(PiLevelServiceContext context) {
        this.context = Objects.requireNonNull(context, "context");
        Class<S> stateType = PiStateTypeResolver.levelServiceStateType((Class<? extends PiStateLevelService<S>>) getClass());
        this.host = new PiHostState<>(stateType);
    }

    /**
     * Returns the immutable service context.
     *
     * @return service context
     */
    public final PiLevelServiceContext context() {
        return context;
    }

    /**
     * Returns the scoped service registry owned by this service instance.
     *
     * @return scoped service registry
     */
    protected final PibraryServiceContext services() {
        return context.services();
    }

    /**
     * Returns the shared level-level service registry.
     *
     * @return shared level service registry
     */
    protected final PibraryServiceContext sharedServices() {
        return context.sharedServices();
    }

    /**
     * Finds another service in the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service, if present
     */
    protected final <T> Optional<T> findService(PibraryServiceKey<T> key) {
        return services().find(key);
    }

    /**
     * Requires another service from the local scoped registry.
     *
     * @param key service key
     * @param <T> service type
     * @return resolved service
     */
    protected final <T> T requireService(PibraryServiceKey<T> key) {
        return services().require(key);
    }

    /**
     * Returns the mutable backing state view.
     *
     * @return backing state
     */
    public final S viewState() {
        return host.viewState();
    }

    /**
     * Returns whether this service has dirty fields visible on the requested route.
     *
     * @param route sync route being flushed
     * @return {@code true} when visible dirty data exists
     */
    public final boolean hasDirty(PiSyncRoute route) {
        return host.hasDirty(routeProjection(route));
    }

    /**
     * Serializes the full persistent state.
     *
     * @return persistent payload
     */
    public final CompoundTag savePersistentData() {
        return host.savePersisted();
    }

    /**
     * Loads the full persistent state.
     *
     * @param tag persistent payload
     * @param context decode context
     */
    public final void loadPersistentData(CompoundTag tag, PiDecodeContext context) {
        host.loadPersisted(tag, context);
    }

    /**
     * Builds a sync payload filtered for the requested route.
     * This does not send anything by itself.
     *
     * @param kind full or delta sync
     * @param route target sync route
     * @return sync payload
     */
    public final CompoundTag buildSyncPayload(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        PiProjection projection = routeProjection(route);
        return kind == PiSyncEnvelopeKind.FULL ? host.saveProjection(projection) : host.writeDelta(projection);
    }

    /**
     * Builds a transport-ready sync envelope using this service's backing state schema id.
     * This does not enqueue or dispatch the envelope by itself.
     *
     * @param kind full or delta sync
     * @param route target sync route
     * @return sync envelope
     */
    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return new PiSyncEnvelope(kind, route, host.schemaId(), buildSyncPayload(kind, route));
    }

    /**
     * Builds a transport-ready sync envelope using this service's backing state schema id
     * and the supplied route-local target identity.
     * This does not enqueue or dispatch the envelope by itself.
     *
     * @param kind full or delta sync
     * @param route target sync route
     * @param target route-local sync stream identity
     * @return sync envelope
     */
    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route, PiSyncTarget target) {
        return new PiSyncEnvelope(kind, route, host.schemaId(), target, buildSyncPayload(kind, route));
    }

    /**
     * Applies a sync payload received from the network.
     * Level runtime does not install a default sync transport in phase 1; higher layers call this explicitly.
     *
     * @param kind full or delta sync
     * @param tag payload to apply
     * @param context decode context
     */
    public final void applySyncPayload(PiSyncEnvelopeKind kind, CompoundTag tag, PiDecodeContext context) {
        if (kind == PiSyncEnvelopeKind.FULL) {
            host.loadFull(tag, context);
            return;
        }
        host.applyDelta(tag, context);
    }

    /**
     * Applies a state mutation and tracks dirty fields.
     *
     * @param change state mutation callback
     */
    protected final void updateState(Consumer<S> change) {
        host.updateState(change);
    }

    /**
     * Returns the dirty-set tracker owned by the backing host state.
     *
     * @return dirty-set tracker
     */
    protected final PiDirtySet dirtySet() {
        return host.dirtySet();
    }

    /**
     * Clears all accumulated dirty flags.
     */
    public final void clearDirty() {
        host.clearDirty();
    }

    /**
     * Clears dirty flags visible to the given route.
     *
     * @param route sync route that was flushed
     */
    public final void clearDirty(PiSyncRoute route) {
        host.clearDirty(routeProjection(route));
    }

    private PiProjection routeProjection(PiSyncRoute route) {
        return PiSyncRouteVisibility.projection(route);
    }
}
