package org.pickaid.pibrary.dev.example;

import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiChunkServices;

public final class CounterChunkServices {
    public static final PiChunkServiceType<CounterChunkService> COUNTER_CHUNK =
            PiChunkServices.host(CounterChunkService.class).register();

    private CounterChunkServices() {
    }

    public static void register() {
    }
}
