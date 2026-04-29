package org.pickaid.pibrary.api.math;

/**
 * Pixel viewport for projection helpers.
 *
 * @param width viewport width in pixels
 * @param height viewport height in pixels
 */
public record PiViewport(int width, int height) {
    public PiViewport {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("viewport width and height must be > 0");
        }
    }

    public double aspect() {
        return (double) width / (double) height;
    }

    public double centerX() {
        return width * 0.5D;
    }

    public double centerY() {
        return height * 0.5D;
    }

    public double clampX(double x, double margin) {
        return PiMath.clamp(x, margin, width - margin);
    }

    public double clampY(double y, double margin) {
        return PiMath.clamp(y, margin, height - margin);
    }
}
