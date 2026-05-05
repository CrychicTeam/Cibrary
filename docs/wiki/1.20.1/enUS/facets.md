# Facet State System

Facet is Pibrary's unified state attachment entry for entities, levels, and chunks. Business code uses typed handles instead of manual capability keys.

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
```

Level facets persist by default, but sync is intentionally explicit. Chunk facets persist through chunk capability and never force-load chunks; use `findLoaded(...)` for loaded-only lookup.
