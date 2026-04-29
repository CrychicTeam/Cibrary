/**
 * Thin Registrate helpers for Pibrary projects.
 *
 * <p>This package does not replace Registrate. Keep the normal
 * {@code REGISTRATE.block(...).properties(...).tag(...).register()} chain when
 * it is clear. Pibrary adds typed creative-tab sections, NBT variants, tint
 * declarations, id helpers, and direct builder methods such as
 * {@code blockTags(...)}, {@code itemTags(...)}, and
 * {@code blockAndItemTag(...)} for the few chain-preserving operations that
 * Registrate does not express in one step.</p>
 *
 * <p>Pibrary code should not wrap Registrate's direct methods just to make
 * everything look uniform. If a helper starts naming gameplay concepts such as
 * machines, spells, traits, or modules, it belongs in the owning mod's
 * registrate subclass instead of this package. For project-specific registries, extend
 * {@link org.pickaid.pibrary.api.registrate.PiBaseRegistrate} and put the
 * project-specific builders in the owning mod.</p>
 */
package org.pickaid.pibrary.api.registrate;
