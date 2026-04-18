package org.pickaid.pibrary.runtime.presentation;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.runtime.core.PiWeakIdentityStore;

/**
 * Host-scoped contribution and projection cache store.
 */
public final class PiPresentationCaches {
    private static final PiWeakIdentityStore<PiPresentationCache> CACHES = new PiWeakIdentityStore<>();

    private PiPresentationCaches() {
    }

    /**
     * Returns host metadata used by invalidation policies and world-render accessors.
     *
     * @param host presentation host
     * @return host metadata
     */
    public static HostMetadata contribution(PiPresentationHost host) {
        Objects.requireNonNull(host, "host");
        return HostMetadata.from(PiPresentationResolver.collect(host));
    }

    /**
     * Returns the host projection cache, creating it lazily.
     *
     * @param host presentation host
     * @return host projection cache
     */
    public static PiPresentationCache cache(PiPresentationHost host) {
        Objects.requireNonNull(host, "host");
        return CACHES.computeIfAbsent(host, ignored -> new PiPresentationCache());
    }

    /**
     * Returns whether a host already has a projection cache.
     *
     * @param host presentation host
     * @return {@code true} when a cache exists
     */
    public static boolean hasCache(PiPresentationHost host) {
        Objects.requireNonNull(host, "host");
        return CACHES.containsKey(host);
    }

    /**
     * Invalidates all cached projections matching the given type across surfaces.
     *
     * @param host presentation host
     * @param type projection type
     */
    public static void invalidate(PiPresentationHost host, Class<?> type) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(type, "type");
        PiPresentationCache cache = CACHES.get(host);
        if (cache != null) {
            cache.invalidate(type);
        }
    }

    /**
     * Invalidates all cached projections on one surface.
     *
     * @param host presentation host
     * @param surface projection surface
     */
    public static void invalidate(PiPresentationHost host, PiPresentationSurface surface) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(surface, "surface");
        PiPresentationCache cache = CACHES.get(host);
        if (cache != null) {
            cache.invalidate(surface);
        }
    }

    /**
     * Invalidates only projection types marked by a policy refresh map.
     *
     * @param host presentation host
     * @param refreshes policy refresh map
     */
    public static void invalidateMarked(
            PiPresentationHost host,
            Map<PiPresentationSurface, Set<Class<?>>> refreshes
    ) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(refreshes, "refreshes");
        PiPresentationCache cache = CACHES.get(host);
        if (cache != null) {
            cache.invalidate(refreshes);
        }
    }

    /**
     * Invalidates cached projections marked for client-apply refresh without
     * forcing a host contribution when no cache exists yet.
     *
     * @param host presentation host
     */
    public static void invalidateClientApply(PiPresentationHost host) {
        invalidateMarkedIfCached(host, PiPresentationContribution::clientApplyRefreshes);
    }

    /**
     * Invalidates cached projections marked for client-tick refresh without
     * forcing a host contribution when no cache exists yet.
     *
     * @param host presentation host
     */
    public static void invalidateClientTick(PiPresentationHost host) {
        invalidateMarkedIfCached(host, PiPresentationContribution::clientTickRefreshes);
    }

    /**
     * Invalidates cached projections marked for menu-data refresh without
     * forcing a host contribution when no cache exists yet.
     *
     * @param host presentation host
     */
    public static void invalidateMenuData(PiPresentationHost host) {
        invalidateMarkedIfCached(host, PiPresentationContribution::menuDataRefreshes);
    }

    /**
     * Invalidates all cached projections for a host.
     *
     * @param host presentation host
     */
    public static void invalidateAll(PiPresentationHost host) {
        Objects.requireNonNull(host, "host");
        PiPresentationCache cache = CACHES.get(host);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    private static void invalidateMarkedIfCached(
            PiPresentationHost host,
            Function<PiPresentationContribution, Map<PiPresentationSurface, Set<Class<?>>>> refreshSelector
    ) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(refreshSelector, "refreshSelector");
        PiPresentationCache cache = CACHES.get(host);
        if (cache == null) {
            return;
        }
        cache.invalidate(refreshSelector.apply(PiPresentationResolver.collect(host)));
    }

    /**
     * Factory-free host metadata derived from current host contribution output.
     *
     * @param clientApplyRefreshes client-apply refresh types by surface
     * @param clientTickRefreshes client-tick refresh types by surface
     * @param menuDataRefreshes menu-data refresh types by surface
     * @param worldRenderDistance world-render distance in blocks
     * @param globalRenderer global renderer flag
     */
    public record HostMetadata(
            Map<PiPresentationSurface, Set<Class<?>>> clientApplyRefreshes,
            Map<PiPresentationSurface, Set<Class<?>>> clientTickRefreshes,
            Map<PiPresentationSurface, Set<Class<?>>> menuDataRefreshes,
            double worldRenderDistance,
            boolean globalRenderer
    ) {
        private static HostMetadata from(PiPresentationContribution contribution) {
            return new HostMetadata(
                    contribution.clientApplyRefreshes(),
                    contribution.clientTickRefreshes(),
                    contribution.menuDataRefreshes(),
                    contribution.worldRenderDistance(),
                    contribution.globalRenderer()
            );
        }
    }
}
