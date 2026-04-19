package org.pickaid.pibrary.api.registrate.registry;

import com.mojang.serialization.Codec;
import java.util.Objects;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.runtime.registrate.registry.PiDatapackRegistryDefinition;
import org.pickaid.pibrary.runtime.registrate.registry.PiRegistryCatalog;

public final class PiDatapackRegistryBuilder<T> {
    private final PiRegistrate owner;
    private final String path;
    private final Codec<T> directCodec;
    private final Codec<T> networkCodec;
    private boolean syncToClient = true;

    public PiDatapackRegistryBuilder(PiRegistrate owner, String path, Codec<T> directCodec, Codec<T> networkCodec) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = Objects.requireNonNull(path, "path");
        this.directCodec = Objects.requireNonNull(directCodec, "directCodec");
        this.networkCodec = Objects.requireNonNull(networkCodec, "networkCodec");
    }

    public PiDatapackRegistryBuilder<T> syncToClient() {
        this.syncToClient = true;
        return this;
    }

    public PiDatapackRegistryBuilder<T> noClientSync() {
        this.syncToClient = false;
        return this;
    }

    @SuppressWarnings("unchecked")
    public PiDatapackRegistryHandle<T> register() {
        return (PiDatapackRegistryHandle<T>) PiRegistryCatalog.register(
                PiDatapackRegistryDefinition.of(owner.id(path), directCodec, networkCodec, syncToClient));
    }
}
