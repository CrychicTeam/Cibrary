package org.pickaid.pibrary.api.math;

import net.minecraft.world.phys.Vec3;

/**
 * Result of projecting a world point into a viewport.
 *
 * @param world original world position
 * @param screenX projected pixel x
 * @param screenY projected pixel y
 * @param ndcX normalized device x, where {@code -1} is left and {@code 1} is right
 * @param ndcY normalized device y, where {@code -1} is bottom and {@code 1} is top
 * @param depth signed distance along camera forward
 * @param inFront whether the point is in front of the camera near plane
 * @param insideViewport whether the point is in front and inside the viewport
 */
public record PiProjectedPoint(
        Vec3 world,
        double screenX,
        double screenY,
        double ndcX,
        double ndcY,
        double depth,
        boolean inFront,
        boolean insideViewport
) {
    /**
     * Returns a viewport-clamped screen point.
     *
     * <p>This is the common HUD marker behavior: in-view points stay where they
     * are, off-screen points are attached to the nearest edge, and behind-camera
     * points can be pinned to the top edge so players understand the target is
     * outside their current view direction.</p>
     *
     * @param viewport viewport bounds
     * @param margin edge margin in pixels
     * @param behindAtTop when true, behind-camera points use the top edge
     * @return clamped pixel point
     */
    public ScreenPoint edgeClamped(PiViewport viewport, double margin, boolean behindAtTop) {
        if (margin < 0.0D) {
            throw new IllegalArgumentException("margin must be >= 0");
        }
        double x = viewport.clampX(screenX, margin);
        double y = viewport.clampY(screenY, margin);
        if (!inFront && behindAtTop) {
            y = margin;
        }
        return new ScreenPoint(x, y);
    }

    /**
     * Pixel point used by UI and HUD code.
     *
     * @param x pixel x
     * @param y pixel y
     */
    public record ScreenPoint(double x, double y) {
    }
}
