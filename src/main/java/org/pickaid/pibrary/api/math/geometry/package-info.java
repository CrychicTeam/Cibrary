/**
 * Minecraft-shaped spatial helpers built directly on Mojang math types.
 *
 * <p>Use this package when a helper naturally speaks in {@code Vec3},
 * {@code AABB}, or {@code Direction}. The goal is not to replace Mojang's math
 * classes. The goal is to provide small, reusable operations that come up often
 * in gameplay systems such as targeting, projectile traces, and later
 * projection work.</p>
 *
 * <p>The phase 1 surface stays intentionally narrow:</p>
 * <ul>
 *     <li>{@link org.pickaid.pibrary.api.math.geometry.PiVectors} for common
 *     vector normalization and projection helpers.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.geometry.PiAabbs} for simple box
 *     construction and sampling helpers.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.geometry.PiDirections} for
 *     direction and axis-related helpers.</li>
 *     <li>{@link org.pickaid.pibrary.api.math.geometry.PiRays} for small,
 *     reusable line and segment helpers.</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * Vec3 hitPoint = PiVectors.project(origin, lookDirection, 6.0);
 * AABB queryBounds = PiRays.segmentBounds(origin, hitPoint, 0.5);
 * }</pre>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.geometry;
