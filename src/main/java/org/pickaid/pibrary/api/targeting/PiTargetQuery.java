package org.pickaid.pibrary.api.targeting;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable targeting query with isolated toggles for line of sight, living-only
 * filtering, and whether the caster may be included.
 *
 * @param anchor anchor strategy used to resolve the query volume
 * @param range forward distance used by directional anchors
 * @param radius radius of the resolved query volume
 * @param requireLineOfSight whether block line of sight is required
 * @param livingOnly whether only living entities should match
 * @param includeCaster whether the caster can be returned by the query
 * @param anchorPoint explicit anchor point for projected and area-center modes
 * @param anchorDirection explicit direction for projected-point mode
 */
public record PiTargetQuery(
        PiTargetAnchor anchor,
        double range,
        double radius,
        boolean requireLineOfSight,
        boolean livingOnly,
        boolean includeCaster,
        @Nullable Vec3 anchorPoint,
        @Nullable Vec3 anchorDirection
) {
    public PiTargetQuery {
        Objects.requireNonNull(anchor, "anchor");
        if (range < 0.0D) {
            throw new IllegalArgumentException("range must be >= 0");
        }
        if (radius < 0.0D) {
            throw new IllegalArgumentException("radius must be >= 0");
        }

        anchorDirection = normalize(anchorDirection);
        switch (anchor) {
            case SELF, LOOK_VECTOR -> {
                if (anchorPoint != null || anchorDirection != null) {
                    throw new IllegalArgumentException(anchor + " does not accept explicit anchorPoint/anchorDirection");
                }
            }
            case PROJECTED_POINT -> {
                Objects.requireNonNull(anchorPoint, "anchorPoint");
                Objects.requireNonNull(anchorDirection, "anchorDirection");
            }
            case AREA_CENTER -> {
                Objects.requireNonNull(anchorPoint, "anchorPoint");
                if (anchorDirection != null) {
                    throw new IllegalArgumentException("AREA_CENTER does not accept anchorDirection");
                }
            }
        }
    }

    /**
     * Creates a self-centered spherical query.
     *
     * @param radius query radius
     * @return immutable query
     */
    public static PiTargetQuery self(double radius) {
        return new PiTargetQuery(PiTargetAnchor.SELF, 0.0D, radius, false, false, false, null, null);
    }

    /**
     * Creates a sweep query starting at the caster eye position and extending
     * forward along the look vector.
     *
     * @param range forward distance
     * @param radius sweep radius
     * @return immutable query
     */
    public static PiTargetQuery look(double range, double radius) {
        return new PiTargetQuery(PiTargetAnchor.LOOK_VECTOR, range, radius, false, false, false, null, null);
    }

    /**
     * Creates a query centered on an explicit point projected along a direction.
     *
     * @param anchorPoint starting point
     * @param direction projection direction
     * @param range projection distance
     * @param radius sphere radius at the projected point
     * @return immutable query
     */
    public static PiTargetQuery projectedPoint(Vec3 anchorPoint, Vec3 direction, double range, double radius) {
        return new PiTargetQuery(PiTargetAnchor.PROJECTED_POINT, range, radius, false, false, false, anchorPoint, direction);
    }

    /**
     * Creates a query centered on an explicit world-space point.
     *
     * @param center sphere center
     * @param radius sphere radius
     * @return immutable query
     */
    public static PiTargetQuery areaCenter(Vec3 center, double radius) {
        return new PiTargetQuery(PiTargetAnchor.AREA_CENTER, 0.0D, radius, false, false, false, center, null);
    }

    /**
     * Returns a copy with an updated line-of-sight requirement.
     *
     * @param value desired line-of-sight flag
     * @return copied query
     */
    public PiTargetQuery withLineOfSight(boolean value) {
        return new PiTargetQuery(anchor, range, radius, value, livingOnly, includeCaster, anchorPoint, anchorDirection);
    }

    /**
     * Returns a copy with an updated living-only flag.
     *
     * @param value desired living-only flag
     * @return copied query
     */
    public PiTargetQuery withLivingOnly(boolean value) {
        return new PiTargetQuery(anchor, range, radius, requireLineOfSight, value, includeCaster, anchorPoint, anchorDirection);
    }

    /**
     * Returns a copy with an updated include-caster flag.
     *
     * @param value desired include-caster flag
     * @return copied query
     */
    public PiTargetQuery withIncludeCaster(boolean value) {
        return new PiTargetQuery(anchor, range, radius, requireLineOfSight, livingOnly, value, anchorPoint, anchorDirection);
    }

    /**
     * Convenience helper that enables the line-of-sight requirement.
     *
     * @return copied query
     */
    public PiTargetQuery requiringLineOfSight() {
        return withLineOfSight(true);
    }

    /**
     * Convenience helper that restricts the query to living entities.
     *
     * @return copied query
     */
    public PiTargetQuery livingTargetsOnly() {
        return withLivingOnly(true);
    }

    /**
     * Convenience helper that allows the caster to be returned.
     *
     * @return copied query
     */
    public PiTargetQuery includingCaster() {
        return withIncludeCaster(true);
    }

    private static Vec3 normalize(@Nullable Vec3 direction) {
        if (direction == null) {
            return null;
        }
        if (direction.lengthSqr() < 1.0E-7D) {
            throw new IllegalArgumentException("anchorDirection must not be zero");
        }
        return direction.normalize();
    }
}
