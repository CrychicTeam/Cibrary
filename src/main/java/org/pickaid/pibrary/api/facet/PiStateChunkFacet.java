package org.pickaid.pibrary.api.facet;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pinet.api.sync.model.PiSyncTarget;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.runtime.state.PiFacetState;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;
import org.pickaid.pibrary.runtime.sync.PiSyncRouteVisibility;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiProjection;

/**
 * Base class for chunk-scoped facets backed by a generated state schema.
 *
 * @param <S> backing state type
 */
public abstract class PiStateChunkFacet<S> {
    private final PiChunkFacetContext context;
    private final PiFacetState<S> state;

    @SuppressWarnings("unchecked")
    protected PiStateChunkFacet(PiChunkFacetContext context) {
        this.context = java.util.Objects.requireNonNull(context, "context");
        Class<S> stateType = PiStateTypeResolver.chunkFacetStateType((Class<? extends PiStateChunkFacet<S>>) getClass());
        this.state = new PiFacetState<>(stateType);
    }

    public final PiChunkFacetContext context() {
        return context;
    }

    protected final PibraryScope scope() {
        return context.scope();
    }

    protected final PibraryScope sharedScope() {
        return context.sharedScope();
    }

    protected final <T> Optional<T> findScoped(PibraryScopeKey<T> key) {
        return scope().find(key);
    }

    protected final <T> T requireScoped(PibraryScopeKey<T> key) {
        return scope().require(key);
    }

    protected final <T extends PiStateChunkFacet<?>> T facet(Class<T> facetClass) {
        return context.container().get(facetClass);
    }

    public final S viewState() {
        return state.viewState();
    }

    protected final boolean updateState(Consumer<S> change) {
        return state.updateState(change);
    }

    protected final PiDirtySet dirtySet() {
        return state.dirtySet();
    }

    public final boolean hasDirty(PiSyncRoute route) {
        return state.hasDirty(routeProjection(route));
    }

    public final CompoundTag savePersistentData() {
        return state.savePersisted();
    }

    public final void loadPersistentData(CompoundTag tag, PiDecodeContext context) {
        state.loadPersisted(tag, context);
    }

    public final CompoundTag buildSyncPayload(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        PiProjection projection = routeProjection(route);
        return kind == PiSyncEnvelopeKind.FULL ? state.saveProjection(projection) : state.writeDelta(projection);
    }

    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        return new PiSyncEnvelope(kind, route, state.schemaId(), buildSyncPayload(kind, route));
    }

    public final PiSyncEnvelope buildSyncEnvelope(PiSyncEnvelopeKind kind, PiSyncRoute route, PiSyncTarget target) {
        return new PiSyncEnvelope(kind, route, state.schemaId(), target, buildSyncPayload(kind, route));
    }

    public final void applySyncPayload(PiSyncEnvelopeKind kind, CompoundTag tag, PiDecodeContext context) {
        if (kind == PiSyncEnvelopeKind.FULL) {
            state.loadFull(tag, context);
            return;
        }
        state.applyDelta(tag, context);
    }

    public final void clearDirty() {
        state.clearDirty();
    }

    public final void clearDirty(PiSyncRoute route) {
        state.clearDirty(routeProjection(route));
    }

    private PiProjection routeProjection(PiSyncRoute route) {
        return PiSyncRouteVisibility.projection(route);
    }
}
