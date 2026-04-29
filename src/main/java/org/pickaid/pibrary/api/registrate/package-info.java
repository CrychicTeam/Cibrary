/**
 * Thin Registrate helpers for Pibrary projects.
 *
 * <p>This package does not replace Registrate. It keeps the normal
 * {@code REGISTRATE.block(...).properties(...).transform(...).register()} chain
 * and adds reusable transforms for the parts that appear in many mods:
 * entry callbacks, datagen hooks, mining tags, simple item tags, item stack
 * sizes, common base block properties, and tint declarations.</p>
 *
 * <p>A transform should describe a small repeated operation on an existing
 * builder. If the method starts naming gameplay concepts such as machines,
 * spells, traits, or modules, it belongs in the owning mod's registrate subclass
 * instead of this package. For project-specific registries, extend
 * {@link org.pickaid.pibrary.api.registrate.PiBaseRegistrate} and put the
 * project-specific builders in the owning mod.</p>
 */
package org.pickaid.pibrary.api.registrate;
