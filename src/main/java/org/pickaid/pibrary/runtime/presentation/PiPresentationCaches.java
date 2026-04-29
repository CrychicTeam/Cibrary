package org.pickaid.pibrary.runtime.presentation;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.runtime.core.PiWeakIdentityMap;

/**
 * Source-scoped contribution and projection caches.
 */
public final class PiPresentationCaches {
    private static final PiWeakIdentityMap<PiPresentationCache> CACHES = new PiWeakIdentityMap<>();

    private PiPresentationCaches() {
    }

    /**
     * Returns source metadata used by invalidation policies and world-render accessors.
     *
     * @param source presentation source
     * @return source metadata
     */
    public static SourceMetadata contribution(PiPresentationSource source) {
        Objects.requireNonNull(source, "source");
        return SourceMetadata.from(PiPresentationResolver.collect(source));
    }

    /**
     * Returns the source projection cache, creating it lazily.
     *
     * @param source presentation source
     * @return source projection cache
     */
    public static PiPresentationCache cache(PiPresentationSource source) {
        Objects.requireNonNull(source, "source");
        return CACHES.computeIfAbsent(source, ignored -> new PiPresentationCache());
    }

    /**
     * Returns whether a source already has a projection cache.
     *
     * @param source presentation source
     * @return {@code true} when a cache exists
     */
    public static boolean hasCache(PiPresentationSource source) {
        Objects.requireNonNull(source, "source");
        return CACHES.containsKey(source);
    }

    /**
     * Invalidates all cached projections matching the given type across surfaces.
     *
     * @param source presentation source
     * @param type projection type
     */
    public static void invalidate(PiPresentationSource source, Class<?> type) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
        PiPresentationCache cache = CACHES.get(source);
        if (cache != null) {
            cache.invalidate(type);
        }
    }

    /**
     * Invalidates all cached projections on one surface.
     *
     * @param source presentation source
     * @param surface projection surface
     */
    public static void invalidate(PiPresentationSource source, PiPresentationSurface surface) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(surface, "surface");
        PiPresentationCache cache = CACHES.get(source);
        if (cache != null) {
            cache.invalidate(surface);
        }
    }

    /**
     * Invalidates only projection types marked by a policy refresh map.
     *
     * @param source presentation source
     * @param refreshes policy refresh map
     */
    public static void invalidateMarked(
            PiPresentationSource source,
            Map<PiPresentationSurface, Set<Class<?>>> refreshes
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(refreshes, "refreshes");
        PiPresentationCache cache = CACHES.get(source);
        if (cache != null) {
            cache.invalidate(refreshes);
        }
    }

    /**
     * Invalidates cached projections marked for client-apply refresh without
     * forcing a source contribution when no cache exists yet.
     *
     * @param source presentation source
     */
    public static void invalidateClientApply(PiPresentationSource source) {
        invalidateMarkedIfCached(source, PiPresentationContribution::clientApplyRefreshes);
    }

    /**
     * Invalidates cached projections marked for client-tick refresh without
     * forcing a source contribution when no cache exists yet.
     *
     * @param source presentation source
     */
    public static void invalidateClientTick(PiPresentationSource source) {
        invalidateMarkedIfCached(source, PiPresentationContribution::clientTickRefreshes);
    }

    /**
     * Invalidates cached projections marked for menu-data refresh without
     * forcing a source contribution when no cache exists yet.
     *
     * @param source presentation source
     */
    public static void invalidateMenuData(PiPresentationSource source) {
        invalidateMarkedIfCached(source, PiPresentationContribution::menuDataRefreshes);
    }

    /**
     * Invalidates all cached projections for a source.
     *
     * @param source presentation source
     */
    public static void invalidateAll(PiPresentationSource source) {
        Objects.requireNonNull(source, "source");
        PiPresentationCache cache = CACHES.get(source);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    private static void invalidateMarkedIfCached(
            PiPresentationSource source,
            Function<PiPresentationContribution, Map<PiPresentationSurface, Set<Class<?>>>> refreshSelector
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(refreshSelector, "refreshSelector");
        PiPresentationCache cache = CACHES.get(source);
        if (cache == null) {
            return;
        }
        cache.invalidate(refreshSelector.apply(PiPresentationResolver.collect(source)));
    }

    /**
     * Factory-free source metadata derived from current source contribution output.
     *
     * @param clientApplyRefreshes client-apply refresh types by surface
     * @param clientTickRefreshes client-tick refresh types by surface
     * @param menuDataRefreshes menu-data refresh types by surface
     * @param worldRenderDistance world-render distance in blocks
     * @param globalRenderer global renderer flag
     */
    public record SourceMetadata(
            Map<PiPresentationSurface, Set<Class<?>>> clientApplyRefreshes,
            Map<PiPresentationSurface, Set<Class<?>>> clientTickRefreshes,
            Map<PiPresentationSurface, Set<Class<?>>> menuDataRefreshes,
            double worldRenderDistance,
            boolean globalRenderer
    ) {
        private static SourceMetadata from(PiPresentationContribution contribution) {
            return new SourceMetadata(
                    contribution.clientApplyRefreshes(),
                    contribution.clientTickRefreshes(),
                    contribution.menuDataRefreshes(),
                    contribution.worldRenderDistance(),
                    contribution.globalRenderer()
            );
        }
    }
}
