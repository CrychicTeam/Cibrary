package org.pickaid.pibrary.api.screen;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record PiScreenHit(
        ResourceLocation areaId,
        int zOrder,
        PiScreenPoint localPoint,
        Optional<PiSlotReference> slotReference
) {
    public PiScreenHit {
        Objects.requireNonNull(areaId, "areaId");
        Objects.requireNonNull(localPoint, "localPoint");
        slotReference = Objects.requireNonNull(slotReference, "slotReference");
    }

    public static PiScreenHit area(ResourceLocation areaId, int zOrder, PiScreenPoint localPoint) {
        return new PiScreenHit(areaId, zOrder, localPoint, Optional.empty());
    }
}
