package org.pickaid.pibrary.api.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable typed values for one {@link PiConfigSpec}.
 *
 * <p>Loaders can decode raw files into this object, while gameplay code keeps
 * reading through typed {@link PiConfigEntry} handles. Missing values fall back
 * to entry defaults.</p>
 */
public final class PiConfigValues {
    private final PiConfigSpec spec;
    private final Map<ResourceLocation, Object> overrides;

    private PiConfigValues(PiConfigSpec spec, Map<ResourceLocation, Object> overrides) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.overrides = Map.copyOf(Objects.requireNonNull(overrides, "overrides"));
    }

    public static PiConfigValues defaults(PiConfigSpec spec) {
        return new PiConfigValues(spec, Map.of());
    }

    public static Builder builder(PiConfigSpec spec) {
        return new Builder(spec);
    }

    public PiConfigSpec spec() {
        return spec;
    }

    public boolean hasOverride(PiConfigEntry<?> entry) {
        return overrides.containsKey(requireRegistered(entry).id());
    }

    public Map<ResourceLocation, Object> overrides() {
        return overrides;
    }

    public Builder toBuilder() {
        return new Builder(spec, overrides);
    }

    public <T> T get(PiConfigEntry<T> entry) {
        PiConfigEntry<T> registeredEntry = requireRegistered(entry);
        Object value = overrides.getOrDefault(registeredEntry.id(), registeredEntry.defaultValue());
        return registeredEntryTypeCast(registeredEntry, value);
    }

    private <T> PiConfigEntry<T> requireRegistered(PiConfigEntry<T> entry) {
        Objects.requireNonNull(entry, "entry");
        PiConfigEntry<?> registered = spec.find(entry.id())
                .orElseThrow(() -> new IllegalArgumentException("config entry does not belong to spec: " + entry.id()));
        if (registered != entry && !registered.equals(entry)) {
            throw new IllegalArgumentException("config entry id is registered with a different definition: " + entry.id());
        }
        return entry;
    }

    @SuppressWarnings("unchecked")
    private static <T> T registeredEntryTypeCast(PiConfigEntry<T> entry, Object value) {
        try {
            return (T) value;
        } catch (ClassCastException exception) {
            throw new IllegalStateException("stored config value does not match entry type: " + entry.id(), exception);
        }
    }

    public static final class Builder {
        private final PiConfigSpec spec;
        private final Map<ResourceLocation, Object> overrides;

        private Builder(PiConfigSpec spec) {
            this(spec, Map.of());
        }

        private Builder(PiConfigSpec spec, Map<ResourceLocation, Object> overrides) {
            this.spec = Objects.requireNonNull(spec, "spec");
            this.overrides = new LinkedHashMap<>(Objects.requireNonNull(overrides, "overrides"));
        }

        public <T> Builder set(PiConfigEntry<T> entry, T value) {
            Objects.requireNonNull(value, "value");
            PiConfigEntry<T> registeredEntry = requireRegistered(entry);
            var errors = registeredEntry.validate(value);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("invalid config value for " + registeredEntry.id() + ": " + String.join("; ", errors));
            }
            overrides.put(registeredEntry.id(), value);
            return this;
        }

        public Builder clear(PiConfigEntry<?> entry) {
            overrides.remove(requireRegistered(entry).id());
            return this;
        }

        public PiConfigValues build() {
            return new PiConfigValues(spec, overrides);
        }

        private <T> PiConfigEntry<T> requireRegistered(PiConfigEntry<T> entry) {
            Objects.requireNonNull(entry, "entry");
            PiConfigEntry<?> registered = spec.find(entry.id())
                    .orElseThrow(() -> new IllegalArgumentException("config entry does not belong to spec: " + entry.id()));
            if (registered != entry && !registered.equals(entry)) {
                throw new IllegalArgumentException("config entry id is registered with a different definition: " + entry.id());
            }
            return entry;
        }
    }
}
