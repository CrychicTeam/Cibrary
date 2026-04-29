package org.pickaid.pibrary.api.state;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed logical key for state values exposed outside generated schema bindings.
 *
 * @param id logical state id
 * @param valueType runtime value type
 * @param syncMode sync policy for the value
 * @param <T> value type
 */
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
