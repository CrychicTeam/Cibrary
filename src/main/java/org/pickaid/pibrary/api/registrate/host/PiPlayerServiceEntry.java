package org.pickaid.pibrary.api.registrate.host;

import java.util.Objects;
import java.util.function.Function;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateDefaults;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.runtime.registrate.host.PiRegistrateHostBridge;

public final class PiPlayerServiceEntry<S, T extends PiStateLivingEntityService<S>> {
    private final PiRegistrate owner;
    private final String path;
    private final Class<S> stateType;
    private final Function<PiLivingServiceContext, T> factory;
    private PiServiceSyncMode syncMode = PiServiceSyncMode.NONE;
    private boolean persisted;

    public PiPlayerServiceEntry(PiRegistrate owner, String path, Class<S> stateType, Function<PiLivingServiceContext, T> factory) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = PiRegistrateDefaults.requirePath(path);
        this.stateType = Objects.requireNonNull(stateType, "stateType");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public PiPlayerServiceEntry<S, T> ownerSync() {
        syncMode = PiServiceSyncMode.OWNER;
        return this;
    }

    public PiPlayerServiceEntry<S, T> trackingSync() {
        syncMode = PiServiceSyncMode.TRACKING;
        return this;
    }

    public PiPlayerServiceEntry<S, T> noSyncByDefault() {
        syncMode = PiServiceSyncMode.NONE;
        return this;
    }

    public PiPlayerServiceEntry<S, T> persisted() {
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

    public Function<PiLivingServiceContext, T> factory() {
        return factory;
    }

    public PiServiceSyncMode syncMode() {
        return syncMode;
    }

    public boolean persistedValue() {
        return persisted;
    }

    public PiLivingServiceType<T> register(Class<T> serviceType) {
        return PiRegistrateHostBridge.registerLiving(owner, path, stateType, Objects.requireNonNull(serviceType, "serviceType"));
    }
}
