package org.pickaid.pibrary.api.math.view.camera;

/**
 * Lens-like projection settings for one camera snapshot.
 *
 * <p>Use this when world-to-screen math needs the same core values Minecraft's
 * renderer also cares about: field of view plus near and far clip distances.
 * Common cases are:</p>
 * <ul>
 *     <li>projecting a world marker into HUD space;</li>
 *     <li>building a debug overlay that should respect the current camera
 *     FOV;</li>
 *     <li>capturing a custom camera frame for a cinematic or detached viewer.</li>
 * </ul>
 *
 * <p>This record keeps the surface small by storing only the projection values
 * phase 1 math uses directly.</p>
 *
 * @param verticalFovDegrees the vertical field of view in degrees; must be
 *     greater than {@code 0} and less than {@code 180}
 * @param nearPlane the near clip distance; must be greater than {@code 0}
 * @param farPlane the far clip distance; must be greater than
 *     {@code nearPlane}
 */
public record PiCameraLens(double verticalFovDegrees, double nearPlane, double farPlane) {
    /**
     * Creates lens settings that match Minecraft's default gameplay near plane.
     *
     * <p>This is the usual shortcut when you already know the current gameplay
     * FOV and far plane, and you want a lens that behaves like the normal
     * player camera without spelling out the hard-coded near plane every time.</p>
     *
     * @param verticalFovDegrees the gameplay vertical field of view in degrees
     * @param farPlane the gameplay far clip distance
     * @return validated gameplay lens settings
     */
    public static PiCameraLens minecraftDefault(double verticalFovDegrees, double farPlane) {
        return new PiCameraLens(verticalFovDegrees, 0.05D, farPlane);
    }

    /**
     * Creates validated lens settings.
     *
     * <p>Failing early here prevents projection math from silently producing
     * nonsense later. If the captured FOV or clip planes are wrong, it is
     * better to reject the frame at construction time.</p>
     *
     * @param verticalFovDegrees the vertical field of view in degrees
     * @param nearPlane the near clip distance
     * @param farPlane the far clip distance
     * @throws IllegalArgumentException if the field of view is outside the open
     *     interval {@code (0, 180)}, if {@code nearPlane <= 0}, or if
     *     {@code farPlane <= nearPlane}
     */
    public PiCameraLens {
        if (verticalFovDegrees <= 0.0D || verticalFovDegrees >= 180.0D) {
            throw new IllegalArgumentException("verticalFovDegrees must be between 0 and 180");
        }
        if (nearPlane <= 0.0D) {
            throw new IllegalArgumentException("nearPlane must be > 0");
        }
        if (farPlane <= nearPlane) {
            throw new IllegalArgumentException("farPlane must be > nearPlane");
        }
    }
}
