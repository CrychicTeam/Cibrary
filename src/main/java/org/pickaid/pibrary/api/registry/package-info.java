/**
 * Registry declaration contracts.
 *
 * <p>This package is intentionally split from the concrete Forge registration
 * adapter. Mods can define typed registry families, emit requests, and validate
 * duplicate ids before anything reaches the mod event bus. Groups keep reports
 * readable when a plan combines materials, machines, spells, or other feature
 * groups.</p>
 *
 * <p>Keep this package for simple registry declarations and custom registry
 * entries. Complex blocks, items, entities, models, loot, recipes, and render
 * setup should stay on the normal Registrate builder chain. Runtime adapters
 * can apply the same plan to Forge 1.20.1, NeoForge 1.21+, or future branches
 * without changing the declaration shape.</p>
 */
package org.pickaid.pibrary.api.registry;
