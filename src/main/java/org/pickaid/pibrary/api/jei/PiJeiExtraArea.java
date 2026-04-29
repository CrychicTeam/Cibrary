package org.pickaid.pibrary.api.jei;

import java.util.Objects;

/**
 * GUI rectangle that should be treated as an extra occupied area by a recipe-viewer runtime.
 *
 * @param screenClassName fully qualified screen class name
 * @param x left coordinate
 * @param y top coordinate
 * @param width rectangle width
 * @param height rectangle height
 */
public record PiJeiExtraArea(
        String screenClassName,
        int x,
        int y,
        int width,
        int height
) {
    public PiJeiExtraArea {
        Objects.requireNonNull(screenClassName, "screenClassName");
        if (screenClassName.isBlank()) {
            throw new IllegalArgumentException("screenClassName must not be blank");
        }
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("x and y must be >= 0");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("width and height must be >= 1");
        }
    }
}
