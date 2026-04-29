package org.pickaid.pibrary.api.math;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * Generic camera projection frame.
 *
 * <p>This class does not own the Minecraft camera runtime. It is just math:
 * feed it a camera position, forward vector, up vector, field of view, and
 * viewport size, then it can project a world-space {@link Vec3} into screen
 * pixels. Client code can build the frame from the real Minecraft camera, while
 * tests and server-independent tools can build it from plain vectors.</p>
 */
public final class PiCameraFrame {
    private final Vec3 position;
    private final Vec3 forward;
    private final Vec3 up;
    private final Vec3 right;
    private final double verticalFovDegrees;
    private final double nearPlane;
    private final double farPlane;
    private final PiViewport viewport;

    public PiCameraFrame(
            Vec3 position,
            Vec3 forward,
            Vec3 up,
            double verticalFovDegrees,
            double nearPlane,
            double farPlane,
            PiViewport viewport
    ) {
        this.position = Objects.requireNonNull(position, "position");
        PiOrientation orientation = PiOrientation.of(forward, up);
        this.forward = orientation.forward();
        this.up = orientation.up();
        this.right = orientation.right();
        this.verticalFovDegrees = requireFov(verticalFovDegrees);
        this.nearPlane = requirePositive(nearPlane, "nearPlane");
        this.farPlane = requireFarPlane(farPlane, nearPlane);
        this.viewport = Objects.requireNonNull(viewport, "viewport");
    }

    public Vec3 position() {
        return position;
    }

    public Vec3 forward() {
        return forward;
    }

    public Vec3 up() {
        return up;
    }

    public Vec3 right() {
        return right;
    }

    public double verticalFovDegrees() {
        return verticalFovDegrees;
    }

    public double nearPlane() {
        return nearPlane;
    }

    public double farPlane() {
        return farPlane;
    }

    public PiViewport viewport() {
        return viewport;
    }

    /**
     * Projects a world-space point to viewport pixels.
     *
     * <p>The output uses the normal UI coordinate convention: x grows to the
     * right, y grows downward. A point can be projected even when it is behind
     * the camera; in that case {@link PiProjectedPoint#inFront()} is false and
     * callers should normally clamp it to an edge marker.</p>
     *
     * @param world world-space point
     * @return projection result
     */
    public PiProjectedPoint project(Vec3 world) {
        Objects.requireNonNull(world, "world");
        Vec3 delta = world.subtract(position);
        double depth = delta.dot(forward);
        double projectionDepth = Math.max(Math.abs(depth), nearPlane);
        double tanHalfFov = Math.tan(Math.toRadians(verticalFovDegrees) * 0.5D);
        double ndcX = delta.dot(right) / (projectionDepth * tanHalfFov * viewport.aspect());
        double ndcY = delta.dot(up) / (projectionDepth * tanHalfFov);
        double screenX = (ndcX + 1.0D) * 0.5D * viewport.width();
        double screenY = (1.0D - ndcY) * 0.5D * viewport.height();
        boolean inFront = depth >= nearPlane && depth <= farPlane;
        boolean inside = inFront && Math.abs(ndcX) <= 1.0D && Math.abs(ndcY) <= 1.0D;
        return new PiProjectedPoint(world, screenX, screenY, ndcX, ndcY, depth, inFront, inside);
    }

    private static double requireFov(double fov) {
        PiMath.requireFinite(fov, "verticalFovDegrees");
        if (!(fov > 0.0D && fov < 180.0D)) {
            throw new IllegalArgumentException("verticalFovDegrees must be in the exclusive range (0, 180)");
        }
        return fov;
    }

    private static double requirePositive(double value, String name) {
        PiMath.requireFinite(value, name);
        if (!(value > 0.0D)) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }

    private static double requireFarPlane(double farPlane, double nearPlane) {
        requirePositive(farPlane, "farPlane");
        if (farPlane <= nearPlane) {
            throw new IllegalArgumentException("farPlane must be greater than nearPlane");
        }
        return farPlane;
    }
}
