package org.pickaid.pibrary.runtime.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;

class PiPendingLivingSyncsTest {
    @AfterEach
    void clearQueue() {
        PiPendingLivingSyncs.clear();
    }

    @Test
    void queuedPacketsDrainInInsertionOrderForOneEntity() {
        PiLivingSyncPacket first = new PiLivingSyncPacket(7, PiSyncEnvelopeKind.FULL, PiSyncRoute.OWNER, payload("first"));
        PiLivingSyncPacket second = new PiLivingSyncPacket(7, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, payload("second"));

        PiPendingLivingSyncs.enqueue(first);
        PiPendingLivingSyncs.enqueue(second);

        assertEquals(List.of(first, second), PiPendingLivingSyncs.drain(7));
        assertTrue(PiPendingLivingSyncs.drain(7).isEmpty());
    }

    @Test
    void drainingOneEntityDoesNotConsumeOthers() {
        PiLivingSyncPacket first = new PiLivingSyncPacket(7, PiSyncEnvelopeKind.FULL, PiSyncRoute.OWNER, payload("first"));
        PiLivingSyncPacket second = new PiLivingSyncPacket(9, PiSyncEnvelopeKind.FULL, PiSyncRoute.TRACKING, payload("second"));

        PiPendingLivingSyncs.enqueue(first);
        PiPendingLivingSyncs.enqueue(second);

        assertEquals(List.of(first), PiPendingLivingSyncs.drain(7));
        assertEquals(List.of(second), PiPendingLivingSyncs.drain(9));
    }

    @Test
    void clearDropsQueuedPackets() {
        PiPendingLivingSyncs.enqueue(new PiLivingSyncPacket(7, PiSyncEnvelopeKind.FULL, PiSyncRoute.OWNER, payload("first")));

        PiPendingLivingSyncs.clear();

        assertTrue(PiPendingLivingSyncs.drain(7).isEmpty());
    }

    private static CompoundTag payload(String value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("value", value);
        return tag;
    }
}
