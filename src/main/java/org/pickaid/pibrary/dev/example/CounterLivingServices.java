package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiLivingServices;

public final class CounterLivingServices {
    public static final PiLivingServiceType<CounterPlayerService> COUNTER_PLAYER =
            PiLivingServices.host(CounterPlayerService.class).register();

    private CounterLivingServices() {
    }

    public static void register() {
    }
}
