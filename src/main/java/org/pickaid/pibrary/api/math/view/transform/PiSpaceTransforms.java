package org.pickaid.pibrary.api.math.view.transform;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * Small factories for common space transforms.
 *
 * <p>Use these helpers when a full custom transform implementation would be
 * overkill. Phase 1 starts with the two cases that show up immediately in real
 * overlay and projection work: "do nothing" and "shift every point by this
 * vector".</p>
 */
public final class PiSpaceTransforms {
    private PiSpaceTransforms() {
    }

    /**
     * Returns a transform that leaves points unchanged.
     *
     * <p>Use this when your points are already in world space but the calling
     * API still accepts a transform.</p>
     *
     * @return the identity transform
     */
    public static PiSpaceTransform identity() {
        return point -> point;
    }

    /**
     * Returns a transform that adds one offset vector to every point.
     *
     * <p>Use this for the common case where local coordinates are already
     * aligned correctly and only need a world-space anchor, such as a block
     * center, entity position, or system origin.</p>
     *
     * @param delta the offset to add
     * @return a transform that translates points by {@code delta}
     * @throws NullPointerException if {@code delta} is {@code null}
     */
    public static PiSpaceTransform offset(Vec3 delta) {
        Objects.requireNonNull(delta, "delta");
        return point -> point.add(delta);
    }
}
