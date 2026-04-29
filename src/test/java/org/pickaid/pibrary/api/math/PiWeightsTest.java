package org.pickaid.pibrary.api.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PiWeightsTest {
    @Test
    void chooseUsesPositiveWeights() {
        List<Entry> entries = List.of(new Entry("low", 1.0D), new Entry("mid", 3.0D), new Entry("high", 6.0D));

        Optional<Entry> selected = PiWeights.choose(entries, Entry::weight, new Random(0L));

        assertTrue(selected.isPresent());
        assertEquals("high", selected.get().name());
    }

    @Test
    void zeroTotalReturnsEmpty() {
        assertTrue(PiWeights.choose(List.of(new Entry("none", 0.0D)), Entry::weight, new Random(1L)).isEmpty());
    }

    @Test
    void invalidWeightIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PiWeights.total(List.of(new Entry("bad", -1.0D)), Entry::weight));
    }

    private record Entry(String name, double weight) {
    }
}
