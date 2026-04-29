package org.pickaid.pibrary.api.presentation;

import org.pickaid.pibrary.api.presentation.hud.PiHudPresentationContext;
import org.pickaid.pibrary.api.presentation.screen.PiScreenPresentationContext;
import org.pickaid.pibrary.api.presentation.world.PiWorldRenderPresentationContext;

/**
 * Surface-specific presentation contribution context.
 */
public interface PiPresentationContext {
    /**
     * Returns the world-render contribution context.
     *
     * @return world-render context
     */
    PiWorldRenderPresentationContext worldRender();

    /**
     * Returns the HUD contribution context.
     *
     * @return HUD context
     */
    PiHudPresentationContext hud();

    /**
     * Returns the screen contribution context.
     *
     * @return screen context
     */
    PiScreenPresentationContext screens();
}
