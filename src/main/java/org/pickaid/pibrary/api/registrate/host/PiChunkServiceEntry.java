package org.pickaid.pibrary.api.registrate.host;

import java.util.Objects;
import java.util.function.Function;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateDefaults;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.runtime.registrate.host.PiRegistrateHostBridge;

public final class PiChunkServiceEntry<S, T extends PiStateChunkService<S>> {
    private final PiRegistrate owner;
    private final String path;
    private final Class<S> stateType;
    private final Function<PiChunkServiceContext, T> factory;
    private PiServiceSyncMode syncMode = PiServiceSyncMode.NONE;
    private boolean persisted;

    public PiChunkServiceEntry(PiRegistrate owner, String path, Class<S> stateType, Function<PiChunkServiceContext, T> factory) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = PiRegistrateDefaults.requirePath(path);
        this.stateType = Objects.requireNonNull(stateType, "stateType");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public PiChunkServiceEntry<S, T> ownerSync() {
        syncMode = PiServiceSyncMode.OWNER;
        return this;
    }

    public PiChunkServiceEntry<S, T> trackingSync() {
        syncMode = PiServiceSyncMode.TRACKING;
        return this;
    }

    public PiChunkServiceEntry<S, T> noSyncByDefault() {
        syncMode = PiServiceSyncMode.NONE;
        return this;
    }

    public PiChunkServiceEntry<S, T> persisted() {
        persisted = true;
        return this;
    }

    public PiRegistrate owner() {
        return owner;
    }

    public String path() {
        return path;
    }

    public Class<S> stateType() {
        return stateType;
    }

    public Function<PiChunkServiceContext, T> factory() {
        return factory;
    }

    public PiServiceSyncMode syncMode() {
        return syncMode;
    }

    public boolean persistedValue() {
        return persisted;
    }

    public PiChunkServiceType<T> register(Class<T> serviceType) {
        return PiRegistrateHostBridge.registerChunk(owner, path, stateType, Objects.requireNonNull(serviceType, "serviceType"));
    }
}
