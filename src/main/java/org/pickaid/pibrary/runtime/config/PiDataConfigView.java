package org.pickaid.pibrary.runtime.config;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

/**
 * Cached derived view for one {@link PiDataConfigBinding}.
 *
 * <p>Use this when loaded datapack files need a lookup table, sorted list, or
 * other runtime index. The view rebuilds lazily after the binding reloads and
 * is reused between reloads.</p>
 *
 * @param <T> loaded config value type
 * @param <V> derived view type
 */
public final class PiDataConfigView<T, V> {
    private final PiDataConfigBinding<T> binding;
    private final Function<? super Map<ResourceLocation, T>, ? extends V> factory;
    private volatile long version = Long.MIN_VALUE;
    private volatile V value;

    PiDataConfigView(
            PiDataConfigBinding<T> binding,
            Function<? super Map<ResourceLocation, T>, ? extends V> factory
    ) {
        this.binding = Objects.requireNonNull(binding, "binding");
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public V get() {
        long currentVersion = binding.version();
        V cached = value;
        if (version == currentVersion && cached != null) {
            return cached;
        }
        synchronized (this) {
            if (version != currentVersion || value == null) {
                value = Objects.requireNonNull(factory.apply(binding.entries()), "view");
                version = currentVersion;
            }
            return value;
        }
    }
}
