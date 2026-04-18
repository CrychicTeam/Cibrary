package org.pickaid.pibrary.api.presentation;

import org.pickaid.pibrary.runtime.presentation.PiPresentationCaches;
import org.pickaid.pibrary.runtime.presentation.PiPresentationResolver;
import org.pickaid.pibrary.runtime.presentation.PiScreenSessionStore;

/**
 * Author-facing facade for resolving and invalidating presentation projections.
 */
public final class PiPresentations {
    private static final WorldRenderAccess WORLD_RENDER = new WorldRenderAccess();
    private static final HudAccess HUD = new HudAccess();
    private static final ScreenAccess SCREENS = new ScreenAccess();

    private PiPresentations() {
    }

    /**
     * Returns world-render presentation accessors.
     *
     * @return world-render accessors
     */
    public static WorldRenderAccess worldRender() {
        return WORLD_RENDER;
    }

    /**
     * Returns HUD presentation accessors.
     *
     * @return HUD accessors
     */
    public static HudAccess hud() {
        return HUD;
    }

    /**
     * Returns screen presentation accessors.
     *
     * @return screen accessors
     */
    public static ScreenAccess screens() {
        return SCREENS;
    }

    /**
     * Invalidates every cached projection matching the given type.
     *
     * @param host presentation host
     * @param type projection type
     */
    public static void invalidate(PiPresentationHost host, Class<?> type) {
        PiPresentationCaches.invalidate(host, type);
    }

    /**
     * Invalidates every cached projection for a host.
     *
     * @param host presentation host
     */
    public static void invalidateAll(PiPresentationHost host) {
        PiPresentationCaches.invalidateAll(host);
    }

    /**
     * Invalidates projection types marked for client-apply refresh.
     *
     * @param host presentation host
     */
    public static void invalidateClientApply(PiPresentationHost host) {
        PiPresentationCaches.invalidateClientApply(host);
    }

    /**
     * Invalidates projection types marked for client-tick refresh.
     *
     * @param host presentation host
     */
    public static void invalidateClientTick(PiPresentationHost host) {
        PiPresentationCaches.invalidateClientTick(host);
    }

    /**
     * Invalidates projection types marked for menu-data refresh.
     *
     * @param host presentation host
     */
    public static void invalidateMenuData(PiPresentationHost host) {
        PiPresentationCaches.invalidateMenuData(host);
    }

    /**
     * World-render projection accessors and metadata.
     */
    public static final class WorldRenderAccess {
        /**
         * Resolves one world-render projection by type.
         *
         * @param host presentation host
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationHost host, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(host, PiPresentationSurface.WORLD_RENDER, type, partialTick);
        }

        /**
         * Returns world-render distance metadata for a host.
         *
         * @param host presentation host
         * @return world-render distance in blocks
         */
        public double renderDistance(PiPresentationHost host) {
            return PiPresentationCaches.contribution(host).worldRenderDistance();
        }

        /**
         * Returns whether a host requests a global renderer.
         *
         * @param host presentation host
         * @return global renderer flag
         */
        public boolean globalRenderer(PiPresentationHost host) {
            return PiPresentationCaches.contribution(host).globalRenderer();
        }

        /**
         * Invalidates all world-render projections for a host.
         *
         * @param host presentation host
         */
        public void invalidate(PiPresentationHost host) {
            PiPresentationCaches.invalidate(host, PiPresentationSurface.WORLD_RENDER);
        }
    }

    /**
     * HUD projection accessors.
     */
    public static final class HudAccess {
        /**
         * Resolves one HUD projection by type.
         *
         * @param host presentation host
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationHost host, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(host, PiPresentationSurface.HUD, type, partialTick);
        }

        /**
         * Invalidates all HUD projections for a host.
         *
         * @param host presentation host
         */
        public void invalidate(PiPresentationHost host) {
            PiPresentationCaches.invalidate(host, PiPresentationSurface.HUD);
        }
    }

    /**
     * Screen projection accessors.
     */
    public static final class ScreenAccess {
        /**
         * Resolves one screen projection by type.
         *
         * @param host presentation host
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationHost host, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(host, PiPresentationSurface.SCREEN, type, partialTick);
        }

        /**
         * Resolves a client-local screen session registered by the given host.
         *
         * @param owner local screen or menu owner controlling session lifetime
         * @param host presentation host that registered the session contract
         * @param type session type
         * @param <S> session value type
         * @return stable local session object
         */
        public <S> S session(Object owner, PiPresentationHost host, Class<S> type) {
            return PiScreenSessionStore.session(owner, host, type);
        }

        /**
         * Invalidates all screen projections for a host.
         *
         * @param host presentation host
         */
        public void invalidate(PiPresentationHost host) {
            PiPresentationCaches.invalidate(host, PiPresentationSurface.SCREEN);
        }
    }
}
