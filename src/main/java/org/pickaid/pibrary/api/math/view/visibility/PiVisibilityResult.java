package org.pickaid.pibrary.api.math.view.visibility;

/**
 * Reusable visibility outcomes for projected results.
 *
 * <p>Use these values to drive small policy decisions after projection, such
 * as draw now, clamp to edge, skip entirely, or treat as a special behind-the-
 * camera case.</p>
 */
public enum PiVisibilityResult {
    /**
     * The projected result is visible on screen.
     */
    VISIBLE,

    /**
     * The result projected successfully but lies outside the viewport.
     *
     * <p>This commonly means an arrow or edge-clamped marker could still be
     * shown, even though the direct point is outside the window.</p>
     */
    OFFSCREEN,

    /**
     * The point is not in front of the camera and therefore cannot be shown.
     *
     * <p>This is usually handled differently from a merely offscreen result,
     * because the target is behind the player rather than just outside the
     * visible rectangle.</p>
     */
    BEHIND_CAMERA,

    /**
     * The value could not produce a usable projected result.
     *
     * <p>This is mainly the bounds-oriented case where there was not enough
     * valid projected data to build a screen rectangle.</p>
     */
    NOT_PROJECTABLE
}
