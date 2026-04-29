package org.pickaid.pibrary.api.math.view.camera;

/**
 * Pixel dimensions of a projection target.
 *
 * <p>Use this when projected values need to land in a concrete 2D area, most
 * often the game window or a render target with known pixel size. Common cases
 * are HUD markers, screen-space labels, and debug overlays.</p>
 *
 * @param width the viewport width in pixels; must be greater than {@code 0}
 * @param height the viewport height in pixels; must be greater than {@code 0}
 */
public record PiViewport(int width, int height) {
    /**
     * Creates a validated viewport.
     *
     * @param width the viewport width in pixels
     * @param height the viewport height in pixels
     * @throws IllegalArgumentException if {@code width <= 0} or
     *     {@code height <= 0}
     */
    public PiViewport {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("viewport width and height must be > 0");
        }
    }

    /**
     * Returns the width-to-height ratio of this viewport.
     *
     * <p>This is the value projection math needs when converting camera-space
     * X coordinates into normalized screen-space X coordinates.</p>
     *
     * @return {@code width / height} as a double
     */
    public double aspectRatio() {
        return this.width / (double) this.height;
    }
}
