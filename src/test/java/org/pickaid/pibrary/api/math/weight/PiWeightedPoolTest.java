package org.pickaid.pibrary.api.math.weight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import org.junit.jupiter.api.Test;

class PiWeightedPoolTest {
    @Test
    void selectsEntriesByNormalizedWeightAndReportsTotalWeight() {
        PiWeightedPool<String> pool = PiWeightedPool.of(
            PiWeightedEntry.of("low", 1.0),
            PiWeightedEntry.of("mid", 3.0),
            PiWeightedEntry.of("high", 6.0)
        );

        assertEquals(Optional.of("low"), pool.select(0.00));
        assertEquals(Optional.of("mid"), pool.select(0.20));
        assertEquals(Optional.of("high"), pool.select(0.95));
        assertEquals(10.0, pool.totalWeight());
    }

    @Test
    void randomSourceSelectionUsesMojangRandomDirectly() {
        PiWeightedPool<String> pool = PiWeightedPool.of(
            PiWeightedEntry.of("low", 1.0),
            PiWeightedEntry.of("mid", 3.0),
            PiWeightedEntry.of("high", 6.0)
        );

        RandomSource randomForPool = RandomSource.create(12345L);
        RandomSource randomForUnit = RandomSource.create(12345L);

        assertEquals(pool.select(randomForUnit.nextDouble()), pool.select(randomForPool));
    }

    @Test
    void vanillaWeightedListsRoundTripAndShareSelectionSemantics() {
        PiWeightedPool<String> pool = PiWeightedPool.of(
            PiWeightedEntry.of("low", 1.0),
            PiWeightedEntry.of("mid", 3.0),
            PiWeightedEntry.of("high", 6.0)
        );

        SimpleWeightedRandomList<String> vanilla = pool.toVanilla();
        PiWeightedPool<String> roundTrip = PiWeightedPool.fromVanilla(vanilla);

        assertEquals(Optional.of("low"), roundTrip.select(0.00));
        assertEquals(Optional.of("mid"), roundTrip.select(0.20));
        assertEquals(Optional.of("high"), roundTrip.select(0.95));
        assertEquals(pool.totalWeight(), roundTrip.totalWeight());
        assertEquals(3, vanilla.unwrap().size());
    }

    @Test
    void emptyPoolReturnsEmptySelection() {
        PiWeightedPool<String> pool = PiWeightedPool.of();

        assertEquals(Optional.empty(), pool.select(0.5));
        assertEquals(Optional.empty(), pool.select(RandomSource.create(1L)));
        assertEquals(Optional.empty(), PiWeightedPool.fromVanilla(SimpleWeightedRandomList.<String>empty()).select(0.5));
    }

    @Test
    void invalidWeightsAndSelectionUnitsFailFast() {
        assertThrows(IllegalArgumentException.class, () -> PiWeightedEntry.of("bad", 0.0));
        assertThrows(IllegalArgumentException.class, () -> PiWeightedEntry.of("bad", -1.0));
        assertThrows(IllegalArgumentException.class, () -> PiWeightedEntry.of("bad", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> PiWeightedEntry.of("bad", Double.POSITIVE_INFINITY));

        PiWeightedPool<String> pool = PiWeightedPool.of(
            PiWeightedEntry.of("value", 1.0)
        );

        assertThrows(IllegalArgumentException.class, () -> pool.select(-0.01));
        assertThrows(IllegalArgumentException.class, () -> pool.select(1.01));
        assertThrows(IllegalArgumentException.class, () -> pool.select(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> pool.select(Double.POSITIVE_INFINITY));
        assertThrows(IllegalStateException.class, () -> PiWeightedPool.of(PiWeightedEntry.of("fractional", 1.5)).toVanilla());
    }
}
