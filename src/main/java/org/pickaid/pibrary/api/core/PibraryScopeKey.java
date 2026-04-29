package org.pickaid.pibrary.api.core;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed identifier for a scoped value registered in the {@link PibraryScopeRegistry}.
 *
 * @param id unique logical id of the scoped value
 * @param type runtime type used for validation and lookup
 * @param <T> value contract type
 */
public record PibraryScopeKey<T>(ResourceLocation id, Class<T> type) {
    public PibraryScopeKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
    }
}
