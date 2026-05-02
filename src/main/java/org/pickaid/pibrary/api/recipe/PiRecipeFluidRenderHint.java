package org.pickaid.pibrary.api.recipe;

/**
 * Viewer hint matching JEI's fluid renderer slot options.
 *
 * @param capacity maximum fluid amount represented by this slot
 * @param showCapacity whether the capacity should be shown in the tooltip
 * @param width rendered tank width in GUI pixels
 * @param height rendered tank height in GUI pixels
 */
public record PiRecipeFluidRenderHint(long capacity, boolean showCapacity, int width, int height) {
    public PiRecipeFluidRenderHint {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("fluid renderer width and height must be > 0");
        }
    }
}
