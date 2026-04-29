/**
 * Edge-clamped marker helpers for projected world points.
 *
 * <p>Use this package after projection when leaving the viewport should not
 * mean "stop showing the target". Typical cases are objective arrows, tracked
 * target markers, and other HUD elements that should stick to the nearest edge
 * instead of vanishing outright. A point directly behind the camera falls back
 * to the top edge when no better direction is available.</p>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.view.indicator;
