/**
 * Core math helpers for Pibrary.
 *
 * <p>This package keeps the Pi series on Minecraft's own {@code Vec3} and
 * {@code AABB} types instead of introducing custom vector or box replacements.
 * It provides focused helpers around those types: numeric interpolation,
 * vector basis math, AABB construction, weighted selection, curves, and generic
 * world-to-screen projection.</p>
 *
 * <p>The package is intentionally runtime-light. It does not own camera modes,
 * render backends, HUD widgets, or worldgen registries. Those systems can use
 * these helpers as shared math foundations without pulling extra engine
 * responsibilities into Pibrary core.</p>
 */
package org.pickaid.pibrary.api.math;
