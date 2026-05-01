package org.pickaid.pibrary.runtime.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.config.PiDataConfigType;

/**
 * Current values loaded for one datapack config type.
 *
 * @param <T> config value type
 */
public final class PiDataConfigBinding<T> {
    private final PiDataConfigType<T> type;
    private volatile Map<ResourceLocation, T> values = Map.of();
    private volatile T merged;
    private volatile long version;

    PiDataConfigBinding(PiDataConfigType<T> type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public PiDataConfigType<T> type() {
        return type;
    }

    public Optional<T> find(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(values.get(id));
    }

    public T getEntry(ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        return values.get(id);
    }

    public T requireEntry(ResourceLocation id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("missing datapack config "
                + type.folder() + "/" + id));
    }

    public Collection<T> getAll() {
        return values.values();
    }

    public Map<ResourceLocation, T> entries() {
        return values;
    }

    public long version() {
        return version;
    }

    public <V> PiDataConfigView<T, V> view(Function<? super Map<ResourceLocation, T>, ? extends V> factory) {
        return new PiDataConfigView<>(this, factory);
    }

    public T getMerged() {
        T cached = merged;
        if (cached == null) {
            cached = type.merge(values.values());
            merged = cached;
        }
        return cached;
    }

    void update(Map<ResourceLocation, T> loadedValues) {
        values = Map.copyOf(new LinkedHashMap<>(Objects.requireNonNull(loadedValues, "loadedValues")));
        merged = null;
        version++;
    }
}
