package org.pickaid.pibrary.runtime.config;

import java.util.Objects;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;

/**
 * Current values loaded for one datapack-backed config spec.
 */
public final class PiConfigResourceBinding {
    private final PiConfigSpec spec;
    private volatile PiConfigValues values;

    PiConfigResourceBinding(PiConfigSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.values = PiConfigValues.defaults(spec);
    }

    public PiConfigSpec spec() {
        return spec;
    }

    public PiConfigValues values() {
        return values;
    }

    public <T> T get(PiConfigEntry<T> entry) {
        return values.get(entry);
    }

    void update(PiConfigValues values) {
        if (!values.spec().equals(spec)) {
            throw new IllegalArgumentException("loaded values belong to another config spec: " + values.spec().id());
        }
        this.values = values;
    }
}
