# Pibrary Render-Core Bridge

`Pibrary` does not treat this package as the center of client presentation.

The stable center is the presentation core:
- host truth;
- `world_render`, `hud`, and `screen` surfaces;
- typed projection scope and local screen-session contracts;
- cache and invalidation policy driven by client apply, menu data, and explicit refresh.

This package stays narrow. It only keeps the world-space bridge pieces that still belong in `Pibrary` core:
- thin extracted render-state helpers that consume `world_render` projections;
- minimal context carriers used by core examples;
- stable host-facing bridge points that future render packs may consume.

This package does not own:
- the main render engine contract;
- backend routing or backend compatibility;
- render layers, trails, beams, or post-processing runtime;
- geo entity rendering;
- player animation runtime;
- UI visual runtime.

Those belong to:
- `PiRenderBridge`
- `PiGeoRender`
- `PiPlayerAnim`
- `PiUI` for `hud` and `screen` runtime

## Current Helpers

The current helpers stay in `Pibrary` only because they still sit on the host-truth side of the boundary:
- `PiRenderContext`
- `PiEntityRenderContext`
- `PiBlockEntityRenderer`
- `PiEntityRenderer`

Their allowed job is narrow:
- extract a render-facing state from a host object;
- pass a thin context object through to drawing code;
- stay independent from any backend, layer runtime, or visual extension registry.

## Author Shape

Block entity example:

```java
public final class CounterBlockEntityRenderer
        extends PiBlockEntityRenderer<CounterBlockEntity, CounterWorldRenderVisual> {

    @Override
    protected CounterWorldRenderVisual extract(CounterBlockEntity blockEntity, float partialTick) {
        return PiPresentations.worldRender().resolve(blockEntity, CounterWorldRenderVisual.class, partialTick);
    }

    @Override
    protected void renderState(
            CounterBlockEntity blockEntity,
            CounterWorldRenderVisual visual,
            PiRenderContext context
    ) {
        drawBillboardText(context, visual.text(), visual.color());
    }
}
```

Entity example:

```java
public final class ArcBoltRenderer
        extends PiEntityRenderer<ArcBoltEntity, ArcBoltRenderState> {

    public ArcBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected ArcBoltRenderState extract(ArcBoltEntity entity, float partialTick) {
        return ArcBoltRenderState.of(entity, partialTick);
    }

    @Override
    protected void renderState(
            ArcBoltEntity entity,
            ArcBoltRenderState state,
            PiEntityRenderContext context
    ) {
        drawBolt(state, context);
    }
}
```

## Boundary Rule

Use one question when adding a type here:

"Is this describing presentation-core truth, or is it describing how visuals are rendered?"

If it describes host/runtime truth, it may stay.

If it describes how visuals are rendered, it should move to a render-family repo.
