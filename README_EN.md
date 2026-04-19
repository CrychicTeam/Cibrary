# Pibrary

[中文版](README.MD)

`Pibrary` is the root foundation mod for the PickAID stack. It is no longer the place where every engine and compat layer gets folded back into one monolith.

The start-stage goals are explicit:
1. keep `pibrary` as a real public core mod;
2. define stable cross-repo contracts for the parts every repo will share;
3. provide one common root for future independent repos instead of growing the old monolith further;
4. preserve legacy code for migration, without letting the README pretend that legacy structure is still the target architecture.

## PiRegistrate

`PiRegistrate` is now the preferred main registration root. Normal content, registries, host services, config, and creative tabs can all start from one place instead of scattering registration code across unrelated helpers.

```java
public static final PiRegistrate REG = PiRegistrate.create(MODID);

public static final PiRegistryHandle<SpellType> SPELL_TYPES =
        REG.customRegistry("spell_type", SpellType.class)
                .codec(SpellType.CODEC)
                .defaultKey("basic")
                .register();

public static final PiLevelServiceType<WeatherService> WEATHER =
        REG.levelService("weather", WeatherState.class, WeatherService::new)
                .persisted()
                .noSyncByDefault()
                .register(WeatherService.class);

public static final PiConfigEntry<Integer> COMBAT =
        REG.config("combat", Codec.INT, 5)
                .scope(PiConfigScope.SERVER_DATA_PACK)
                .register();

public static final PiCreativeTabRegistration MAIN_TAB =
        REG.creativeTab("main", "PickAID")
                .register();
```

If you already use direct `PiLivingServices.host(...)`, `PiChunkServices.host(...)`, or `PiLevelServices.host(...)` registration, that path still works. The main change is that `PiRegistrate` is now the cleaner default entry point for new code.

## Use It Today

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

If you want one persistent level service, use the same pattern: define the service class, register one typed handle, and resolve through that handle.

```java
@PiLevelService(namespace = "example", path = "counter_level")
public final class CounterLevelService extends PiStateLevelService<CounterState> {
    public CounterLevelService(PiLevelServiceContext context) {
        super(context);
    }
}

public final class ExampleLevelServices {
    public static final PiLevelServiceType<CounterLevelService> COUNTER_LEVEL =
            PiLevelServices.host(CounterLevelService.class).register();

    private ExampleLevelServices() {
    }

    public static void register() {
    }
}

CounterLevelService service = ExampleLevelServices.COUNTER_LEVEL.get(level);
```

Call `ExampleLevelServices.register()` during your own mod bootstrap. `Pibrary` bootstraps descriptor discovery, but your mod still decides which discovered services become active.

If you want one persistent chunk service, keep the same handle-first pattern.

```java
@PiChunkService(namespace = "example", path = "counter_chunk")
public final class CounterChunkService extends PiStateChunkService<CounterState> {
    public CounterChunkService(PiChunkServiceContext context) {
        super(context);
    }
}

public final class ExampleChunkServices {
    public static final PiChunkServiceType<CounterChunkService> COUNTER_CHUNK =
            PiChunkServices.host(CounterChunkService.class).register();

    private ExampleChunkServices() {
    }

    public static void register() {
    }
}

CounterChunkService service = ExampleChunkServices.COUNTER_CHUNK.get(chunk);
Optional<CounterChunkService> existing = PiChunkServices.find(level, chunkPos, CounterChunkService.class);
CounterChunkService resolved = PiChunkServices.resolve(level, chunkPos, CounterChunkService.class);
```

`find(level, chunkPos, ...)` never loads a chunk. `resolve(level, chunkPos, ...)` is the explicit path that may resolve it.

## Repo Direction

`Pibrary` is being shaped around these long-term responsibilities:
1. core service location and lightweight service registry contracts;
2. shared API boundaries for config, diagnostics, state, targeting, and registry support;
3. living-service registration, attachment, and sync as a stable root pattern for host-owned state;
4. JEI-neutral module, bootstrap, and spec contracts for an optional `PiJEICompat` runtime;
5. future shared boundaries such as entity lifecycle, projectile, and presentation contracts when they are ready to live in the root repo instead of one gameplay repo;
6. the minimum stable root dependency for the Pi series;
7. shared language for the independent foundation libs and higher-level engine mods.

These are no longer part of `Pibrary`'s final scope:
1. networking runtime implementation;
2. serialization runtime implementation;
3. KubeJS plugin acceleration and RecipeJS compat;
4. object-counter and reaction-graph gameplay logic itself;
5. camera, narrative, UI, animation, entity FX, and data-graph engines;
6. the render engine runtime, backend compatibility, and visual effect pipelines.

## Presentation Direction

As the repo family splits further, one rule should stay stable:

`host owns truth, clients consume projections`

In practice, that means:
1. hosts keep the authoritative state;
2. client render and UI layers should consume derived views instead of poking raw host state directly;
3. anything that must stay in the root repo should stay small, explicit, and easy to share across repos.

## Repo Relationships

Recommended structure at this stage:
1. `Pibrary`: root core mod and shared API contracts.
2. `PiNet`: independent networking foundation.
3. `PiSerializeKit`: independent serialization foundation.
4. `PiKubeJSCompat`: independent KubeJS / RecipeJS / builder compat toolkit.
5. `PiJEICompat`: independent JEI compat repo that turns the neutral `api/jei` contracts into a real recipe-viewer plugin.
6. `PiDataGraph`: object counters, reaction chains, and graph-driven logic built on top of `PiSerializeKit`.
7. future `PiEngine` family repos: camera, UI, animation, story, render bridge, and other engine-level packs.

As that split continues:
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

## Usable Now

In the current committed tree, the most usable public packages are:
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
6. `api/service`
   living-service annotations, typed handles, descriptor bootstrap, and state-backed base classes.
7. `api/targeting`
   target query contracts and resolvers.
8. `api/entity`
   current entity lifecycle, spatial index, and living-hurt boundaries.
9. `api/projectile`
   the current projectile tracing contracts.
10. `api/jei`
   JEI-neutral recipe-viewer contracts, module bootstrap surfaces, and GUI / alias / transfer specs.

The living-service example above is backed by committed runtime code in `runtime/capability`, `runtime/service`, `runtime/state`, and `runtime/sync`.

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
