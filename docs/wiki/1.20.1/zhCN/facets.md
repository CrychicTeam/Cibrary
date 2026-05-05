# Facet 状态系统

Facet 是 Pibrary 对实体、level、chunk 状态挂载的统一入口。业务代码拿 typed handle，不手写 capability key，也不手动找 generated descriptor。

## Living / Player

```java
@PiLivingFacet(namespace = "example", path = "counter_player")
public final class CounterPlayerFacet extends PiStatePlayerFacet<CounterState> {
    public CounterPlayerFacet(PiLivingFacetContext context) {
        super(context);
    }
}

public final class ExampleLivingFacets {
    public static final PiLivingFacetType<CounterPlayerFacet> COUNTER_PLAYER =
            PiLivingFacets.bind(CounterPlayerFacet.class).register();

    public static void register() {
    }
}

CounterPlayerFacet facet = ExampleLivingFacets.COUNTER_PLAYER.get(player);
facet.gainEnergy(2);
```

## Level

Level Facet 默认负责持久化。是否同步由更高层决定，避免 level 级状态默认广播成网络负担。

```java
@PiLevelFacet(namespace = "example", path = "counter_level")
public final class CounterLevelFacet extends PiStateLevelFacet<CounterState> {
    public CounterLevelFacet(PiLevelFacetContext context) {
        super(context);
    }
}

public final class ExampleLevelFacets {
    public static final PiLevelFacetType<CounterLevelFacet> COUNTER_LEVEL =
            PiLevelFacets.bind(CounterLevelFacet.class).register();

    public static void register() {
    }
}
```

## Chunk

Chunk Facet 走 chunk capability 持久化，不会主动加载或生成 chunk。查询已加载 chunk 时使用 `findLoaded(...)`。

```java
Optional<OreMemoryFacet> memory =
        PiChunkFacets.findLoaded(serverLevel, chunkPos, OreMemoryFacet.class);
```

模组初始化时调用自己的 `ExampleLivingFacets.register()` / `ExampleLevelFacets.register()` / `ExampleChunkFacets.register()`，让静态 handle 被加载。
