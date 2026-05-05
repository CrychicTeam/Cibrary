# Projectile Trace

Projectile trace puts "check movement from A to B" into one request. Gameplay code supplies start, end, sweep bounds, target filtering, and hit limits; the installed tracer handles block/entity tests and graze results.

```java
PiProjectileTraceRequest request = new PiProjectileTraceRequest(
        level,
        caster,
        eyePosition,
        eyePosition.add(look.scale(24.0D)),
        new AABB(-0.15D, -0.15D, -0.15D, 0.15D, 0.15D, 0.15D),
        PiTargetQuery.look(24.0D, 0.5D).livingTargetsOnly(),
        PiProjectileCollisionMode.STOP_ON_BLOCK,
        4,
        0.25D
);

PiProjectileTraceResult result = PiProjectileTraces.trace(request);
```

Common use:

- Real entity hits run damage, knockback, fire, or custom logic.
- Graze hits are for near-miss particles, sounds, or feedback, not real hits.
- `maxEntityHits` caps how many targets one projectile can process.
- `PiTargetQuery` describes the query volume, living-only filtering, and line-of-sight basics. More specific ally/enemy checks belong in hit handling or in the project's resolver.

The Forge 1.20.1 tracer is installed in the runtime layer. Gameplay code should depend on `PiProjectileTraceRequest` and `PiProjectileTraces`, so later porting starts with the runtime implementation.
