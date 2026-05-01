package org.pickaid.pibrary.dev.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.fml.config.ModConfig;
import org.junit.jupiter.api.Test;

class CounterConfigRuntimeTest {
    @Test
    void exampleRuntimeReadsForgeLoadedValues() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("max_energy", 350);
        config.set("starter_spell", "pibrary:counter_burst");
        CounterConfigRuntime.forgeSpec().setConfig(config);

        assertEquals(ModConfig.Type.COMMON, CounterConfigRuntime.forgeType());
        assertEquals(350, CounterConfigRuntime.maxEnergy());
        assertEquals("pibrary:counter_burst", CounterConfigRuntime.starterSpell());
    }
}
