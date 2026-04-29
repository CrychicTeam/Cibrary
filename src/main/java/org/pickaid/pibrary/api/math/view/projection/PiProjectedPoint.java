package org.pickaid.pibrary.api.math.view.projection;

/**
 * The screen-space result of projecting one point.
 *
 * <p>Use this record as the answer to questions like "where should this label
 * be drawn?" or "is this target still on screen?" It stores both the pixel
 * coordinates and the basic visibility facts that later code usually needs
 * immediately.</p>
 *
 * @param screenX the horizontal screen coordinate in pixels
 * @param screenY the vertical screen coordinate in pixels
 * @param depth the forward distance in camera space
 * @param inFront whether the point lies in front of the camera's near-plane
 *     test
 * @param onScreen whether the projected point lies inside the viewport
 */
public record PiProjectedPoint(double screenX, double screenY, double depth, boolean inFront, boolean onScreen) {
    /**
     * Creates a hidden point result with no usable screen coordinates.
     *
     * <p>This is the standard result for points that fail the "in front of the
     * camera" check. Callers can inspect {@link #depth()} and visibility flags
     * without accidentally drawing from fake coordinates.</p>
     *
     * @param depth the computed depth value
     * @return a hidden point result
     */
    public static PiProjectedPoint hidden(double depth) {
        return new PiProjectedPoint(Double.NaN, Double.NaN, depth, false, false);
    }
}
