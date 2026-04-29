package org.pickaid.pibrary.api.math.view.indicator;

import java.util.Objects;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibilityResult;

/**
 * One screen-edge marker position derived from a projected world point.
 *
 * <p>Use this as the final numeric answer for "show me an indicator on the
 * window border". The record stores where the marker should sit and which way
 * it should point.</p>
 *
 * @param screenX the indicator X position in screen pixels
 * @param screenY the indicator Y position in screen pixels
 * @param angleRadians the screen-space pointing angle from the viewport center
 * @param source the visibility bucket that produced this indicator
 */
public record PiEdgeIndicator(
    double screenX,
    double screenY,
    double angleRadians,
    PiVisibilityResult source
) {
    /**
     * Creates a validated edge indicator.
     *
     * <p>Indicators are only meaningful for offscreen or behind-camera points.
     * Visible points do not need one, and non-projectable bounds are handled by
     * other view helpers.</p>
     *
     * @param screenX the indicator X position
     * @param screenY the indicator Y position
     * @param angleRadians the indicator angle
     * @param source the originating visibility classification
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws IllegalArgumentException if {@code source} is not
     *     {@link PiVisibilityResult#OFFSCREEN} or
     *     {@link PiVisibilityResult#BEHIND_CAMERA}
     */
    public PiEdgeIndicator {
        Objects.requireNonNull(source, "source");
        if (source != PiVisibilityResult.OFFSCREEN && source != PiVisibilityResult.BEHIND_CAMERA) {
            throw new IllegalArgumentException("edge indicators only support OFFSCREEN or BEHIND_CAMERA points");
        }
    }
}
