package org.pickaid.pibrary.api.registrate.registry;

import com.mojang.serialization.Codec;
import java.util.Objects;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.runtime.registrate.registry.PiCustomRegistryDefinition;
import org.pickaid.pibrary.runtime.registrate.registry.PiRegistryCatalog;

public final class PiCustomRegistryBuilder<T> {
    private final PiRegistrate owner;
    private final String path;
    private final Class<T> valueType;
    private Codec<T> codec;
    private String defaultPath;

    public PiCustomRegistryBuilder(PiRegistrate owner, String path, Class<T> valueType) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = Objects.requireNonNull(path, "path");
        this.valueType = Objects.requireNonNull(valueType, "valueType");
    }

    public PiCustomRegistryBuilder<T> codec(Codec<T> codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
        return this;
    }

    public PiCustomRegistryBuilder<T> defaultKey(String defaultPath) {
        this.defaultPath = defaultPath;
        return this;
    }

    public PiRegistryHandle<T> register() {
        PiCustomRegistryDefinition<T> definition = PiCustomRegistryDefinition.of(owner.id(path), codec, defaultPath);
        return PiRegistryCatalog.register(definition);
    }
}
