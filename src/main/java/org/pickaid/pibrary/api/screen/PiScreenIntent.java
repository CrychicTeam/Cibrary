package org.pickaid.pibrary.api.screen;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record PiScreenIntent(
        ResourceLocation action,
        Optional<PiScreenHit> hit,
        Optional<PiSlotReference> slotReference,
        int sequence
) {
    public PiScreenIntent {
        Objects.requireNonNull(action, "action");
        hit = Objects.requireNonNull(hit, "hit");
        slotReference = Objects.requireNonNull(slotReference, "slotReference");
    }
}
