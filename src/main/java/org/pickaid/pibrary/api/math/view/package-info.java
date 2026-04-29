/**
 * Explicit camera-to-screen math pipeline for Pibrary.
 *
 * <p>Use the {@code view} family when a world-space value needs to become a
 * screen-space answer. The practical targets are HUD markers, screen labels,
 * area highlights, and debug overlays.</p>
 *
 * <p>The package is intentionally split by the steps you actually perform in
 * code:</p>
 * <ol>
 *     <li>capture a camera snapshot from
 *     {@link org.pickaid.pibrary.api.math.view.camera};</li>
 *     <li>convert local coordinates into world space with
 *     {@link org.pickaid.pibrary.api.math.view.transform};</li>
 *     <li>project into screen coordinates with
 *     {@link org.pickaid.pibrary.api.math.view.projection};</li>
 *     <li>turn the raw result into a policy decision with
 *     {@link org.pickaid.pibrary.api.math.view.visibility};</li>
 *     <li>optionally clamp surviving targets to the screen edge with
 *     {@link org.pickaid.pibrary.api.math.view.indicator}.</li>
 * </ol>
 *
 * <p>The current design is matrix-first. A camera frame still keeps readable
 * pose and lens data, but projection flows through derived view,
 * projection, and view-projection matrices so the math follows Minecraft's
 * real render pipeline instead of a separate approximate model.</p>
*/
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.view;
