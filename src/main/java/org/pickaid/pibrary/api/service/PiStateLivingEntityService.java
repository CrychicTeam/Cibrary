package org.pickaid.pibrary.api.service;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pinet.api.sync.model.PiSyncTarget;
import org.pickaid.pibrary.runtime.state.PiHostState;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;
import org.pickaid.pibrary.runtime.sync.PiSyncRouteVisibility;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiProjection;

/**
 * Base class for living services backed by a generated state schema.
 *
 * @param <S> backing state type
 */
public abstract class PiStateLivingEntityService<S> extends PiLivingEntityService {
    private final PiHostState<S> host;

    @SuppressWarnings("unchecked")
    protected PiStateLivingEntityService(PiLivingServiceContext context) {
        super(context);
        Class<S> stateType = PiStateTypeResolver.livingServiceStateType((Class<? extends PiStateLivingEntityService<S>>) getClass());
        this.host = new PiHostState<>(stateType);
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
     * Applies a state mutation and tracks dirty fields through the backing host state.
     *
     * @param change state mutation callback
     * @return {@code true} when the state changed
     */
    protected final boolean updateState(Consumer<S> change) {
        return host.updateState(change);
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
     * Copies the full state from another service instance of the same type.
     *
     * @param other source service
     */
    public final void copyFrom(PiStateLivingEntityService<S> other) {
        host.loadFull(other.host.saveFull(), PiDecodeContext.strict());
    }

    /**
     * Clears dirty flags for every sync scope.
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
