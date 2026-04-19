package org.pickaid.pibrary.api.registrate.config;

import com.mojang.serialization.Codec;
import java.util.Objects;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateDefaults;
import org.pickaid.pibrary.runtime.registrate.config.PiConfigRegistry;

public final class PiConfigRegistration<T> {
    private final PiRegistrate owner;
    private final String path;
    private final Codec<T> codec;
    private final T defaultValue;
    private PiConfigScope scope = PiConfigScope.COMMON_BOOTSTRAP;

    public PiConfigRegistration(PiRegistrate owner, String path, Codec<T> codec, T defaultValue) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.path = PiRegistrateDefaults.requirePath(path);
        this.codec = Objects.requireNonNull(codec, "codec");
        this.defaultValue = defaultValue;
    }

    public PiConfigRegistration<T> scope(PiConfigScope scope) {
        this.scope = Objects.requireNonNull(scope, "scope");
        return this;
    }

    public PiConfigEntry<T> register() {
        return PiConfigRegistry.register(new PiConfigEntry<>(owner.id(path), codec, scope, defaultValue));
    }
}
