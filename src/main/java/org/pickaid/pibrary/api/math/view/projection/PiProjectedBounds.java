package org.pickaid.pibrary.api.math.view.projection;

/**
 * The screen-space result of projecting a bounds volume.
 *
 * <p>Use this when one point is not enough and you need the screen rectangle
 * that covers a world-space box. Common cases are drawing a 2D highlight around
 * a block entity, placing a label above an area instead of one point, or
 * deciding whether a debug box is even worth rendering.</p>
 *
 * <p>A bounds result separates two useful ideas:</p>
 * <ul>
 *     <li>{@code projectable}: at least one sampled point could be projected in
 *     front of the camera.</li>
 *     <li>{@code visible}: the projected rectangle intersects the viewport.</li>
 * </ul>
 *
 * @param minX the minimum projected screen X coordinate
 * @param minY the minimum projected screen Y coordinate
 * @param maxX the maximum projected screen X coordinate
 * @param maxY the maximum projected screen Y coordinate
 * @param projectable whether any projected sample was available
 * @param visible whether the projected rectangle intersects the viewport
 */
public record PiProjectedBounds(
    double minX,
    double minY,
    double maxX,
    double maxY,
    boolean projectable,
    boolean visible
) {
    /**
     * Creates a hidden, non-projectable bounds result.
     *
     * <p>This is the standard answer when every sampled point ended up behind
     * the camera or otherwise unusable.</p>
     *
     * @return a hidden bounds result
     */
    public static PiProjectedBounds hidden() {
        return new PiProjectedBounds(Double.NaN, Double.NaN, Double.NaN, Double.NaN, false, false);
    }
}
