package org.pickaid.pibrary.api.math.view.transform;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * A transform from one spatial frame into another.
 *
 * <p>Use this when your value does not start in world space yet. Common cases
 * are:</p>
 * <ul>
 *     <li>a point defined relative to an entity, block entity, or system
 *     anchor;</li>
 *     <li>a local debug shape that must be translated into world space before
 *     projection;</li>
 *     <li>a later chain where one transform converts local coordinates and
 *     another applies an extra offset.</li>
 * </ul>
 *
 * <p>Phase 1 keeps the contract deliberately small: one forward mapping plus
 * one composition helper, so transform chains stay explicit and readable.</p>
 */
@FunctionalInterface
public interface PiSpaceTransform {
    /**
     * Transforms a point into the next space.
     *
     * <p>In the current projection pipeline the "next space" is usually world
     * space, but callers can also use this contract in intermediate transform
     * chains before a final world conversion.</p>
     *
     * @param point the point to transform
     * @return the transformed point
     */
    Vec3 toWorld(Vec3 point);

    /**
     * Composes this transform with another transform that should run after it.
     *
     * <p>If {@code a.then(b)} is called, a point passes through {@code a}
     * first and {@code b} second. That is useful when one step converts from a
     * local frame and the next step applies a world-space offset or correction.</p>
     *
     * @param next the next transform in the chain
     * @return the composed transform
     * @throws NullPointerException if {@code next} is {@code null}
     */
    default PiSpaceTransform then(PiSpaceTransform next) {
        Objects.requireNonNull(next, "next");
        return point -> next.toWorld(this.toWorld(point));
    }
}
