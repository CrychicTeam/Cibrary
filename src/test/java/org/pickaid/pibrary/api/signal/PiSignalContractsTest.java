package org.pickaid.pibrary.api.signal;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PiSignalContractsTest {
    @Test
    void signalCarriesTypedContractFieldsWithoutDispatcher() {
        PiSignalType<TestPayload> type = PiSignalType.create(new ResourceLocation("test", "damage_applied"), TestPayload.class);
        PiSignal signal = PiSignalFrame.builder(type)
                .source(PiSignalSource.entity(11))
                .target(PiSignalTarget.entity(22))
                .scope(PiSignalScope.trackingEntity(22))
                .priority(PiSignalPriority.NORMAL)
                .gameTime(30L)
                .payload(new TestPayload(7))
                .build();

        assertEquals(type, signal.type());
        assertEquals(30L, signal.gameTime());
        assertEquals(PiSignalScope.trackingEntity(22), signal.scope());
    }

    private record TestPayload(int value) {
    }
}
