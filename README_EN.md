# Pibrary

[中文](README.MD)

`Pibrary` is the root foundation mod for the PickAID ecosystem. It owns the common stable APIs downstream projects repeatedly need: Facet state attachment, registry helpers, entity and projectile utilities, math tools, config entry points, diagnostics, recipe-viewer neutral contracts, and small render/tint helpers.

Current mainline documentation:

- [1.20.1 Chinese Wiki](docs/wiki/1.20.1/zhCN/README.md)
- [1.20.1 English Wiki](docs/wiki/1.20.1/enUS/README.md)

## Position

- Stable root dependency for Pi projects.
- Short, stable, migration-friendly APIs for downstream mods.
- Higher-level entry points over PiSerializeKit and PiNet.
- Compatibility boundaries for Minecraft/Forge APIs that tend to move between versions.

The README is intentionally short. Facets, Registrate helpers, config, JEI, math, text, recipe, projectile, and tint usage live in the versioned wiki.

## Credits

Pibrary's config and Registrate-facing design learns from lcy's L2 series code, especially L2Core and L2Hostility. The implementation here is Pibrary's own, but several design directions come from studying L2: keeping registry entries and default datapack config close together, using config types to manage groups of JSON files, and collecting datagen output through a unified writer.

## Maven

If the project uses the Pi template, add the dependency in `project.toml`:

```toml
[dependencies.deobf_implementation]
pibrary = { notation = "com.mihono.pickaid:pibrary:<version>", transitive = false }

[dependencies.jarjar]
pibrary = { notation = "com.mihono.pickaid:pibrary:<version>", range = "[<version>,0.1.0)", transitive = false }
```
