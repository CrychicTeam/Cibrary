package org.pickaid.pibrary.dev.example;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValidators;

/**
 * Dev-only config contract used by the counter examples.
 */
public final class CounterConfigs {
    public static final PiConfigEntry<Integer> MAX_ENERGY = PiConfigEntry
            .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
            .comment("Maximum energy stored by the counter examples.")
            .alsoValidate(PiConfigValidators.intRange(1, 10_000))
            .build();

    public static final PiConfigEntry<String> STARTER_SPELL = PiConfigEntry
            .builder(id("gameplay/starter_spell"), Codec.STRING, PiConfigScope.COMMON_BOOTSTRAP, "pibrary:counter_pulse")
            .comment("Spell id used by examples that need a configured starter spell.")
            .alsoValidate(PiConfigValidators.notBlank("starter spell must not be blank"))
            .alsoValidate(value -> value.contains(":")
                    ? Optional.empty()
                    : Optional.of("starter spell must include namespace"))
            .build();

    public static final PiConfigSpec GAMEPLAY = PiConfigSpec
            .builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
            .entry(MAX_ENERGY)
            .entry(STARTER_SPELL)
            .build();

    private CounterConfigs() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("pibrary", path);
    }
}
