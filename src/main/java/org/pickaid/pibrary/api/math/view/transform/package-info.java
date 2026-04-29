/**
 * Explicit space-to-space transform contracts for Pibrary view math.
 *
 * <p>Use this package when points do not begin in world space. A block-local
 * marker, an entity-relative anchor, or a system-owned local debug shape can
 * be translated explicitly before projection. Keeping that work here prevents
 * projection helpers from turning into opaque "do everything" utilities.</p>
 */
@org.jetbrains.annotations.ApiStatus.Experimental
package org.pickaid.pibrary.api.math.view.transform;
