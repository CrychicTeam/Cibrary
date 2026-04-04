package org.pickaid.pibrary.api.service;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import org.pickaid.pinet.api.sync.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.PiSyncRoute;
import org.pickaid.pibrary.runtime.state.PiHostState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

public abstract class PiStateLivingEntityService<S> extends PiLivingEntityService {
    private final PiHostState<S> host;

    protected PiStateLivingEntityService(PiLivingServiceContext context, Class<S> stateType) {
        super(context);
        this.host = new PiHostState<>(stateType);
    }

    public final S viewState() {
        return host.viewState();
    }

    protected final boolean updateState(Consumer<S> change) {
        return host.updateState(change);
    }

    protected final PiDirtySet dirtySet() {
        return host.dirtySet();
    }

    public final boolean hasDirty(PiSyncRoute route) {
        return host.hasDirty(routeFilter(route));
    }

    public final CompoundTag savePersistentData() {
        return host.saveFull();
    }

    public final void loadPersistentData(CompoundTag tag, PiDecodeContext context) {
        host.loadFull(tag, context);
    }

    public final CompoundTag buildSyncPayload(PiSyncEnvelopeKind kind, PiSyncRoute route) {
        Predicate<PiFieldDescriptor> filter = routeFilter(route);
        return kind == PiSyncEnvelopeKind.FULL ? host.saveFiltered(filter) : host.writeDelta(filter);
    }

    public final void applySyncPayload(PiSyncEnvelopeKind kind, CompoundTag tag, PiDecodeContext context) {
        if (kind == PiSyncEnvelopeKind.FULL) {
            host.loadFull(tag, context);
            return;
        }
        host.applyDelta(tag, context);
    }

    public final void copyFrom(PiStateLivingEntityService<S> other) {
        host.loadFull(other.host.saveFull(), PiDecodeContext.strict());
    }

    public final void clearDirty() {
        host.clearDirty();
    }

    public final void clearDirty(PiSyncRoute route) {
        host.clearDirty(routeFilter(route));
    }

    private Predicate<PiFieldDescriptor> routeFilter(PiSyncRoute route) {
        return field -> visibleToRoute(field.syncScope(), route);
    }

    private boolean visibleToRoute(PiSyncScope scope, PiSyncRoute route) {
        return switch (route) {
            case OWNER, PLAYER -> switch (scope) {
                case OWNER, TRACKING, CHUNK, GLOBAL -> true;
                case NONE, MENU -> false;
            };
            case TRACKING -> switch (scope) {
                case TRACKING, CHUNK, GLOBAL -> true;
                case NONE, OWNER, MENU -> false;
            };
            case CHUNK -> scope == PiSyncScope.CHUNK || scope == PiSyncScope.GLOBAL;
            case MENU -> switch (scope) {
                case MENU, OWNER, TRACKING, CHUNK, GLOBAL -> true;
                case NONE -> false;
            };
            case GLOBAL -> scope == PiSyncScope.GLOBAL;
        };
    }
}
