# Runtime Contracts

Pibrary provides the stable vocabulary used by the Pi runtime modules:

- `api.signal`: typed signal frames, sources, targets, scopes, priorities, budgets, and trace descriptors.
- `api.ref`: typed references for cross-module ids.
- `api.generated`: marker annotations for generated internal access classes.
- `api.screen`: screen point, rect, hit, input, intent, and slot reference primitives.

Pibrary does not dispatch signals, run gameplay effects, own input routing, or render UI widgets. PiEngine and PiEffectReaction own runtime signal dispatch, PiKey owns input routing, and PiUI owns panels, layout, and motion.

Generated access classes are for internal library consumption. Application code should use registry constants, typed refs, or public DSLs instead of depending on generated helper class names.
