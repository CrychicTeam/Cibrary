# Pibrary Presentation Core Design

## Goal

Define the long-term presentation core that belongs in `Pibrary`.

This design extends the earlier render-boundary draft. It makes one stronger decision:

`Pibrary` does not stop at thin render hooks. It owns the stable presentation contracts that connect host state, sync, invalidation, and client-facing projections across `world_render`, `hud`, and `screen` surfaces.

`Pibrary` still does not become a render engine or a UI engine.

## Decision Summary

`Pibrary` owns:
- host truth;
- schema-backed state and dirty tracking;
- sync boundaries and client-apply hooks;
- presentation contracts for `world_render`, `hud`, and `screen`;
- projection scope, cache, and invalidation policy;
- author-facing APIs that hide generated schema details.

`Pibrary` does not own:
- the main world render runtime;
- the HUD runtime;
- the screen and widget runtime;
- renderer or UI backend compatibility;
- animation, layers, shaders, trails, beams, or post-processing;
- visual effect libraries;
- rich widget libraries or layout engines.

Those belong to:
- `PiRenderBridge` and render-family packs;
- `PiUI` and UI-family packs.

## Why This Belongs In Core

The stable problem is not "how to draw a thing." The stable problem is "who owns runtime truth, how does that truth sync, and how do client consumers read it without breaking isolation."

Without a presentation core, the codebase regresses to separate ad hoc paths:
- block entities expose raw fields to renderers;
- player capabilities expose raw fields to layers or overlays;
- screens rebuild view models manually;
- refresh rules are spread across packets, menus, renderers, and screens;
- each subsystem invents its own cache and invalidation logic.

That approach is acceptable for isolated features. It is weak for a platform.

`Pibrary` should instead define one platform rule:

`host owns truth, clients consume projections`

That rule is stable across multiple host families and multiple surface families.

## Non-Goals

This design does not put these in `Pibrary`:
- a render layer framework;
- a full `BlockEntityRenderer` or `EntityRenderer` abstraction system;
- a widget toolkit;
- a screen layout DSL;
- shader management;
- animation graphs;
- backend-specific client libraries.

This design also does not replace vanilla or Forge registration. `Pibrary` defines stable contracts. Client packs still register renderers, layers, overlays, and screens through the normal platform entry points.

## Core Model

### 0. Two Axes

This design is intentionally two-dimensional.

The first axis is host family:
- `block_entity_host`;
- `entity_host`;
- `projectile_host`;
- `living_service_host`;
- `level_service_host`;
- `menu_host`.

The second axis is surface family:
- `world_render`;
- `hud`;
- `screen`.

These axes are orthogonal.

That means:
- a `BlockEntity` host may contribute projections to `world_render`, `screen`, or a future `viewer`;
- a living service host may contribute projections to `world_render`, `hud`, and `screen`;
- `world_render` is a surface name, not a synonym for `Level` or `World` host state.

This distinction is required. Without it, `BlockEntity`, `Entity`, `LivingService`, and `LevelService` appear to be mixed into one object family, which is not the intended design.

### 1. Host

A host is the runtime authority for one piece of state.

Examples:
- a `PiStateBlockEntity`;
- a `Projectile`;
- a living service attached to a player or other living entity;
- a menu that exposes a screen-facing model and owns a local session boundary.

The host owns:
- persisted state;
- sync-visible state;
- dirty tracking;
- the lifecycle points that tell the client when a projection may be stale.

The host does not own:
- renderer logic;
- overlay logic;
- widget logic.

### 2. Presentation Surface

A presentation surface is a consumer family with its own lifecycle.

This design defines three core surfaces:
- `world_render`
  - used by `BlockEntityRenderer`, `EntityRenderer`, and player `RenderLayer`;
- `hud`
  - used by overlays, crosshair-adjacent displays, bars, local status panels, and other owner-local UI;
- `screen`
  - used by menu screens and panel view models.

These surfaces share one mental model but not one lifecycle.

`world_render` cares about:
- partial tick;
- render distance;
- tracking scope;
- global renderer policy.

`hud` cares about:
- local player context;
- owner-local refresh;
- transient client timing.

`screen` cares about:
- menu lifetime;
- server-driven model state;
- client-only session state such as selected tab, expanded panel, or hovered logical mode.

### 3. Projection

A projection is a typed client-facing model derived from host truth.

Examples:
- `TrialPlateVisual`;
- `RuneMantleWorldVisual`;
- `RuneMantleHudModel`;
- `RuneForgeScreenModel`.

A projection:
- is read-only from the consumer side;
- expresses what a client surface needs to render or display;
- does not become the new authority for business state.

### 4. Scope

Every projection has a visibility scope.

The stable scopes are:
- `OWNER`;
- `TRACKING`;
- `OWNER_AND_TRACKING`;
- `LOCAL_ONLY`.

Scope is part of the core contract because it affects:
- sync fan-out;
- client cache identity;
- which consumers are allowed to resolve a projection.

### 5. Invalidation

Projection refresh must be explicit and centralized.

`Pibrary` owns the invalidation rules that connect:
- host state mutation;
- sync application on the client;
- menu data refresh;
- tick-driven transient updates;
- projection cache eviction.

This is a core responsibility because it is the point where state and presentation meet.

### 6. Session

`screen` surface needs one extra concept: local session state.

Session state is:
- client-only;
- not part of host truth;
- not synchronized unless the author sends an explicit action;
- scoped to the menu or screen instance.

Examples:
- selected tab;
- local filter mode;
- expansion and collapse state;
- transient preview toggles.

This separation is required to preserve isolation. It prevents screen-local interaction state from polluting host truth.

## Author-Facing Shape

The author should write code that declares:
- the host state;
- the projections a host contributes;
- the scope of each projection;
- the refresh policy of each projection.

The author should not need to write:
- generated schema type names in normal code paths;
- manual client cache invalidation glue;
- duplicated state extraction in renderers, overlays, and screens.

The intended author shape is:

```java
public final class RuneMantleService extends PiStatePlayerService<RuneMantleState>
        implements PiPresentationHost {

    @Override
    public void contributePresentation(PiPresentationContext context) {
        context.worldRender().snapshot(
                RuneMantleWorldVisual.class,
                PiPresentationScope.OWNER_AND_TRACKING,
                partialTick -> RuneMantleWorldVisual.from(viewState(), partialTick)
        );
        context.worldRender().refreshOnClientApply(RuneMantleWorldVisual.class);

        context.hud().snapshot(
                RuneMantleHudModel.class,
                PiPresentationScope.OWNER,
                partialTick -> RuneMantleHudModel.from(viewState(), partialTick)
        );
        context.hud().refreshOnClientApply(RuneMantleHudModel.class);
    }
}
```

The render or UI consumer then resolves a typed projection:

```java
RuneMantleWorldVisual visual =
        PiPresentations.worldRender().resolve(player, RuneMantleWorldVisual.class, partialTick);
```

or:

```java
RuneMantleHudModel model =
        PiPresentations.hud().resolve(player, RuneMantleHudModel.class, partialTick);
```

or:

```java
RuneForgeScreenModel model =
        PiPresentations.screens().resolve(menu, RuneForgeScreenModel.class, partialTick);
```

This keeps author code short without hiding ownership boundaries.

## Surface Contracts

### World Render Surface

`world_render` exists for world-space consumers.

Core responsibilities:
- typed projection registration;
- scope-aware resolution;
- render distance metadata;
- global renderer metadata;
- invalidation after client sync or host-triggered refresh.

Not in core:
- renderer inheritance trees;
- shader and pass systems;
- animation backends;
- beam, trail, and effect runtimes.

### HUD Surface

`hud` exists for local overlays and player-facing heads-up displays.

Core responsibilities:
- typed owner-local or tracking-local projection registration;
- client-apply and tick refresh policy;
- cache identity and invalidation.

Not in core:
- overlay compositing framework;
- UI theme system;
- icon libraries;
- animation or transition toolkit.

### Screen Surface

`screen` exists for menu-bound presentation.

Core responsibilities:
- typed screen model registration;
- menu-bound projection resolution;
- session model registration and access;
- refresh on client menu data changes and host sync.

Not in core:
- widget system;
- layout engine;
- theme and skinning;
- animation and interaction library.

## Cache And Invalidation Rules

`Pibrary` should maintain typed caches per host and per surface.

The cache key must include:
- surface;
- projection type;
- scope;
- host identity.

The core invalidation causes are:
- host state changed on the client;
- client sync payload applied;
- menu data updated;
- tick-based refresh policy;
- explicit author invalidation call.

The invalidation API should support:
- invalidate one projection type;
- invalidate one surface;
- invalidate all projections for one host.

This is what makes the presentation core useful in production. Without typed caches and explicit invalidation, the API remains a thin wrapper.

## Data Flow

### Block Entity Flow

`server host state changed`
-> dirty tracking updated
-> sync payload emitted
-> client payload applied
-> `afterClientStateApplied()`
-> presentation invalidation
-> `BlockEntityRenderer` resolves projection
-> renderer draws

### Living Service Flow

`server service state changed`
-> capability-backed service dirty tracking updated
-> owner and tracking sync flushed
-> client payload applied
-> service post-sync hooks run
-> presentation invalidation
-> player layer or HUD resolves projection
-> consumer draws

### Screen Flow

`server host or menu state changed`
-> menu data or sync payload updated
-> client menu state applied
-> screen projection invalidated
-> screen resolves fresh model
-> local session state remains intact unless explicitly changed

## Repo Boundary

### `Pibrary`

Owns:
- host truth;
- schema-backed state;
- dirty and route-aware sync boundaries;
- capability-backed living service attachment and lifecycle;
- presentation contracts for `world_render`, `hud`, and `screen`;
- typed projection scope, cache, and invalidation policy;
- small host-focused examples and integration tests.

Does not own:
- world render engine runtime;
- HUD overlay runtime;
- widget and screen runtime;
- animation or render effect runtimes.

### `PiRenderBridge` And Render Packs

Own:
- actual `BlockEntityRenderer` and `EntityRenderer` helpers;
- player layer helpers and composition tools;
- animation, effect, geo, trail, beam, and shader systems;
- render backend compatibility;
- world-space visual authoring libraries.

They consume `Pibrary` projections. They do not own host truth.

### `PiUI`

Owns:
- overlay runtime;
- screen runtime;
- widget libraries;
- theme, layout, animation, and interaction systems;
- rich text, rich content, image, slot, and panel authoring libraries;
- UI-side author DSLs that consume `hud` and `screen` projections.

It consumes `Pibrary` projections and session contracts. It does not redefine sync or host truth.

## Comparison

### Compared With Vanilla

Vanilla gives each subsystem its own path:
- block entities manually save and sync;
- renderers read raw host state directly;
- screens build their own local view models;
- there is no shared projection contract.

`Pibrary` presentation core is stronger because it:
- centralizes host truth;
- centralizes projection registration;
- centralizes invalidation;
- lets multiple client surfaces consume one stable model.

### Compared With L2-Style Convenience Bases

L2-style systems are strong at making one object easy to serialize and sync.

This design keeps that convenience goal but adds:
- multi-surface projection;
- typed scope and cache policy;
- host-to-HUD and host-to-screen paths;
- a stronger long-term isolation story between state, render, and UI.

The intention is not to copy L2. The intention is to exceed it in coverage while keeping cleaner package boundaries.

## Error Handling And Stability

The core must fail at the boundary, not in the consumer runtime.

That means:
- missing projection registrations should fail clearly and early in development;
- absent render or UI packs must not break host state or sync;
- local session state should never silently overwrite host truth;
- scope violations should be visible as contract errors, not hidden behavior.

## Migration Plan

### Phase 1. Establish Presentation Core Types

Add the core contracts for:
- `PiPresentationHost`;
- `PiPresentationContext`;
- `worldRender`, `hud`, and `screen` subcontexts;
- scope;
- cache and invalidation policies;
- session contracts for screens.

### Phase 2. Reframe Existing `api/render`

Reduce the current render package from a thin renderer-helper identity to a host-presentation identity.

Existing helper types may remain temporarily, but they should no longer define the center of the design.

### Phase 3. Connect Existing Host Types

Bridge:
- `PiStateBlockEntity`;
- living services;
- projectile and entity host types;
- menu-facing models.

Their client-apply hooks should feed the presentation invalidation runtime.

### Phase 4. Keep Render And UI Outside Core

Move real runtime authoring tools to:
- `PiRenderBridge` and render-family packs;
- `PiUI`.

Those repos should grow their own helper APIs on top of the core contracts.

### Phase 5. Build Production Examples

Add examples that prove:
- a block entity `world_render` projection;
- a player layer projection from a living service;
- a HUD projection from the same service;
- a screen model plus local session split.

## Testing Rule

Core tests should verify:
- host state changes invalidate the correct projection caches;
- client sync application invalidates only the projections that declare that policy;
- owner and tracking scopes resolve correctly;
- screen session state stays local and is not serialized as host truth;
- projection resolution remains stable when render or UI packs are absent.

Core tests should not attempt to verify:
- real renderer output;
- real widget layout;
- animation timing or shader pipelines.

## Final Rule

When deciding whether a type belongs in `Pibrary`, ask two questions:

1. Is this type part of host truth, sync, projection scope, or invalidation?
2. Can this type stay valid even if no render or UI runtime is installed?

If both answers are yes, it belongs in `Pibrary`.

If the type mainly describes how something is drawn, animated, laid out, themed, or composed on screen, it belongs in `PiRenderBridge`, `PiUI`, or another client-side pack.
