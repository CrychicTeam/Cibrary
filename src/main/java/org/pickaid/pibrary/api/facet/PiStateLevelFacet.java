package org.pickaid.pibrary.api.facet;

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
import org.pickaid.pibrary.runtime.state.PiFacetState;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;
import org.pickaid.pibrary.runtime.sync.PiSyncRouteVisibility;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiProjection;

/**
 * Lightweight state holder for level-scoped facets.
 *
 * <p>This base exposes persistence and sync helpers, but phase-1 runtime only guarantees
 * persisted storage. Level facets may build or apply sync payloads when a higher layer
 * chooses to transport them, but no default runtime path auto-syncs level facets.</p>
 *
 * @param <S> backing state type
 */
public abstract class PiStateLevelFacet<S> {
    private final PiLevelFacetContext context;
    private final PiFacetState<S> state;

    @SuppressWarnings("unchecked")
    protected PiStateLevelFacet(PiLevelFacetContext context) {
        this.context = Objects.requireNonNull(context, "context");
        Class<S> stateType = PiStateTypeResolver.levelFacetStateType((Class<? extends PiStateLevelFacet<S>>) getClass());
        this.state = new PiFacetState<>(stateType);
    }

    /**
     * Returns the immutable facet context.
     *
     * @return facet context
     */
    public final PiLevelFacetContext context() {
        return context;
    }

    /**
     * Returns the scoped service registry owned by this facet instance.
     *
     * @return scoped service registry
     */
    protected final PibraryServiceContext services() {
        return context.services();
    }

    /**
     * Returns the shared level service registry.
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
        return state.viewState();
    }

    /**
     * Returns whether this facet has dirty fields visible on the requested route.
     *
     * @param route sync route being flushed
     * @return {@code true} when visible dirty data exists
     */
    public final boolean hasDirty(PiSyncRoute route) {
        return state.hasDirty(routeProjection(route));
    }

    /**
     * Serializes the full persistent state.
     *
     * @return persistent payload
     */
    public final CompoundTag savePersistentData() {
        return state.savePersisted();
    }

    /**
     * Loads the full persistent state.
     *
     * @param tag persistent payload
     * @param context decode context
     */
    public final void loadPersistentData(CompoundTag tag, PiDecodeContext context) {
        state.loadPersisted(tag, context);
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
        return kind == PiSyncEnvelopeKind.FULL ? state.saveProjection(projection) : state.writeDelta(projection);
    }

    /**
     * Builds a transport-ready sync envelope using this facet's backing state schema id.
     * This does not enqueue or dispatch the envelope by itself.
     *
     * @param kind full or delta sync
     * @param route target sync route
     * @return sync envelope
     */
    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return new PiSyncEnvelope(kind, route, state.schemaId(), buildSyncPayload(kind, route));
    }

    /**
     * Builds a transport-ready sync envelope using this facet's backing state schema id
     * and the supplied route-local target identity.
     * This does not enqueue or dispatch the envelope by itself.
     *
     * @param kind full or delta sync
     * @param route target sync route
     * @param target route-local sync stream identity
     * @return sync envelope
     */
    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route, PiSyncTarget target) {
        return new PiSyncEnvelope(kind, route, state.schemaId(), target, buildSyncPayload(kind, route));
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
            state.loadFull(tag, context);
            return;
        }
        state.applyDelta(tag, context);
    }

    /**
     * Applies a state mutation and tracks dirty fields.
     *
     * @param change state mutation callback
     */
    protected final void updateState(Consumer<S> change) {
        state.updateState(change);
    }

    /**
     * Returns the dirty-set tracker owned by the backing facet state.
     *
     * @return dirty-set tracker
     */
    protected final PiDirtySet dirtySet() {
        return state.dirtySet();
    }

    /**
     * Clears all accumulated dirty flags.
     */
    public final void clearDirty() {
        state.clearDirty();
    }

    /**
     * Clears dirty flags visible to the given route.
     *
     * @param route sync route that was flushed
     */
    public final void clearDirty(PiSyncRoute route) {
        state.clearDirty(routeProjection(route));
    }

    private PiProjection routeProjection(PiSyncRoute route) {
        return PiSyncRouteVisibility.projection(route);
    }
}
