# Pibrary Render-Core Bridge

The render bridge starts from the presentation core:
- authoritative state lives on the source object;
- `world_render`, `hud`, and `screen` expose typed projections;
- cache invalidation is driven by client apply, menu data, ticking, or explicit refresh.

This package keeps the small world-space helpers that are useful before a full render engine is involved:
- extracted render-state helpers that consume `world_render` projections;
- minimal render context carriers;
- stable bridge points that render packs can reuse.

## Current Helpers

- `PiRenderContext`
- `PiEntityRenderContext`
- `PiBlockEntityRenderer`
- `PiEntityRenderer`

Their job is simple:
- extract render-facing state from a source object;
- pass a thin context object through to drawing code;
- stay backend-neutral.

## Example

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
