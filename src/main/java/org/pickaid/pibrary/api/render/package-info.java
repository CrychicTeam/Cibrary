/**
 * Render-core bridge contracts that consume the Pibrary presentation core.
 *
 * <p>{@code Pibrary} owns authoritative state plus typed {@code world_render},
 * {@code hud}, and {@code screen} presentation contracts. This package keeps
 * only the thin world-space bridge pieces that still belong in core, such as
 * extracted-state helpers and minimal render contexts.</p>
 *
 * <p>Engine-level render runtime belongs to {@code PiRenderBridge} and related
 * visual packs. {@code PiUI} owns HUD and screen runtime.</p>
 */
package org.pickaid.pibrary.api.render;
