package org.pickaid.pibrary.api.math.view.indicator;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.math.view.camera.PiCameraFrame;
import org.pickaid.pibrary.api.math.view.projection.PiProjectedPoint;
import org.pickaid.pibrary.api.math.view.projection.PiProjector;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibility;
import org.pickaid.pibrary.api.math.view.visibility.PiVisibilityResult;

/**
 * Edge-clamping helpers for world markers and offscreen indicators.
 *
 * <p>Use this right after projection when a point is allowed to survive as a
 * screen-edge marker instead of simply disappearing. This is the usual layer
 * for quest markers, target arrows, and other "keep pointing at it even when
 * it leaves the window" UI.</p>
 *
 * <p>The usual control flow is:</p>
 * <pre>{@code
 * PiProjectedPoint projected = PiProjector.projectPoint(frame, worldPoint);
 * PiVisibilityResult visibility = PiVisibility.classify(projected);
 *
 * switch (visibility) {
 *     case VISIBLE -> drawInsideScreen(projected.screenX(), projected.screenY());
 *     case OFFSCREEN, BEHIND_CAMERA -> {
 *         PiEdgeIndicator edge = PiEdgeIndicators.fromWorldPoint(frame, worldPoint, 12.0D).orElseThrow();
 *         drawOnScreenEdge(edge.screenX(), edge.screenY(), edge.angleRadians());
 *     }
 *     case NOT_PROJECTABLE -> {
 *     }
 * }
 * }</pre>
 *
 * <p>When a point sits directly behind the camera and there is no left/right
 * information to preserve, this helper falls back to the top edge.</p>
 */
public final class PiEdgeIndicators {
    private static final double EPSILON = 1.0E-6D;

    private PiEdgeIndicators() {
    }

    /**
     * Produces an edge indicator for one world-space point when needed.
     *
     * <p>If the point is already visible, this method returns
     * {@link Optional#empty()}. If the point is offscreen or behind the camera,
     * it returns one clamped edge anchor with a matching screen-space angle.</p>
     *
     * @param frame the camera frame to evaluate against
     * @param worldPoint the world-space point to classify
     * @param margin the inset distance from each viewport edge
     * @return an edge indicator when the point needs one, otherwise empty
     * @throws NullPointerException if {@code frame} or {@code worldPoint} is
     *     {@code null}
     * @throws IllegalArgumentException if {@code margin} is invalid for the
     *     viewport
     */
    public static Optional<PiEdgeIndicator> fromWorldPoint(PiCameraFrame frame, Vec3 worldPoint, double margin) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(worldPoint, "worldPoint");
        validateMargin(frame, margin);

        PiProjectedPoint projected = PiProjector.projectPoint(frame, worldPoint);
        PiVisibilityResult visibility = PiVisibility.classify(projected);
        if (visibility == PiVisibilityResult.VISIBLE) {
            return Optional.empty();
        }

        Vec3 direction = direction(frame, worldPoint, projected, visibility);
        Vec3 clamped = clamp(frame, direction, margin);
        return Optional.of(new PiEdgeIndicator(clamped.x, clamped.y, Math.atan2(direction.y, direction.x), visibility));
    }

    static Vec3 clamp(PiCameraFrame frame, Vec3 direction, double margin) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(direction, "direction");
        validateMargin(frame, margin);

        double dx = direction.x;
        double dy = direction.y;
        if (!Double.isFinite(dx) || !Double.isFinite(dy)) {
            throw new IllegalArgumentException("direction must be finite");
        }
        if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
            dx = 0.0D;
            dy = 1.0D;
        }

        double centerX = frame.viewport().width() * 0.5D;
        double centerY = frame.viewport().height() * 0.5D;
        double minX = margin;
        double minY = margin;
        double maxX = frame.viewport().width() - margin;
        double maxY = frame.viewport().height() - margin;

        double scale = Double.POSITIVE_INFINITY;
        if (dx > EPSILON) {
            scale = Math.min(scale, (maxX - centerX) / dx);
        } else if (dx < -EPSILON) {
            scale = Math.min(scale, (minX - centerX) / dx);
        }
        if (dy > EPSILON) {
            scale = Math.min(scale, (maxY - centerY) / dy);
        } else if (dy < -EPSILON) {
            scale = Math.min(scale, (minY - centerY) / dy);
        }

        return new Vec3(centerX + dx * scale, centerY + dy * scale, 0.0D);
    }

    private static Vec3 direction(PiCameraFrame frame, Vec3 worldPoint, PiProjectedPoint projected, PiVisibilityResult visibility) {
        double centerX = frame.viewport().width() * 0.5D;
        double centerY = frame.viewport().height() * 0.5D;

        if (visibility == PiVisibilityResult.OFFSCREEN
            && Double.isFinite(projected.screenX())
            && Double.isFinite(projected.screenY())) {
            double dx = projected.screenX() - centerX;
            double dy = projected.screenY() - centerY;
            if (Math.abs(dx) < EPSILON) {
                dx = 0.0D;
            }
            if (Math.abs(dy) < EPSILON) {
                dy = 0.0D;
            }
            if (Math.abs(dx) >= EPSILON || Math.abs(dy) >= EPSILON) {
                return new Vec3(dx, dy, 0.0D);
            }
        }

        Vec3 offset = worldPoint.subtract(frame.pose().position());
        double dx = offset.dot(frame.pose().right());
        double dy = -offset.dot(frame.pose().up());
        if (Math.abs(dx) < EPSILON) {
            dx = 0.0D;
        }
        if (Math.abs(dy) < EPSILON) {
            dy = 0.0D;
        }
        if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
            return new Vec3(0.0D, -1.0D, 0.0D);
        }
        return new Vec3(dx, dy, 0.0D);
    }

    private static void validateMargin(PiCameraFrame frame, double margin) {
        if (!Double.isFinite(margin) || margin < 0.0D) {
            throw new IllegalArgumentException("margin must be finite and >= 0");
        }
        if (margin * 2.0D >= frame.viewport().width() || margin * 2.0D >= frame.viewport().height()) {
            throw new IllegalArgumentException("margin must leave visible interior area inside the viewport");
        }
    }
}
