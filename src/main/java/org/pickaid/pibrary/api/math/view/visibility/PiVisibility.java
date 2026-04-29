package org.pickaid.pibrary.api.math.view.visibility;

import java.util.Objects;
import org.pickaid.pibrary.api.math.view.projection.PiProjectedBounds;
import org.pickaid.pibrary.api.math.view.projection.PiProjectedPoint;

/**
 * Small helpers for classifying projected results.
 *
 * <p>Use this after projection when code wants one clean visibility answer
 * instead of repeating the same boolean checks. Common cases are HUD marker
 * filtering, debug overlay culling, and deciding whether to clamp an indicator
 * to the screen edge.</p>
 */
public final class PiVisibility {
    private PiVisibility() {
    }

    /**
     * Classifies one projected point.
     *
     * <p>This is the usual next step after {@code PiProjector.projectPoint(...)}
     * when later code only cares about the visibility bucket, not the raw
     * booleans.</p>
     *
     * @param point the projected point result
     * @return the visibility classification
     * @throws NullPointerException if {@code point} is {@code null}
     */
    public static PiVisibilityResult classify(PiProjectedPoint point) {
        Objects.requireNonNull(point, "point");
        if (!point.inFront()) {
            return PiVisibilityResult.BEHIND_CAMERA;
        }
        return point.onScreen() ? PiVisibilityResult.VISIBLE : PiVisibilityResult.OFFSCREEN;
    }

    /**
     * Classifies one projected bounds result.
     *
     * <p>This is the usual next step after {@code PiProjector.projectBounds(...)}
     * when later code wants a coarse visibility decision for a world-space box.</p>
     *
     * @param bounds the projected bounds result
     * @return the visibility classification
     * @throws NullPointerException if {@code bounds} is {@code null}
     */
    public static PiVisibilityResult classify(PiProjectedBounds bounds) {
        Objects.requireNonNull(bounds, "bounds");
        if (!bounds.projectable()) {
            return PiVisibilityResult.NOT_PROJECTABLE;
        }
        return bounds.visible() ? PiVisibilityResult.VISIBLE : PiVisibilityResult.OFFSCREEN;
    }
}
