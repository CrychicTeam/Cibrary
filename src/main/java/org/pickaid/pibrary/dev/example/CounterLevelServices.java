package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiLevelServices;

public final class CounterLevelServices {
    public static final PiLevelServiceType<CounterLevelService> COUNTER_LEVEL =
            PiLevelServices.host(CounterLevelService.class).register();

    private CounterLevelServices() {
    }

    public static void register() {
    }
}
