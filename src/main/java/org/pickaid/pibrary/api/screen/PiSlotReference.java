package org.pickaid.pibrary.api.screen;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiSlotReference(ResourceLocation containerId, int slotIndex, PiSlotReferenceKind kind) {
    public PiSlotReference {
        Objects.requireNonNull(containerId, "containerId");
        Objects.requireNonNull(kind, "kind");
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must not be negative");
        }
    }
}
