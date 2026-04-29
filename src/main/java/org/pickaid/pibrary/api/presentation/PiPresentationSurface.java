package org.pickaid.pibrary.api.presentation;

/**
 * Surface where a projection is consumed.
 */
public enum PiPresentationSurface {
    /**
     * World-space rendering consumers such as block entity and entity renderers.
     */
    WORLD_RENDER,

    /**
     * Owner-local HUD and overlay consumers.
     */
    HUD,

    /**
     * Screen and menu-bound consumers.
     */
    SCREEN
}
