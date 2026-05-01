package org.pickaid.pibrary.dev.example;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.pickaid.pibrary.runtime.config.PiForgeConfigBinding;

/**
 * Dev-only Forge config bridge for the counter examples.
 */
public final class CounterConfigRuntime {
    public static final PiForgeConfigBinding GAMEPLAY = PiForgeConfigBinding.build(CounterConfigs.GAMEPLAY);

    private CounterConfigRuntime() {
    }

    public static ModConfig.Type forgeType() {
        return GAMEPLAY.suggestedType();
    }

    public static ForgeConfigSpec forgeSpec() {
        return GAMEPLAY.forgeSpec();
    }

    public static int maxEnergy() {
        return GAMEPLAY.get(CounterConfigs.MAX_ENERGY);
    }

    public static String starterSpell() {
        return GAMEPLAY.get(CounterConfigs.STARTER_SPELL);
    }
}
