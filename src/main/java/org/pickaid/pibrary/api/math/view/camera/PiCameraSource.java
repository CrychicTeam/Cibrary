package org.pickaid.pibrary.api.math.view.camera;

import java.util.Objects;

/**
 * Supplies camera frames for a render moment.
 *
 * <p>Use this contract when code should depend on "something that can provide a
 * camera right now" instead of hard-coding one global camera lookup. Common
 * cases are:</p>
 * <ul>
 *     <li>the normal player camera during HUD or world-overlay rendering;</li>
 *     <li>a detached preview camera for editor-like tools;</li>
 *     <li>a scripted camera that follows an entity or authored path.</li>
 * </ul>
 *
 * <p>For the normal gameplay camera, the raw values usually come from vanilla
 * like this:</p>
 * <pre>{@code
 * Minecraft minecraft = Minecraft.getInstance();
 * Camera camera = minecraft.gameRenderer.getMainCamera();
 *
 * PiCameraFrame frame = PiCameraFrame.gameplayView(
 *     camera.getPosition(),
 *     camera.getLookVector(),
 *     camera.getUpVector(),
 *     minecraft.options.fov().get(),
 *     minecraft.gameRenderer.getDepthFar(),
 *     minecraft.getWindow().getGuiScaledWidth(),
 *     minecraft.getWindow().getGuiScaledHeight()
 * );
 * }</pre>
 */
@FunctionalInterface
public interface PiCameraSource {
    /**
     * Captures a camera frame for the given partial tick.
     *
     * <p>The returned frame should describe one coherent render moment. Callers
     * can then reuse it for several projection operations during the same pass
     * without worrying that pose and lens came from different moments.</p>
     *
     * @param partialTick the render interpolation fraction
     * @return the captured camera frame
     */
    PiCameraFrame capture(float partialTick);

    /**
     * Returns a source that always yields the same frame.
     *
     * <p>This is mainly useful for tests, preview tools, and any deterministic
     * math path where the camera should stay frozen.</p>
     *
     * @param frame the fixed frame to return
     * @return a source that always returns {@code frame}
     * @throws NullPointerException if {@code frame} is {@code null}
     */
    static PiCameraSource fixed(PiCameraFrame frame) {
        Objects.requireNonNull(frame, "frame");
        return partialTick -> frame;
    }
}
