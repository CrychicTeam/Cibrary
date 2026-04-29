package org.pickaid.pibrary.api.math.view.projection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.pickaid.pibrary.api.math.geometry.PiAabbs;
import org.pickaid.pibrary.api.math.view.camera.PiCameraFrame;
import org.pickaid.pibrary.api.math.view.transform.PiSpaceTransform;
import org.pickaid.pibrary.api.math.view.transform.PiSpaceTransforms;

/**
 * Turns world-space values into screen-space results.
 *
 * <p>Use this class when some gameplay or debug value needs to answer
 * screen-related questions without immediately drawing anything. Common cases
 * are:</p>
 * <ul>
 *     <li>screen indicators for world targets;</li>
 *     <li>nameplates, markers, or debug labels that need a screen position;</li>
 *     <li>screen-space bounds for a world box before render code decides how
 *     to draw it.</li>
 * </ul>
 *
 * <p>This class gives later code the numeric projection results it needs before
 * deciding how to draw labels, markers, or overlays.</p>
 *
 * <p>The pipeline stays explicit: choose a camera frame, optionally transform a
 * local point into world space, then project.</p>
 */
public final class PiProjector {
    private PiProjector() {
    }

    /**
     * Projects one world-space point through the supplied camera frame.
     *
     * <p>Use this overload when your point is already in world space, such as a
     * block center, entity eye position, or projectile impact point.</p>
     *
     * @param frame the camera frame to project through
     * @param worldPoint the world-space point to project
     * @return the projected point result
     * @throws NullPointerException if {@code frame} or {@code worldPoint} is
     *     {@code null}
     */
    public static PiProjectedPoint projectPoint(PiCameraFrame frame, Vec3 worldPoint) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(worldPoint, "worldPoint");
        return projectPoint(frame, PiSpaceTransforms.identity(), worldPoint);
    }

    /**
     * Projects a local point after first transforming it into world space.
     *
     * <p>Use this overload when the point starts in some local coordinate
     * system, such as a contraption, system-owned local space, or another
     * translated frame.</p>
     *
     * @param frame the camera frame to project through
     * @param transform the transform that converts {@code localPoint} into world
     *     space
     * @param localPoint the point in local space
     * @return the projected point result
     * @throws NullPointerException if any argument is {@code null}
     */
    public static PiProjectedPoint projectPoint(PiCameraFrame frame, PiSpaceTransform transform, Vec3 localPoint) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(transform, "transform");
        Objects.requireNonNull(localPoint, "localPoint");

        Vec3 worldPoint = transform.toWorld(localPoint);
        Vector4f worldVector = new Vector4f((float) worldPoint.x, (float) worldPoint.y, (float) worldPoint.z, 1.0F);
        Vector4f viewVector = frame.viewMatrix().transform(new Vector4f(worldVector));
        double forwardDepth = -viewVector.z();
        if (!Double.isFinite(forwardDepth) || forwardDepth <= frame.lens().nearPlane()) {
            return PiProjectedPoint.hidden(forwardDepth);
        }

        Vector4f clipVector = frame.viewProjectionMatrix().transform(worldVector);
        if (!Float.isFinite(clipVector.x()) || !Float.isFinite(clipVector.y()) || !Float.isFinite(clipVector.z()) || !Float.isFinite(clipVector.w())
                || clipVector.w() <= 0.0F) {
            return PiProjectedPoint.hidden(forwardDepth);
        }

        double ndcX = clipVector.x() / clipVector.w();
        double ndcY = clipVector.y() / clipVector.w();
        double ndcZ = clipVector.z() / clipVector.w();
        double screenX = (ndcX * 0.5D + 0.5D) * frame.viewport().width();
        double screenY = (0.5D - ndcY * 0.5D) * frame.viewport().height();
        boolean withinClipDepth = ndcZ >= -1.0D && ndcZ <= 1.0D;
        boolean onScreen = withinClipDepth && ndcX >= -1.0D && ndcX <= 1.0D && ndcY >= -1.0D && ndcY <= 1.0D;

        return new PiProjectedPoint(screenX, screenY, forwardDepth, true, onScreen);
    }

    /**
     * Projects an {@link AABB} by sampling its corners.
     *
     * <p>Use this when a single point is not enough and you need a screen-space
     * rectangle for a world box. A common next step is drawing a label,
     * highlight, or debug frame around that rectangle.</p>
     *
     * <p>The result is projectable when at least one corner lies in front of
     * the camera. The result is visible when the resulting screen-space
     * rectangle intersects the viewport.</p>
     *
     * @param frame the camera frame to project through
     * @param box the bounds to project
     * @return the projected bounds result
     * @throws NullPointerException if {@code frame} or {@code box} is
     *     {@code null}
     */
    public static PiProjectedBounds projectBounds(PiCameraFrame frame, AABB box) {
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(box, "box");

        List<PiProjectedPoint> frontPoints = new ArrayList<>();
        for (Vec3 corner : PiAabbs.corners(box)) {
            PiProjectedPoint point = projectPoint(frame, corner);
            if (point.inFront()) {
                frontPoints.add(point);
            }
        }

        if (frontPoints.isEmpty()) {
            return PiProjectedBounds.hidden();
        }

        double minX = frontPoints.stream().mapToDouble(PiProjectedPoint::screenX).min().orElseThrow();
        double minY = frontPoints.stream().mapToDouble(PiProjectedPoint::screenY).min().orElseThrow();
        double maxX = frontPoints.stream().mapToDouble(PiProjectedPoint::screenX).max().orElseThrow();
        double maxY = frontPoints.stream().mapToDouble(PiProjectedPoint::screenY).max().orElseThrow();
        boolean intersectsViewport = maxX >= 0.0D
            && minX <= frame.viewport().width()
            && maxY >= 0.0D
            && minY <= frame.viewport().height();
        boolean withinFarPlane = frontPoints.stream().anyMatch(point -> point.depth() <= frame.lens().farPlane());
        boolean visible = withinFarPlane && intersectsViewport;

        return new PiProjectedBounds(minX, minY, maxX, maxY, true, visible);
    }
}
