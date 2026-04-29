package org.pickaid.pibrary.runtime.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.LivingEntity;

/**
 * Client-side holding area for living sync packets received before the target entity exists.
 */
final class PiPendingLivingSyncs {
    private static final Map<Integer, List<PiLivingSyncPacket>> PENDING = new ConcurrentHashMap<>();

    private PiPendingLivingSyncs() {
    }

    static void enqueue(PiLivingSyncPacket packet) {
        PENDING.compute(packet.entityId(), (entityId, packets) -> {
            List<PiLivingSyncPacket> next = packets == null ? new ArrayList<>() : new ArrayList<>(packets);
            next.add(packet);
            return next;
        });
    }

    static List<PiLivingSyncPacket> drain(int entityId) {
        List<PiLivingSyncPacket> packets = PENDING.remove(entityId);
        return packets == null ? List.of() : List.copyOf(packets);
    }

    static void applyTo(LivingEntity living) {
        for (PiLivingSyncPacket packet : drain(living.getId())) {
            PiLivingSyncMessages.apply(living, packet);
        }
    }

    static void clear() {
        PENDING.clear();
    }
}
