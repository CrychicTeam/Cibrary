# Pibrary

[中文版](README.MD)

`Pibrary` is the root foundation mod for the PickAID stack. It is no longer the place where every engine and compat layer gets folded back into one monolith.

The start-stage goals are explicit:
1. keep `pibrary` as a real public core mod;
2. define stable cross-repo contracts for the parts every repo will share;
3. provide one common root for future independent repos instead of growing the old monolith further;
4. preserve legacy code for migration, without letting the README pretend that legacy structure is still the target architecture.

## Current Role

`Pibrary` now owns these long-term core responsibilities:
1. core service location and lightweight service registry contracts;
2. shared API boundaries for config, diagnostics, state, targeting, and registry support;
3. shared boundaries for entity lifecycle, vehicle lifecycle, targeting, high-performance projectile tracing, projectile lifecycle, and projectile impact handling;
4. JEI-neutral module, bootstrap, and spec contracts for an optional `PiJEICompat` runtime;
5. the presentation core for `world_render`, `hud`, and `screen`, including typed projection contracts, cache, invalidation, and local screen-session boundaries;
6. minimal render-core bridge hooks where host sync or host refresh state must stay in core;
7. the minimum stable root dependency for the Pi series;
8. shared language for the independent foundation libs and higher-level engine mods.

These are no longer part of `Pibrary`'s final scope:
1. networking runtime implementation;
2. serialization runtime implementation;
3. KubeJS plugin acceleration and RecipeJS compat;
4. object-counter and reaction-graph gameplay logic itself;
5. camera, narrative, UI, animation, entity FX, and data-graph engines;
6. the render engine runtime, backend compatibility, and visual effect pipelines.

## Presentation Core

`Pibrary` now follows one stable rule:

`host owns truth, clients consume projections`

That means:
1. block entities, living services, and future hosts keep the authoritative state;
2. `world_render`, `hud`, and `screen` consumers resolve typed projections instead of reading raw host fields directly;
3. client apply, sync apply, menu data, and explicit invalidation refresh projection caches in one core path;
4. local screen session state remains client-local and does not silently overwrite host truth;
5. render-family packs and `PiUI` consume these contracts instead of redefining sync ownership themselves.

## Living Services

If you want a player service with persistent state, register the typed handle once and use that handle everywhere you resolve the service.

```java
@PiLivingService(namespace = "example", path = "counter_player")
public final class CounterPlayerService extends PiStatePlayerService<CounterState> {
    public CounterPlayerService(PiLivingServiceContext context) {
        super(context);
    }
}

public final class ExampleLivingServices {
    public static final PiLivingServiceType<CounterPlayerService> COUNTER_PLAYER =
            PiLivingServices.host(CounterPlayerService.class).register();

    private ExampleLivingServices() {
    }

    public static void register() {
    }
}

CounterPlayerService service = ExampleLivingServices.COUNTER_PLAYER.get(player);
```

Call `ExampleLivingServices.register()` during common bootstrap after descriptor discovery and before sync/bootstrap code that depends on active living services. The empty method is there to force class loading, so the handle field registers exactly once in a place you control.

## Repo Relationships

Recommended structure at this stage:
1. `Pibrary`: root core mod and shared API contracts.
2. `PiNet`: independent networking foundation.
3. `PiSerializeKit`: independent serialization foundation.
4. `PiKubeJSCompat`: independent KubeJS / RecipeJS / builder compat toolkit.
5. `PiJEICompat`: independent JEI compat repo that turns the neutral `api/jei` contracts into a real recipe-viewer plugin.
6. `PiDataGraph`: object counters, reaction chains, and graph-driven logic built on top of `PiSerializeKit`.
7. future `PiEngine` family repos: camera, UI, animation, story, render bridge, and other engine-level packs.

In that split:
1. `Pibrary` owns host truth, presentation contracts, and invalidation policy.
2. `PiRenderBridge` and render-family packs own the real world render runtime.
3. `PiUI` owns HUD runtime, screen runtime, widgets, and richer client-side view systems.

## Current Code State

This repo is intentionally split into two layers:
1. `src/main/java/org/pickaid/pibrary/api/**`
   This is where the new core-facing API surface starts.
2. `src/main/java/org/pickaid/pibrary/content/**` and other legacy packages
   These still contain old monolithic-era code that will be migrated out over time.

The preserved legacy snapshot lives in:
`restore/legacy-monolith-20260331`

Its purpose is:
1. keep the previous implementation available for reference and migration;
2. avoid pushing old monolithic assumptions back into the new architecture;
3. let the new contracts replace old behavior incrementally instead of deleting everything blindly.

## Start-Stage Core API Surface

The current core-facing packages are being defined around:
1. `api/core`
   service keys and service registry contracts.
2. `api/config`
   config entries and config scope boundaries.
3. `api/diagnostics`
   unified debug and diagnostics sinks.
4. `api/registry`
   registration requests and phases for future registry helpers.
5. `api/state`
   shared state keys and sync policy.
6. `api/targeting`
   target query contracts and resolvers.
7. `api/entity`
   entity lifecycle, vehicle lifecycle, and hurt-handling boundaries.
8. `api/projectile`
   performance-oriented projectile tracing, lifecycle hooks, impact handling, and isolated projectile runtime contracts.
9. `api/jei`
   JEI-neutral recipe-viewer contracts, module bootstrap surfaces, and GUI / alias / transfer specs.

These are deliberately lightweight right now:
1. first make the boundaries correct;
2. make them reusable across multiple repos;
3. let real engine and compat work decide where concrete implementations belong.

## Explicitly Not Heavy Yet

This stage does not try to fully implement:
1. automatic config assembly;
2. a generic sync runtime;
3. a complete registry runtime helper;
4. advanced targeting geometry systems;
5. a one-shot migration of all legacy gameplay code;
6. the object-counter logic itself, which belongs in `PiDataGraph`.

## Maven

The repo currently keeps the existing publication path:

```gradle
repositories {
    maven { url = 'https://maven.mihono.cn/repository/pickaid1201/' }
}
```

## Next Direction

1. make `Pibrary`'s core APIs consumable by `PiNet`, `PiSerializeKit`, `PiKubeJSCompat`, and future engine repos;
2. let `PiDataGraph` own counter and graph logic while `PiSerializeKit` provides the typed serialization boundary;
3. keep extracting reusable pieces from the legacy monolith into stable contracts;
4. continue the repo split by responsibility instead of letting `pibrary` grow back into the old all-in-one structure.
