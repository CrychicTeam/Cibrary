package org.pickaid.pibrary.dev.example;

import net.minecraft.data.PackOutput;
import org.pickaid.pibrary.api.config.PiDataConfigCollector;
import org.pickaid.pibrary.runtime.config.PiDataConfigProvider;

/**
 * Dev-only datapack config generator for the spell samples.
 */
public final class CounterSpellConfigProvider extends PiDataConfigProvider {
    public CounterSpellConfigProvider(PackOutput output) {
        super(output, "pibrary_config", "Pibrary spell config samples");
    }

    @Override
    protected void add(PiDataConfigCollector collector) {
        CounterSpellConfigs.collectDefaults(collector);
    }
}
