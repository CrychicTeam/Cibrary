/**
 * Explicit camera state for Pibrary view math.
 *
 * <p>Use this package when projection code should depend on a clean camera
 * snapshot instead of reaching into Minecraft globals at every call site. The
 * package models the parts that world-to-screen math actually needs: pose,
 * lens settings, viewport size, and a way to capture one coherent frame for
 * the current render moment.</p>
 *
 * <p>For normal gameplay HUD math, the shortest practical path is
 * {@link org.pickaid.pibrary.api.math.view.camera.PiCameraFrame#gameplayView(
 * net.minecraft.world.phys.Vec3,
 * org.joml.Vector3fc,
 * org.joml.Vector3fc,
 * double,
 * double,
 * int,
 * int)}.</p>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.view.camera;
