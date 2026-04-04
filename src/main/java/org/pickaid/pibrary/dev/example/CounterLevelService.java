package org.pickaid.pibrary.dev.example;

import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.service.PiStateLevelService;

public final class CounterLevelService extends PiStateLevelService<CounterState> {
    public CounterLevelService() {
        super(CounterState.class);
    }

    public void startTrial(ResourceLocation id) {
        updateState(state -> state.trial = id);
    }
}
