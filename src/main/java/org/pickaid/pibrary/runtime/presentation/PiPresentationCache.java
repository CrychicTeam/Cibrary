package org.pickaid.pibrary.runtime.presentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.pickaid.pibrary.api.presentation.PiPresentationFactory;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;

/**
 * Per-host cache of resolved projection entries.
 */
final class PiPresentationCache {
    private final Map<CacheKey, PiPresentationEntry<?>> entries = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    <V> V resolve(
            PiPresentationSurface surface,
            Class<V> type,
            float partialTick,
            PiPresentationFactory<V> recomputeFactory
    ) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(recomputeFactory, "recomputeFactory");
        PiPresentationEntry<V> entry;
        synchronized (this) {
            CacheKey key = new CacheKey(surface, type);
            entry = (PiPresentationEntry<V>) entries.computeIfAbsent(key, ignored -> new PiPresentationEntry<>());
        }
        return entry.resolve(partialTick, recomputeFactory);
    }

    synchronized void invalidate(Class<?> type) {
        Objects.requireNonNull(type, "type");
        entries.entrySet().removeIf(entry -> entry.getKey().type() == type);
    }

    synchronized void invalidate(PiPresentationSurface surface) {
        Objects.requireNonNull(surface, "surface");
        entries.entrySet().removeIf(entry -> entry.getKey().surface() == surface);
    }

    synchronized void invalidate(PiPresentationSurface surface, Class<?> type) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        entries.remove(new CacheKey(surface, type));
    }

    synchronized void invalidate(Map<PiPresentationSurface, Set<Class<?>>> refreshes) {
        Objects.requireNonNull(refreshes, "refreshes");
        for (Map.Entry<PiPresentationSurface, Set<Class<?>>> entry : refreshes.entrySet()) {
            PiPresentationSurface surface = Objects.requireNonNull(entry.getKey(), "refreshes contains null surface");
            Set<Class<?>> types = Objects.requireNonNull(entry.getValue(), "refreshes contains null type set");
            for (Class<?> type : types) {
                invalidate(surface, Objects.requireNonNull(type, "refreshes contains null type"));
            }
        }
    }

    synchronized void invalidateAll() {
        entries.clear();
    }

    private record CacheKey(PiPresentationSurface surface, Class<?> type) {
    }
}
