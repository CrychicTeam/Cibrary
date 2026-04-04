package org.pickaid.pibrary.api.state;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiStateKey<T>(
        ResourceLocation id,
        Class<T> valueType,
        PiStateSyncMode syncMode
) {
    public PiStateKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(valueType, "valueType");
        Objects.requireNonNull(syncMode, "syncMode");
    }
}
