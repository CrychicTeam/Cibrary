# Pibrary 1.20.1 Wiki

Pibrary 1.20.1 docs are split by foundation area:

- [Facet state system](facets.md)
- [PiRegistrate and creative tabs](registrate.md)
- [Config and datapack data](../../../enUS/config.md)
- [Recipe / JEI neutral contracts](../../../enUS/jei-integration.md)
- [Recipe runtime](recipe.md)
- [Projectile trace](projectile.md)
- [Tint helpers](tint.md)
- [Math tutorial](../../../enUS/math.md)
- [Text markup and image components](../../../enUS/text-markup.md)

## Code Layers

- `api/**`: stable downstream API.
- `runtime/**`: Forge 1.20.1 implementation, such as capability, sync, creative tabs, recipe cache, projectile trace.
- `mixin/**`: access points that truly need Minecraft internals.
- `src/devExample/java/**`: examples compiled for tests but excluded from production jars.
