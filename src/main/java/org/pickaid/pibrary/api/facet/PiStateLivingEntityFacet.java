package org.pickaid.pibrary.api.facet;

import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pinet.api.sync.model.PiSyncTarget;
import org.pickaid.pibrary.runtime.state.PiFacetState;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;
import org.pickaid.pibrary.runtime.sync.PiSyncRouteVisibility;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiProjection;

/**
 * Base class for living facets backed by a generated state schema.
 *
 * @param <S> backing state type
 */
public abstract class PiStateLivingEntityFacet<S> extends PiLivingEntityFacet {
    private final PiFacetState<S> state;

    @SuppressWarnings("unchecked")
    protected PiStateLivingEntityFacet(PiLivingFacetContext context) {
        super(context);
        Class<S> stateType = PiStateTypeResolver.livingFacetStateType((Class<? extends PiStateLivingEntityFacet<S>>) getClass());
        this.state = new PiFacetState<>(stateType);
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
     * Applies a state mutation and tracks dirty fields through the backing facet state.
     *
     * @param change state mutation callback
     * @return {@code true} when the state changed
     */
    protected final boolean updateState(Consumer<S> change) {
        return state.updateState(change);
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
     * Copies the full state from another facet instance of the same type.
     *
     * @param other source facet
     */
    public final void copyFrom(PiStateLivingEntityFacet<S> other) {
        state.loadFull(other.state.saveFull(), PiDecodeContext.strict());
    }

    /**
     * Clears dirty flags for every sync scope.
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
