package org.pickaid.pibrary.dev.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigValues;

class CounterConfigsTest {
    @Test
    void exampleConfigSpecHasStableIdsAndValidation() {
        assertEquals(new ResourceLocation("pibrary", "gameplay"), CounterConfigs.GAMEPLAY.id());
        assertEquals(PiConfigScope.COMMON_BOOTSTRAP, CounterConfigs.GAMEPLAY.scope());
        assertEquals(2, CounterConfigs.GAMEPLAY.entries().size());
        assertTrue(CounterConfigs.GAMEPLAY.validateDefaults().isEmpty());
        assertTrue(CounterConfigs.GAMEPLAY.find(new ResourceLocation("pibrary", "gameplay/max_energy")).isPresent());
        assertEquals(100, PiConfigValues.defaults(CounterConfigs.GAMEPLAY).get(CounterConfigs.MAX_ENERGY));
    }

    @Test
    void exampleConfigEntryRejectsBadRuntimeValue() {
        assertEquals(List.of("starter spell must include namespace"), CounterConfigs.STARTER_SPELL.validate("counter_pulse"));
    }
}
