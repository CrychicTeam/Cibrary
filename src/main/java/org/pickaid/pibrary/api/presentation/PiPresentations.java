package org.pickaid.pibrary.api.presentation;

import org.pickaid.pibrary.runtime.presentation.PiPresentationCaches;
import org.pickaid.pibrary.runtime.presentation.PiPresentationResolver;
import org.pickaid.pibrary.runtime.presentation.PiScreenSessions;

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
     * @param source presentation source
     * @param type projection type
     */
    public static void invalidate(PiPresentationSource source, Class<?> type) {
        PiPresentationCaches.invalidate(source, type);
    }

    /**
     * Invalidates every cached projection for a source.
     *
     * @param source presentation source
     */
    public static void invalidateAll(PiPresentationSource source) {
        PiPresentationCaches.invalidateAll(source);
    }

    /**
     * Invalidates projection types marked for client-apply refresh.
     *
     * @param source presentation source
     */
    public static void invalidateClientApply(PiPresentationSource source) {
        PiPresentationCaches.invalidateClientApply(source);
    }

    /**
     * Invalidates projection types marked for client-tick refresh.
     *
     * @param source presentation source
     */
    public static void invalidateClientTick(PiPresentationSource source) {
        PiPresentationCaches.invalidateClientTick(source);
    }

    /**
     * Invalidates projection types marked for menu-data refresh.
     *
     * @param source presentation source
     */
    public static void invalidateMenuData(PiPresentationSource source) {
        PiPresentationCaches.invalidateMenuData(source);
    }

    /**
     * World-render projection accessors and metadata.
     */
    public static final class WorldRenderAccess {
        /**
         * Resolves one world-render projection by type.
         *
         * @param source presentation source
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationSource source, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(source, PiPresentationSurface.WORLD_RENDER, type, partialTick);
        }

        /**
         * Returns world-render distance metadata for a source.
         *
         * @param source presentation source
         * @return world-render distance in blocks
         */
        public double renderDistance(PiPresentationSource source) {
            return PiPresentationCaches.contribution(source).worldRenderDistance();
        }

        /**
         * Returns whether a source requests a global renderer.
         *
         * @param source presentation source
         * @return global renderer flag
         */
        public boolean globalRenderer(PiPresentationSource source) {
            return PiPresentationCaches.contribution(source).globalRenderer();
        }

        /**
         * Invalidates all world-render projections for a source.
         *
         * @param source presentation source
         */
        public void invalidate(PiPresentationSource source) {
            PiPresentationCaches.invalidate(source, PiPresentationSurface.WORLD_RENDER);
        }
    }

    /**
     * HUD projection accessors.
     */
    public static final class HudAccess {
        /**
         * Resolves one HUD projection by type.
         *
         * @param source presentation source
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationSource source, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(source, PiPresentationSurface.HUD, type, partialTick);
        }

        /**
         * Invalidates all HUD projections for a source.
         *
         * @param source presentation source
         */
        public void invalidate(PiPresentationSource source) {
            PiPresentationCaches.invalidate(source, PiPresentationSurface.HUD);
        }
    }

    /**
     * Screen projection accessors.
     */
    public static final class ScreenAccess {
        /**
         * Resolves one screen projection by type.
         *
         * @param source presentation source
         * @param type projection type
         * @param partialTick partial tick value
         * @param <V> projection value type
         * @return resolved cached projection snapshot
         */
        public <V> V resolve(PiPresentationSource source, Class<V> type, float partialTick) {
            return PiPresentationResolver.resolve(source, PiPresentationSurface.SCREEN, type, partialTick);
        }

        /**
         * Resolves a client-local screen session registered by the given source.
         *
         * @param owner local screen or menu owner controlling session lifetime
         * @param source presentation source that registered the session contract
         * @param type session type
         * @param <S> session value type
         * @return stable local session object
         */
        public <S> S session(Object owner, PiPresentationSource source, Class<S> type) {
            return PiScreenSessions.session(owner, source, type);
        }

        /**
         * Invalidates all screen projections for a source.
         *
         * @param source presentation source
         */
        public void invalidate(PiPresentationSource source) {
            PiPresentationCaches.invalidate(source, PiPresentationSurface.SCREEN);
        }
    }
}
