# Math Tutorial

Pibrary's Math API does not replace Minecraft types. Positions stay as `Vec3`,
boxes stay as `AABB`, and directions stay as `Direction`. Pibrary only fills the
small math gaps that appear again and again in gameplay, HUD code, projectiles,
targeting, config previews, and debug tools.

The API has two layers:

- `org.pickaid.pibrary.api.math` is the common entry layer for everyday gameplay code.
- `org.pickaid.pibrary.api.math.core`, `geometry`, `curve`, `weight`, and `view` are more explicit packages for code that wants clearer responsibility boundaries.

Do not import both top-level `PiAabbs` and `geometry.PiAabbs` in the same file. The top-level `PiAabbs` has more production shape helpers; `geometry.PiAabbs` is smaller and mainly supports projection and simple bounds work.

## Which Class To Use

| Task | Class |
| --- | --- |
| clamp, lerp, remap, wrap, epsilon checks | `PiScalars` or top-level `PiMath` |
| one stable numeric range such as `0..100` | `PiRange` |
| evenly spaced sample points | `PiSamples` |
| safe normalization, projection, rotation, angles | top-level `PiVectors` |
| block centers and short ray endpoints | `geometry.PiVectors` |
| block faces, edges, touched blocks, cylinder/sphere broad-phase boxes | top-level `PiAabbs` |
| broad-phase bounds around a segment | `geometry.PiRays` |
| horizontal/vertical directions, exclude one axis | `geometry.PiDirections` |
| forward/up/right local basis | `PiOrientation` |
| animation, scale, falloff curves | `curve.PiCurves` or top-level `PiCurves` |
| weighted loot, behavior, or variant selection | `PiWeightedPool` / `PiWeightedEntry` or top-level `PiWeights` |
| world-to-screen projection | `view.camera` + `PiProjector` |
| visible, offscreen, behind-camera classification | `PiVisibility` |
| offscreen arrows and edge markers | `PiEdgeIndicators` |

## Numbers: Turn Raw Values Into Usable Values

Numeric helpers answer one question: "Is this double safe and in the unit I need?" The usual flow is normalize, clamp, then map into a UI or gameplay range.

```java
PiRange energy = new PiRange(0.0D, maxEnergy);

double energy01 = energy.normalizeClamped(currentEnergy);
double barWidth = PiScalars.lerp(0.0D, 182.0D, energy01);
double alpha = PiScalars.clamp01(PiScalars.remap(currentEnergy, 0.0D, maxEnergy, 0.25D, 1.0D));
```

`normalize(...)` does not clamp. Use it when underflow or overflow matters.
Use `normalizeClamped(...)` for UI bars, alpha, and progress values.

Use wrap helpers for angles and cyclic timers:

```java
double wrappedYaw = PiScalars.wrap(rawYaw, -180.0D, 180.0D);
double phase = PiScalars.positiveModulo(gameTime + partialTick, 20.0D) / 20.0D;
```

Do not compare calculated doubles with `==`:

```java
if (PiScalars.epsilonEquals(progress, 1.0D, 1.0E-4D)) {
    finish();
}
```

## Vec3: Do Not Create Another Vector Type

Pibrary keeps vanilla `Vec3`. Safe normalization is useful in targeting,
projectile, and AI direction code. Choose a fallback that makes sense for the
current action.

```java
Vec3 origin = player.getEyePosition();
Vec3 rawDirection = target.subtract(origin);
Vec3 direction = PiVectors.safeNormalize(rawDirection, player.getLookAngle());

Vec3 end = origin.add(direction.scale(24.0D));
```

For local "forward, right, up" coordinates, create a `PiOrientation` first:

```java
PiOrientation aim = PiOrientation.fromForward(player.getLookAngle());

Vec3 center = player.position()
        .add(aim.offset(0.0D, 1.2D, 5.0D));
Vec3 leftWing = center.add(aim.offset(-2.0D, 0.0D, 0.0D));
Vec3 rightWing = center.add(aim.offset(2.0D, 0.0D, 0.0D));
```

This is safer than rewriting cross products at every call site. `PiOrientation`
keeps a stable `forward/up/right` basis.

## AABB: Search, Preview, And Broad-Phase Collision

Use the top-level `PiAabbs` for ordinary boxes. It covers centered boxes,
volume, union, scale, faces, edges, touched blocks, and broad-phase round areas.

```java
AABB search = PiAabbs.centered(targetCenter, 6.0D, 3.0D, 6.0D);
List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, search);
```

For block-local shapes or previews, use `face(...)` and `unitEdges(...)`:

```java
AABB northPlate = PiAabbs.face(Direction.NORTH, 0.125D);
List<AABB> outline = PiAabbs.unitEdges(0.0625D);
```

When iterating blocks touched by a box, remember that `forEachTouchedBlock(...)`
reuses one mutable position. Call `immutable()` if you store it.

```java
PiAabbs.forEachTouchedBlock(search, pos -> {
    BlockPos stored = pos.immutable();
    inspectBlock(stored);
});
```

Cylinder and sphere helpers are broad-phase helpers, not exact round collision.
Use them to find candidates, then run a distance or angle check.

```java
for (AABB box : PiAabbs.approximateVerticalCylinder(player.position(), 5.0D, 2.0D)) {
    for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box)) {
        if (candidate.distanceTo(player) <= 5.0F) {
            applyAura(candidate);
        }
    }
}
```

For segment work, build bounds around the segment first:

```java
Vec3 from = player.getEyePosition();
Vec3 to = from.add(player.getLookAngle().scale(32.0D));
AABB broadPhase = PiRays.segmentBounds(from, to, 0.5D);
```

## Curve: Give Numeric Changes A Shape

A curve is `double -> double`. Use it for charge, cooldown, falloff, HUD motion,
alpha, scale, and debug previews. The examples below use
`org.pickaid.pibrary.api.math.curve.PiCurves`.

```java
PiCurve chargeCurve = PiCurves.easeInOutCubic();

double rawCharge01 = chargeRange.normalizeClamped(chargeTicks);
double shapedCharge = chargeCurve.sample(rawCharge01);
double damage = PiScalars.lerp(2.0D, 18.0D, shapedCharge);
```

Use a piecewise curve when one curve is not enough:

```java
PiPiecewiseCurve curve = PiPiecewiseCurve.of(List.of(
        new PiPiecewiseCurve.Segment(new PiRange(0.0D, 0.5D), PiCurves.linear()),
        new PiPiecewiseCurve.Segment(new PiRange(0.5D, 1.0D), PiCurves.easeOutQuad())
));
```

Turn a curve into a preview table when you need to inspect the shape:

```java
List<Double> preview = PiCurveSamples.sample(PiCurves.smoothstep(), 9);
```

## Weight: Testable Weighted Selection

`PiWeightedPool` is for weighted selection that can be tested. Gameplay code can
pass a `RandomSource`; tests can pass a fixed `0..1` value.

```java
PiWeightedPool<ResourceLocation> pool = PiWeightedPool.of(
        PiWeightedEntry.of(id("common_spell"), 8.0D),
        PiWeightedEntry.of(id("rare_spell"), 2.0D),
        PiWeightedEntry.of(id("legendary_spell"), 0.5D)
);

Optional<ResourceLocation> picked = pool.select(level.random);
```

Tests do not need to guess RNG output:

```java
assertEquals(id("rare_spell"), pool.select(0.82D).orElseThrow());
```

If you already have a list and a weight function, top-level `PiWeights.choose(...)` is shorter:

```java
Optional<SpellType> spell = PiWeights.choose(spells, SpellType::weight, random);
```

## Camera: Where The Values Come From

In normal client rendering, camera values come from Minecraft's camera, FOV, and
window size. Pibrary does not own the camera; it turns one captured camera moment
into reusable math data.

```java
Minecraft minecraft = Minecraft.getInstance();
Camera camera = minecraft.gameRenderer.getMainCamera();

PiCameraFrame frame = PiCameraFrame.gameplayView(
        camera.getPosition(),
        camera.getLookVector(),
        camera.getUpVector(),
        minecraft.options.fov().get(),
        minecraft.gameRenderer.getDepthFar(),
        minecraft.getWindow().getGuiScaledWidth(),
        minecraft.getWindow().getGuiScaledHeight()
);
```

If a render event already gives you real matrices, use `PiCameraFrame.fromMatrices(...)` to preserve them. During a Minecraft version migration, this usually limits the change to camera/matrix capture code. Projection, visibility, and edge markers can stay the same.

## World Coordinates To Screen Coordinates

The projection flow is: create a `PiCameraFrame`, project a world point with
`PiProjector`, then decide whether to draw in place, clamp to the edge, or skip.

```java
Vec3 targetPoint = entity.position().add(0.0D, entity.getBbHeight() + 0.5D, 0.0D);
PiProjectedPoint projected = PiProjector.projectPoint(frame, targetPoint);

switch (PiVisibility.classify(projected)) {
    case VISIBLE -> drawMarker(projected.screenX(), projected.screenY());
    case OFFSCREEN, BEHIND_CAMERA -> PiEdgeIndicators
            .fromWorldPoint(frame, targetPoint, 12.0D)
            .ifPresent(edge -> drawArrow(edge.screenX(), edge.screenY(), edge.angleRadians()));
    case NOT_PROJECTABLE -> {
    }
}
```

`OFFSCREEN` means the target is in front of the camera but outside the window.
`BEHIND_CAMERA` means the target is behind the player. When a point is directly
behind the camera and has no left/right information, `PiEdgeIndicators` places
the marker on the top edge.

World boxes can be projected too:

```java
PiProjectedBounds bounds = PiProjector.projectBounds(frame, entity.getBoundingBox());

if (PiVisibility.classify(bounds) == PiVisibilityResult.VISIBLE) {
    drawRect(bounds.minX(), bounds.minY(), bounds.maxX(), bounds.maxY());
}
```

## Local Space: Transform Before Projection

If a point starts in local space relative to an entity, block, or system origin,
use `PiSpaceTransform`:

```java
PiSpaceTransform transform = PiSpaceTransforms
        .offset(Vec3.atCenterOf(blockEntity.getBlockPos()));

PiProjectedPoint point = PiProjector.projectPoint(
        frame,
        transform,
        new Vec3(0.5D, 1.0D, 0.5D)
);
```

Transforms can be chained:

```java
PiSpaceTransform localToWorld = localToEntity.then(entityToWorld);
```

## What To Keep Stable During Migration

The Math API keeps unstable version details near the edge:

- Vanilla `Vec3`, `AABB`, and `Direction` remain the data types.
- Camera capture stays in `PiCameraFrame.gameplayView(...)` or your own `PiCameraSource`.
- Render code consumes `PiProjectedPoint`, `PiProjectedBounds`, and `PiEdgeIndicator`.
- Gameplay code consumes scalar, direction, and bounds helpers without touching matrix details.

When upgrading Minecraft, update camera getters, FOV getters, or matrix capture first. Do not scatter version-specific code across every HUD marker, targeting class, or projectile class.
